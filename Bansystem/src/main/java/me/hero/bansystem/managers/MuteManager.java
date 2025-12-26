// java
package me.hero.bansystem.managers;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.events.PlayerMuteEvent;
import me.hero.bansystem.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages player mutes. Persists to mutes.yml and fires PlayerMuteEvent when a mute is applied.
 */
public class MuteManager {

    private final Bansystem plugin;
    private final Map<UUID, MuteEntry> mutes = new HashMap<>();
    private final File file;

    public record MuteEntry(long expiryMillis, String reason, String staff) {}

    public MuteManager(Bansystem plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mutes.yml");
    }

    public synchronized void loadMutes() {
        mutes.clear();
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("mutes");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long expiry = cfg.getLong("mutes." + key + ".expiry", -1L);
                String reason = cfg.getString("mutes." + key + ".reason", "No reason provided");
                String staff = cfg.getString("mutes." + key + ".staff", "Console");
                mutes.put(uuid, new MuteEntry(expiry, reason, staff));
            } catch (IllegalArgumentException ignored) {
                // skip invalid UUID keys
            }
        }
    }

    public synchronized void saveMutes() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, MuteEntry> entry : mutes.entrySet()) {
            String path = "mutes." + entry.getKey().toString();
            cfg.set(path + ".expiry", entry.getValue().expiryMillis());
            cfg.set(path + ".reason", entry.getValue().reason());
            cfg.set(path + ".staff", entry.getValue().staff());
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mutes.yml: " + e.getMessage());
        }
    }

    /**
     * Apply a mute.
     * @param uuid target UUID
     * @param targetName displayed target name
     * @param durationMillis duration in milliseconds, use -1 for permanent
     * @param reason reason text
     * @param staff staff name
     */
    public synchronized void mute(UUID uuid, String targetName, long durationMillis, String reason, String staff) {
        long expiry = (durationMillis == -1) ? -1L : System.currentTimeMillis() + durationMillis;
        mutes.put(uuid, new MuteEntry(expiry, reason, staff));
        saveMutes();

        String readableDuration = (durationMillis == -1) ? "Permanent" : TimeUtil.formatDuration(durationMillis);
        Bukkit.getPluginManager().callEvent(new PlayerMuteEvent(targetName, staff, reason, readableDuration));
    }

    public synchronized void unmute(UUID uuid) {
        if (mutes.remove(uuid) != null) saveMutes();
    }

    /**
     * Returns true if the UUID is currently muted (and not expired). Expired mutes are removed automatically.
     */
    public synchronized boolean isMuted(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        if (entry == null) return false;
        long expiry = entry.expiryMillis();
        if (expiry != -1L && expiry < System.currentTimeMillis()) {
            mutes.remove(uuid);
            saveMutes();
            return false;
        }
        return true;
    }

    public synchronized MuteEntry getMute(UUID uuid) {
        return mutes.get(uuid);
    }

    public synchronized Map<UUID, MuteEntry> getAllMutes() {
        return Collections.unmodifiableMap(new HashMap<>(mutes));
    }
}
