package me.hero.bansystem.listeners;

import me.hero.bansystem.Bansystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinQuitListener implements Listener {

    private final Bansystem plugin;

    public JoinQuitListener(Bansystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        int altCount = plugin.getIpBanManager().getAlts(player.getAddress().getAddress().getHostAddress()).size();
        if (altCount > 1) {
            String msg = "§8[§4Staff§8] §f" + player.getName() + " §7joined with §f" + altCount + " §7known accounts.";
            Bukkit.broadcast(msg, "bansystem.notify");
        }
    }
}