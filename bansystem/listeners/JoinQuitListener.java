package me.hero.bansystem.listeners;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class JoinQuitListener implements Listener {

    private final BanSystem plugin;

    public JoinQuitListener(BanSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        VanishManager vanishManager = plugin.getVanishManager();

        // Hide all currently vanished players from this joining player
        for (UUID uuid : vanishManager.getVanished()) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished != null && vanished.isOnline()
                    && !player.hasPermission("bansystem.vanish.see")) {
                player.hidePlayer(plugin, vanished);
            }
        }

        // If the joiner themself is vanished, reapply it & hide join message
        if (vanishManager.isVanished(player.getUniqueId())) {
            vanishManager.setVanished(player, true);
            event.joinMessage(null);
            plugin.getMessages().send(player, "vanish-login");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        VanishManager vanishManager = plugin.getVanishManager();

        if (vanishManager.isVanished(player.getUniqueId())) {
            event.quitMessage(null);
        }
    }
}
