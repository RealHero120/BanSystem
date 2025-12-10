package me.hero.bansystem;

import me.hero.bansystem.commands.AltsCommand;
import me.hero.bansystem.commands.BanSystemCommand;
import me.hero.bansystem.commands.IpBanCommand;
import me.hero.bansystem.commands.SeeInvCommand;
import me.hero.bansystem.commands.TempBanCommand;
import me.hero.bansystem.commands.TempMuteCommand;
import me.hero.bansystem.commands.UnbanCommand;
import me.hero.bansystem.commands.UnipBanCommand;
import me.hero.bansystem.commands.UnmuteCommand;
import me.hero.bansystem.commands.VanishCommand;
import me.hero.bansystem.listeners.BanListener;
import me.hero.bansystem.listeners.ChatListener;
import me.hero.bansystem.listeners.IpListener;
import me.hero.bansystem.listeners.JoinQuitListener;
import me.hero.bansystem.managers.BanManager;
import me.hero.bansystem.managers.IpBanManager;
import me.hero.bansystem.managers.MuteManager;
import me.hero.bansystem.managers.VanishManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class BanSystem extends JavaPlugin {

    private static BanSystem instance;

    // Managers
    private BanManager banManager;
    private MuteManager muteManager;
    private VanishManager vanishManager;
    private IpBanManager ipBanManager;

    // Messages
    private MessageUtil messages;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        // Core configs
        saveDefaultConfig();
        saveDefaultMessages();
        reloadMessages();
        this.messages = new MessageUtil(this);

        // Managers
        this.banManager = new BanManager(this);
        this.banManager.loadBans();

        this.muteManager = new MuteManager(this);
        this.muteManager.loadMutes();

        this.vanishManager = new VanishManager(this);

        this.ipBanManager = new IpBanManager(this);

        // Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new JoinQuitListener(this), this);
        pm.registerEvents(new BanListener(this), this);
        pm.registerEvents(new IpListener(this), this);
        pm.registerEvents(vanishManager, this); // VanishManager implements Listener

        // Commands
        if (getCommand("bansystem") != null) {
            getCommand("bansystem").setExecutor(new BanSystemCommand(this));
        }

        if (getCommand("tempban") != null) {
            getCommand("tempban").setExecutor(new TempBanCommand(this));
        }

        if (getCommand("tempmute") != null) {
            getCommand("tempmute").setExecutor(new TempMuteCommand(this));
        }

        if (getCommand("unmute") != null) {
            getCommand("unmute").setExecutor(new UnmuteCommand(this));
        }

        if (getCommand("unban") != null) {
            getCommand("unban").setExecutor(new UnbanCommand(this));
        }

        if (getCommand("history") != null) {
            getCommand("history").setExecutor(new me.hero.bansystem.commands.HistoryCommand(this));
        }

        if (getCommand("ipban") != null) {
            getCommand("ipban").setExecutor(new IpBanCommand(this));
        }

        if (getCommand("unipban") != null) {
            getCommand("unipban").setExecutor(new UnipBanCommand(this));
        }

        if (getCommand("alts") != null) {
            getCommand("alts").setExecutor(new AltsCommand(this));
        }

        if (getCommand("vanish") != null) {
            getCommand("vanish").setExecutor(new VanishCommand(this));
        }

        if (getCommand("seeinv") != null) {
            getCommand("seeinv").setExecutor(new SeeInvCommand(this));
        }

        getLogger().info("BanSystem enabled.");
    }

    @Override
    public void onDisable() {
        if (muteManager != null) {
            muteManager.saveMutes();
        }

        if (banManager != null) {
            banManager.saveBans();
        }

        if (ipBanManager != null) {
            ipBanManager.saveIpBans();
            ipBanManager.saveIpMap();
        }

        getLogger().info("BanSystem disabled.");
    }

    // ===== messages.yml handling =====

    private void saveDefaultMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            getDataFolder().mkdirs();
            saveResource("messages.yml", false);
        }
    }

    public void reloadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveDefaultMessages();
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getMessagesConfig() {
        if (messagesConfig == null) {
            reloadMessages();
        }
        return messagesConfig;
    }

    public MessageUtil getMessages() {
        return messages;
    }

    // ===== config helpers =====

    public boolean isFeatureEnabled(String key) {
        return getConfig().getBoolean("features." + key, true);
    }

    public String getStaffNotifyPermission() {
        return getConfig().getString("settings.staff-notify-permission", "bansystem.notify");
    }

    public boolean allowPermanentBans() {
        return getConfig().getBoolean("settings.allow-permanent-bans", true);
    }

    public String getMaxTempDurationString() {
        return getConfig().getString("settings.max-temp-duration", "30d");
    }

    // ===== singleton + manager getters =====

    public static BanSystem getInstance() {
        return instance;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public IpBanManager getIpBanManager() {
        return ipBanManager;
    }
}
