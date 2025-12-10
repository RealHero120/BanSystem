package me.hero.bansystem.managers;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MuteManager {

    private final BanSystem plugin;
    private final Map<UUID, Long> mutedPlayers = new ConcurrentHashMap<>();

    private File file;
    private FileConfiguration config;

    public MuteManager(BanSystem plugin) {
        this.plugin = plugin;
        createFile();
    }

    private void createFile() {
        file = new File(plugin.getDataFolder(), "mutes.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create mutes.yml!");
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void loadMutes() {
        mutedPlayers.clear();
        if (!config.isConfigurationSection("mutes")) {
            return;
        }

        final long now = System.currentTimeMillis();
        for (String key : config.getConfigurationSection("mutes").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            long until = config.getLong("mutes." + key);

            if (until > now) {
                mutedPlayers.put(uuid, until);
            }
        }
    }

    public void saveMutes() {
        final long now = System.currentTimeMillis();
        config.set("mutes", null);

        for (Map.Entry<UUID, Long> entry : mutedPlayers.entrySet()) {
            long until = entry.getValue();
            if (until > now) {
                config.set("mutes." + entry.getKey(), until);
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save mutes.yml!");
                e.printStackTrace();
            }
        });
    }

    public void mute(UUID uuid, long durationMillis) {
        long until = System.currentTimeMillis() + durationMillis;
        mutedPlayers.put(uuid, until);
        saveMutes();
    }

    public void unmute(UUID uuid) {
        mutedPlayers.remove(uuid);
        saveMutes();
    }

    public boolean isMuted(UUID uuid) {
        Long until = mutedPlayers.get(uuid);
        if (until == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now > until) {
            mutedPlayers.remove(uuid);
            saveMutes();
            return false;
        }
        return true;
    }

    public long getRemaining(UUID uuid) {
        Long until = mutedPlayers.get(uuid);
        if (until == null) {
            return 0L;
        }

        long diff = until - System.currentTimeMillis();
        return Math.max(diff, 0L);
    }
}
