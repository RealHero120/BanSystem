package me.hero.bansystem.listeners;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.TimeUtil;
import me.hero.bansystem.managers.BanManager;
import me.hero.bansystem.managers.BanManager.BanRecord;
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

        if (!banManager.isBanned(player.getUniqueId())) {
            return;
        }

        BanRecord record = banManager.getRecord(player.getUniqueId());
        if (record == null) {
            return;
        }

        long remaining = banManager.getRemaining(player.getUniqueId());
        boolean permanent = remaining == -1L || record.getUntil() < 0;
        String durationText = permanent ? "Permanent" : TimeUtil.formatDuration(remaining);

        String key = permanent ? "ban-screen-perm" : "ban-screen";

        Component kickMessage = plugin.getMessages().componentFromList(
                key,
                "reason", record.getReason(),
                "duration", durationText,
                "staff", record.getStaff()
        );

        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMessage);
    }
}
