package me.hero.bansystem.listeners;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.MuteManager;
import me.hero.bansystem.util.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final BanSystem plugin;

    public ChatListener(BanSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Check with the updated MuteManager
        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
            event.setCancelled(true);

            MuteManager.MuteEntry entry = plugin.getMuteManager().getMute(player.getUniqueId());
            if (entry == null) return;

            long remaining = (entry.expiry() == -1) ? -1 : entry.expiry() - System.currentTimeMillis();
            String durationText = (remaining == -1) ? "Permanent" : TimeUtil.formatDuration(remaining);

            // Send the mute message from messages.yml
            plugin.getMessages().send(player, "mute-message",
                    "reason", entry.reason(),
                    "duration", durationText);
        }
    }
}