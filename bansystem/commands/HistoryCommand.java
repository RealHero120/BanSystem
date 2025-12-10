package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.BanManager;
import me.hero.bansystem.managers.BanManager.BanHistoryEntry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class HistoryCommand implements CommandExecutor {

    private final BanSystem plugin;

    public HistoryCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("bansystem.history")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length != 1) {
            plugin.getMessages().send(sender, "history-usage", "label", label);
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID uuid = target.getUniqueId();
        if (uuid == null) {
            plugin.getMessages().send(sender, "player-not-found", "target", targetName);
            return true;
        }

        BanManager banManager = plugin.getBanManager();
        List<BanHistoryEntry> history = banManager.getHistory(uuid);

        if (history.isEmpty()) {
            plugin.getMessages().send(sender, "history-none", "target", targetName);
            return true;
        }

        plugin.getMessages().send(sender, "history-header", "target", targetName);

        int index = 1;
        for (BanHistoryEntry entry : history) {
            String durationText = entry.isPermanent()
                    ? "Permanent"
                    : me.hero.bansystem.TimeUtil.formatDuration(entry.getDurationMillis());

            plugin.getMessages().send(sender, "history-line",
                    "index", String.valueOf(index),
                    "date", entry.formatDate(),
                    "reason", entry.getReason(),
                    "staff", entry.getStaff(),
                    "duration", durationText,
                    "silent", entry.isSilent() ? "yes" : "no"
            );
            index++;
        }

        return true;
    }
}
