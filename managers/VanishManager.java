package me.hero.bansystem.managers;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager implements Listener {

    private final BanSystem plugin;
    private final Set<UUID> vanished = new HashSet<>();
    private final File file;

    public VanishManager(BanSystem plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "vanish.yml");
    }

    public boolean isVanished(UUID uuid) {
        return vanished.contains(uuid);
    }

    public Set<UUID> getVanished() {
        return vanished;
    }

    public void setVanished(Player player, boolean state) {
        UUID uuid = player.getUniqueId();
        if (state) {
            vanished.add(uuid);
            // Hide from everyone who doesn't have the see permission
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("bansystem.vanish.see") && !online.equals(player)) {
                    online.hidePlayer(plugin, player);
                }
            }
            player.setAllowFlight(true);
        } else {
            vanished.remove(uuid);
            // Show to everyone
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }
        }
        saveVanishes();
    }

    // Moved from the old VanishListener to keep everything in one place
    @EventHandler
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (isVanished(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    public void loadVanishes() {
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            if (cfg.getBoolean(key)) vanished.add(UUID.fromString(key));
        }
    }

    public void saveVanishes() {
        final Set<UUID> snapshot = new HashSet<>(vanished);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            YamlConfiguration out = new YamlConfiguration();
            for (UUID uuid : snapshot) out.set(uuid.toString(), true);
            try {
                out.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save vanish.yml");
            }
        });
    }
}