package me.hero.bansystem.managers;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanManager {

    private final BanSystem plugin;
    private final Map<UUID, BanEntry> bans = new HashMap<>();
    private final File file;

    public BanManager(BanSystem plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bans.yml");
    }

    /**
     * Logic for banning a player.
     * Synchronized with TempBanCommand calls.
     */
    public void ban(UUID uuid, String targetName, long duration, String reason, String staff, boolean silent) {
        long expiry = (duration == -1) ? -1 : System.currentTimeMillis() + duration;
        bans.put(uuid, new BanEntry(targetName, expiry, reason, staff));
        saveBans();
    }

    /**
     * Removes a ban.
     * Synchronized with UnbanCommand calls.
     */
    public void unban(UUID uuid) {
        bans.remove(uuid);
        saveBans();
    }

    public boolean isBanned(UUID uuid) {
        if (!bans.containsKey(uuid)) return false;

        BanEntry entry = bans.get(uuid);
        if (entry.expiry() != -1 && System.currentTimeMillis() > entry.expiry()) {
            bans.remove(uuid);
            saveBans();
            return false;
        }
        return true;
    }

    public BanEntry getBan(UUID uuid) {
        return isBanned(uuid) ? bans.get(uuid) : null;
    }

    // FIXED: Renamed to match BanSystem.java line 37
    public void loadBans() {
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String name = cfg.getString(key + ".name");
            long expiry = cfg.getLong(key + ".expiry");
            String reason = cfg.getString(key + ".reason");
            String staff = cfg.getString(key + ".staff");
            bans.put(uuid, new BanEntry(name, expiry, reason, staff));
        }
    }

    // FIXED: Renamed to match BanSystem.java line 69
    public void saveBans() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, BanEntry> entry : bans.entrySet()) {
            String path = entry.getKey().toString();
            BanEntry data = entry.getValue();
            cfg.set(path + ".name", data.name());
            cfg.set(path + ".expiry", data.expiry());
            cfg.set(path + ".reason", data.reason());
            cfg.set(path + ".staff", data.staff());
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save bans.yml!");
        }
    }

    // Record class for clean data handling
    public record BanEntry(String name, long expiry, String reason, String staff) {}
}