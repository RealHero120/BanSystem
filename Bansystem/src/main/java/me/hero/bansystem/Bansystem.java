// java
package me.hero.bansystem;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import me.hero.bansystem.commands.*;
import me.hero.bansystem.combat.CombatManager;
import me.hero.bansystem.listeners.*;
import me.hero.bansystem.managers.*;
import me.hero.bansystem.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class Bansystem extends JavaPlugin {

    // WorldGuard Flags
    public static StateFlag COMBATGUARD_SAFE_FLAG;
    public static StateFlag COMBATGUARD_TAG_FLAG;

    // Managers
    private BanManager banManager;
    private MuteManager muteManager;
    private IpBanManager ipBanManager;
    private SecureOpManager secureOpManager;
    private CombatManager combatManager;
    private SafeZoneManager safeZoneManager;

    // Utils
    private MessageUtil messageUtil;
    private FileConfiguration messagesConfig;
    private volatile boolean shuttingDown = false;

    @Override
    public void onLoad() {
        // Flags should be registered in onLoad if WorldGuard is present
        registerWorldGuardFlags();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createMessagesConfig();

        // Initialize utilities & managers
        this.messageUtil = new MessageUtil(this);
        this.banManager = new BanManager(this);
        this.muteManager = new MuteManager(this);
        this.ipBanManager = new IpBanManager(this);
        this.secureOpManager = new SecureOpManager(this);

        // Combat related managers
        this.safeZoneManager = new SafeZoneManager(this);
        this.combatManager = new CombatManager(this, safeZoneManager);

        // Load persisted data
        try {
            if (banManager != null) banManager.loadBans();
            if (muteManager != null) muteManager.loadMutes();
            if (ipBanManager != null) ipBanManager.loadBans();
            if (secureOpManager != null) secureOpManager.loadAllowedLists();
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "Failed loading persisted data", t);
        }

        // Register listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new BanListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new IpListener(this), this);
        pm.registerEvents(new JoinQuitListener(this), this);
        pm.registerEvents(new OPListener(this), this);

        // Combat listeners (typed CombatManager)
        pm.registerEvents(new CombatListener(this, combatManager), this);
        pm.registerEvents(new SessionListener(this, combatManager), this);
        pm.registerEvents(new CommandListener(this, combatManager), this);
        pm.registerEvents(new CombatRestrictionListener(this, combatManager, safeZoneManager), this);

        // Death listener delegates to combatManager
        pm.registerEvents(new Listener() {
            @EventHandler
            public void onDeath(org.bukkit.event.entity.PlayerDeathEvent e) {
                if (combatManager != null) combatManager.handleDeath(e.getEntity());
            }
        }, this);

        // Commands
        registerCommands();

        // Tasks
        startTasks();

        getLogger().info("Bansystem & CombatGuard Merged Plugin Enabled.");
    }

    private void registerCommands() {
        // Moderation Commands
        registerCmd("tempban", new TempBanCommand(this));
        registerCmd("unban", new UnbanCommand(this));
        registerCmd("ipban", new IpBanCommand(this));
        registerCmd("unipban", new UnipBanCommand(this));
        registerCmd("tempmute", new TempMuteCommand(this));
        registerCmd("unmute", new UnmuteCommand(this));
        registerCmd("alts", new AltsCommand(this));
        registerCmd("history", new HistoryCommand(this));
        registerCmd("secureop", new SecureOpCommand(this));

        // Combat Command (ensure combatManager may be null-checked by command)
        registerCmd("combat", new CombatCommand(this, combatManager));
    }

    private void registerCmd(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) getCommand(name).setExecutor(executor);
    }

    private void startTasks() {
        // SecureOp scanner: must run on main thread because it interacts with Player API.
        if (secureOpManager != null && getConfig().getBoolean("secureop.scan-enabled", true)) {
            long interval = Math.max(1L, getConfig().getLong("secureop.scan-interval-ticks", 200L));
            // Schedule on the main thread (runTaskTimer) instead of asynchronously
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                try {
                    secureOpManager.scanForUnauthorizedOps();
                } catch (Throwable t) {
                    getLogger().log(Level.WARNING, "SecureOp scan failed", t);
                }
            }, interval, interval);
        }

        // Start combat ticker if available
        if (combatManager != null) {
            try {
                combatManager.startTicker();
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Failed to start combat ticker", t);
            }
        }
    }

    @Override
    public void onDisable() {
        shuttingDown = true;

        // Stop combat ticker cleanly
        try {
            if (combatManager != null) combatManager.stopTicker();
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "Failed to stop combat ticker", t);
        }

        // Cancel any plugin tasks as a safety measure
        try {
            Bukkit.getScheduler().cancelTasks(this);
        } catch (Throwable ignored) {}

        // Persist managers
        try {
            if (banManager != null) banManager.saveBans();
            if (muteManager != null) muteManager.saveMutes();
            if (ipBanManager != null) ipBanManager.saveBans();
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "Failed to save moderation data", t);
        }

        // Persist secure OP allowed lists
        if (secureOpManager != null) {
            try {
                secureOpManager.saveAllowedLists();
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Failed to save SecureOp allowed lists", t);
            }
        }

        getLogger().info("Data saved. Plugin disabled.");
    }

    // --- WorldGuard flag helpers ---

    private void registerWorldGuardFlags() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) return;
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            registerFlag(registry, "combatguard-safe", false);
            registerFlag(registry, "combatguard-tag", true);
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "Failed to register WorldGuard flags", t);
        }
    }

    private void registerFlag(FlagRegistry registry, String name, boolean def) {
        try {
            StateFlag flag = new StateFlag(name, def);
            registry.register(flag);
            if (name.contains("safe")) COMBATGUARD_SAFE_FLAG = flag;
            else COMBATGUARD_TAG_FLAG = flag;
        } catch (FlagConflictException e) {
            // If a flag with the name already exists, try to use it if it's a StateFlag
            try {
                var existing = registry.get(name);
                if (existing instanceof StateFlag sf) {
                    if (name.contains("safe")) COMBATGUARD_SAFE_FLAG = sf;
                    else COMBATGUARD_TAG_FLAG = sf;
                }
            } catch (Throwable t) {
                getLogger().log(Level.FINE, "Flag conflict resolution failed for " + name, t);
            }
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "Unexpected error registering flag " + name, t);
        }
    }

    // --- Utils / config ---

    private void createMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) saveResource("messages.yml", false);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    // --- Getters required by other classes (added to fix compile errors) ---

    public BanManager getBanManager() { return banManager; }
    public MuteManager getMuteManager() { return muteManager; }
    public IpBanManager getIpBanManager() { return ipBanManager; }
    public SecureOpManager getSecureOpManager() { return secureOpManager; }
    public CombatManager getCombatManager() { return combatManager; }
    public SafeZoneManager getSafeZoneManager() { return safeZoneManager; }
    public MessageUtil getMessageUtil() { return messageUtil; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
    public boolean isShuttingDown() { return shuttingDown; }
}