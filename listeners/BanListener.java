package me.hero.bansystem.listeners;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.BanManager; // Standardized Manager import
import me.hero.bansystem.util.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class BanListener implements Listener {

    private final BanSystem plugin;

    public BanListener(BanSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        BanManager banManager = plugin.getBanManager();

        // Check if the player is currently banned
        if (!banManager.isBanned(player.getUniqueId())) {
            return;
        }

        // Fetch the standardized BanEntry (replaces old BanRecord)
        BanManager.BanEntry entry = banManager.getBan(player.getUniqueId());
        if (entry == null) {
            return;
        }

        // Calculate time remaining
        long remaining = (entry.expiry() == -1) ? -1L : entry.expiry() - System.currentTimeMillis();
        boolean permanent = remaining == -1L;
        String durationText = permanent ? "Permanent" : TimeUtil.formatDuration(remaining);

        // Select the correct message key from messages.yml
        String key = permanent ? "ban-screen-perm" : "ban-screen";

        // Construct the kick message using your MessageUtil
        Component kickMessage = plugin.getMessages().componentFromList(
                key,
                "reason", entry.reason(),
                "duration", durationText,
                "staff", entry.staff()
        );

        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMessage);
    }
}