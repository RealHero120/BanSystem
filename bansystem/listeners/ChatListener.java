package me.hero.bansystem.listeners;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.TimeUtil;
import me.hero.bansystem.managers.MuteManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final BanSystem plugin;

    public ChatListener(BanSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        MuteManager muteManager = plugin.getMuteManager();

        if (!muteManager.isMuted(player.getUniqueId())) {
            return;
        }

        long remainingMs = muteManager.getRemaining(player.getUniqueId());
        long remainingSeconds = remainingMs / 1000L;

        event.setCancelled(true);

        plugin.getMessages().send(player, "mute-active",
                "seconds", String.valueOf(remainingSeconds),
                "duration", TimeUtil.formatDuration(remainingMs)
        );
    }
}
