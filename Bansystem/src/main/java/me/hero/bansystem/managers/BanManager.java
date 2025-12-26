package me.hero.bansystem.managers;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.events.PlayerBanEvent;
import me.hero.bansystem.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanManager {
    private final Bansystem plugin;
    private final Map<UUID, BanEntry> bans = new HashMap<>();
    private final File file;

    public record BanEntry(String targetName, long expiry, String reason, String staff) {}

    public BanManager(Bansystem plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bans.yml");
    }

    public void loadBans() {
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("bans");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String name = cfg.getString("bans." + key + ".name", "Unknown");
                    long expiry = cfg.getLong("bans." + key + ".expiry");
                    String reason = cfg.getString("bans." + key + ".reason", "No reason provided");
                    String staff = cfg.getString("bans." + key + ".staff", "Console");
                    bans.put(uuid, new BanEntry(name, expiry, reason, staff));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveBans() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, BanEntry> entry : bans.entrySet()) {
            String path = "bans." + entry.getKey().toString();
            cfg.set(path + ".name", entry.getValue().targetName());
            cfg.set(path + ".expiry", entry.getValue().expiry());
            cfg.set(path + ".reason", entry.getValue().reason());
            cfg.set(path + ".staff", entry.getValue().staff());
        }
        try { cfg.save(file); } catch (IOException e) { plugin.getLogger().severe("Could not save bans.yml!"); }
    }

    public void ban(UUID uuid, String targetName, long duration, String reason, String staff) {
        long expiry = (duration == -1) ? -1 : System.currentTimeMillis() + duration;
        bans.put(uuid, new BanEntry(targetName, expiry, reason, staff));
        saveBans();

        String readableDuration = (duration == -1) ? "Permanent" : TimeUtil.formatDuration(duration);
        Bukkit.getPluginManager().callEvent(new PlayerBanEvent(targetName, staff, reason, readableDuration));
    }

    public void unban(UUID uuid) {
        bans.remove(uuid);
        saveBans();
    }

    public boolean isBanned(UUID uuid) {
        if (!bans.containsKey(uuid)) return false;
        BanEntry entry = bans.get(uuid);
        if (entry.expiry() != -1 && entry.expiry() < System.currentTimeMillis()) {
            bans.remove(uuid);
            saveBans();
            return false;
        }
        return true;
    }

    public BanEntry getBan(UUID uuid) { return bans.get(uuid); }
}