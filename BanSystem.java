package me.hero.bansystem;

import me.hero.bansystem.commands.*;
import me.hero.bansystem.listeners.*;
import me.hero.bansystem.managers.*;
import me.hero.bansystem.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BanSystem extends JavaPlugin {

    private MessageUtil messageUtil;
    private BanManager banManager;
    private MuteManager muteManager;
    private IpBanManager ipBanManager;
    private VanishManager vanishManager;

    @Override
    public void onEnable() {
        // Create folder and save default config
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig();

        // 1. Initialize Message Utility
        saveResource("messages.yml", false);
        this.messageUtil = new MessageUtil(this);

        // 2. Initialize Managers
        this.banManager = new BanManager(this);
        this.muteManager = new MuteManager(this);
        this.ipBanManager = new IpBanManager(this);
        this.vanishManager = new VanishManager(this);

        // 3. Load Persistent Data
        banManager.loadBans();
        muteManager.loadMutes();
        ipBanManager.loadBans();
        vanishManager.loadVanishes();

        // 4. Register Listeners
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BanListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new IpListener(this), this);
        pm.registerEvents(new JoinQuitListener(this), this);
        pm.registerEvents(vanishManager, this); // Important for Mob Protection logic

        // 5. Register Commands
        registerCommand("bansystem", new BanSystemCommand(this));
        registerCommand("tempban", new TempBanCommand(this));
        registerCommand("unban", new UnbanCommand(this));
        registerCommand("tempmute", new TempMuteCommand(this));
        registerCommand("unmute", new UnmuteCommand(this));
        registerCommand("ipban", new IpBanCommand(this));
        registerCommand("unipban", new UnipBanCommand(this));
        registerCommand("alts", new AltsCommand(this));
        registerCommand("history", new HistoryCommand(this));
        registerCommand("seeinv", new SeeInvCommand(this));
        registerCommand("vanish", new VanishCommand(vanishManager)); // Pass manager here

        getLogger().info("BanSystem has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Save all data to files on shutdown
        if (banManager != null) banManager.saveBans();
        if (muteManager != null) muteManager.saveMutes();
        if (ipBanManager != null) ipBanManager.saveBans();
        if (vanishManager != null) vanishManager.saveVanishes();

        getLogger().info("BanSystem has been disabled.");
    }

    /**
     * Helper method to safely register commands and warn if they are missing from plugin.yml
     */
    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
        } else {
            getLogger().warning("Command /" + name + " is defined in code but missing from plugin.yml!");
        }
    }

    // Getters for other classes to access managers
    public MessageUtil getMessages() { return messageUtil; }
    public BanManager getBanManager() { return banManager; }
    public MuteManager getMuteManager() { return muteManager; }
    public IpBanManager getIpBanManager() { return ipBanManager; }
    public VanishManager getVanishManager() { return vanishManager; }

    // Helper for config checks
    public boolean isFeatureEnabled(String path) {
        return getConfig().getBoolean("features." + path, true);
    }

    public String getStaffNotifyPermission() {
        return getConfig().getString("settings.staff-notify-permission", "bansystem.notify");
    }

    public void reloadMessages() {
        if (messageUtil != null) messageUtil.reload();
    }
}