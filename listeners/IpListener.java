package me.hero.bansystem.listeners;

import me.hero.bansystem.BanSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import java.net.InetAddress;

public class IpListener implements Listener {

    private final BanSystem plugin;

    public IpListener(BanSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        // Check if IP bans are actually enabled in config
        if (!plugin.getConfig().getBoolean("features.ip-bans-enabled", false)) return;

        String ip = event.getAddress().getHostAddress();
        if (plugin.getIpBanManager().isIpBanned(ip)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§cYour IP is permanently banned from this server.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = player.getAddress().getAddress().getHostAddress();

        // Log the IP for /alts tracking
        plugin.getIpBanManager().logJoin(player.getUniqueId(), player.getName(), ip);
    }
}