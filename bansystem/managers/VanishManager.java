package me.hero.bansystem.managers;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishManager implements Listener {

    private final BanSystem plugin;
    private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();

    private File file;
    private YamlConfiguration config;

    public VanishManager(BanSystem plugin) {
        this.plugin = plugin;
        setupFile();
        loadVanishes();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupFile() {
        file = new File(plugin.getDataFolder(), "vanish.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create vanish.yml!");
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveVanishes() {
        config.set("vanished", null);
        for (UUID uuid : vanished) {
            config.set("vanished." + uuid, true);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save vanish.yml!");
                e.printStackTrace();
            }
        });
    }

    public void loadVanishes() {
        vanished.clear();
        if (!config.isConfigurationSection("vanished")) {
            return;
        }

        for (String key : config.getConfigurationSection("vanished").getKeys(false)) {
            try {
                vanished.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public boolean isVanished(UUID uuid) {
        return vanished.contains(uuid);
    }

    public void setVanished(Player player, boolean vanish) {
        UUID uuid = player.getUniqueId();

        if (vanish) {
            vanished.add(uuid);
        } else {
            vanished.remove(uuid);
        }

        // Update visibility for all online players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (vanish) {
                if (!online.hasPermission("bansystem.vanish.see")) {
                    online.hidePlayer(plugin, player);
                }
            } else {
                online.showPlayer(plugin, player);
            }
        }

        saveVanishes();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player joining = e.getPlayer();

        for (UUID id : vanished) {
            Player vanishedPlayer = Bukkit.getPlayer(id);
            if (vanishedPlayer != null && !joining.hasPermission("bansystem.vanish.see")) {
                joining.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    public Set<UUID> getVanished() {
        return vanished;
    }
}
