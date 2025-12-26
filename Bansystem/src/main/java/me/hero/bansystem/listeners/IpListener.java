package me.hero.bansystem.listeners;

import me.hero.bansystem.Bansystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class IpListener implements Listener {

    private final Bansystem plugin;

    public IpListener(Bansystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        plugin.getIpBanManager().logJoin(event.getPlayer().getUniqueId(), ip);
    }
}