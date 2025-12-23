package me.hero.bansystem.managers;

import me.hero.bansystem.BanSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteManager {

    private final BanSystem plugin;
    private final Map<UUID, MuteEntry> mutes = new HashMap<>();
    private final File file;

    public MuteManager(BanSystem plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mutes.yml");
    }

    /**
     * Logic for muting a player.
     * Synchronized with TempMuteCommand calls.
     */
    public void mute(UUID uuid, String targetName, long duration, String reason, String staff) {
        long expiry = (duration == -1) ? -1 : System.currentTimeMillis() + duration;
        mutes.put(uuid, new MuteEntry(targetName, expiry, reason, staff));
        saveMutes();
    }

    /**
     * Removes a mute.
     * Synchronized with UnmuteCommand calls.
     */
    public void unmute(UUID uuid) {
        mutes.remove(uuid);
        saveMutes();
    }

    public boolean isMuted(UUID uuid) {
        if (!mutes.containsKey(uuid)) return false;

        MuteEntry entry = mutes.get(uuid);
        if (entry.expiry() != -1 && System.currentTimeMillis() > entry.expiry()) {
            mutes.remove(uuid);
            saveMutes();
            return false;
        }
        return true;
    }

    /**
     * Retrieves mute data for the History command.
     */
    public MuteEntry getMute(UUID uuid) {
        return isMuted(uuid) ? mutes.get(uuid) : null;
    }

    /**
     * FIXED: Renamed to match BanSystem.java line 38
     */
    public void loadMutes() {
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String name = cfg.getString(key + ".name");
                long expiry = cfg.getLong(key + ".expiry");
                String reason = cfg.getString(key + ".reason");
                String staff = cfg.getString(key + ".staff");
                mutes.put(uuid, new MuteEntry(name, expiry, reason, staff));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    /**
     * FIXED: Renamed to match BanSystem.java line 70
     */
    public void saveMutes() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, MuteEntry> entry : mutes.entrySet()) {
            String path = entry.getKey().toString();
            MuteEntry data = entry.getValue();
            cfg.set(path + ".name", data.name());
            cfg.set(path + ".expiry", data.expiry());
            cfg.set(path + ".reason", data.reason());
            cfg.set(path + ".staff", data.staff());
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mutes.yml!");
        }
    }

    // Record class for clean data handling, matches BanEntry style
    public record MuteEntry(String name, long expiry, String reason, String staff) {}
}