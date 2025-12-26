package me.hero.bansystem.listeners;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.managers.MuteManager;
import me.hero.bansystem.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    private final Bansystem plugin;

    public ChatListener(Bansystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        MuteManager muteManager = plugin.getMuteManager();
        if (muteManager == null) return;

        if (muteManager.isMuted(id)) {
            event.setCancelled(true);

            MuteManager.MuteEntry entry = muteManager.getMute(id);
            String timeLeft = "Permanent";

            if (entry != null) {
                long expiry = entry.expiryMillis();
                if (expiry != -1L) {
                    long remaining = expiry - System.currentTimeMillis();
                    timeLeft = TimeUtil.formatDuration(Math.max(0L, remaining));
                }
            }

            final String finalTimeLeft = timeLeft; // make effectively final for the lambda
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) {
                    plugin.getMessageUtil().send(p, "mute-chat-warning", "duration", finalTimeLeft);
                }
            });
        }
    }
}
