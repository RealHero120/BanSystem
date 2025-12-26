package me.hero.bansystem.listeners;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.managers.BanManager;
import me.hero.bansystem.util.TimeUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import net.kyori.adventure.text.Component;
import java.util.List;

public class BanListener implements Listener {
    private final Bansystem plugin;

    public BanListener(Bansystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String ip = event.getAddress().getHostAddress();
        if (plugin.getIpBanManager().isIpBanned(ip)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Component.text("§cYour IP is permanently banned."));
            return;
        }

        if (plugin.getBanManager().isBanned(event.getUniqueId())) {
            BanManager.BanEntry entry = plugin.getBanManager().getBan(event.getUniqueId());
            String timeLeft = (entry.expiry() == -1) ? "Permanent" : TimeUtil.formatDuration(entry.expiry() - System.currentTimeMillis());

            List<String> lines = plugin.getMessageUtil().getList(entry.expiry() == -1 ? "ban-screen-perm" : "ban-screen",
                    "reason", entry.reason(), "duration", timeLeft, "staff", entry.staff());

            StringBuilder kickMessage = new StringBuilder();
            lines.forEach(line -> kickMessage.append(line).append("\n"));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Component.text(kickMessage.toString().trim()));
        }
    }
}