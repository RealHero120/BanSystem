package me.hero.bansystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.hero.bansystem.Bansystem;
import me.hero.bansystem.util.TimeUtil;

public class TempBanCommand implements CommandExecutor {
    private final Bansystem plugin;

    public TempBanCommand(Bansystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("bansystem.tempban")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /tempban <player> <duration> [reason]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        long duration = TimeUtil.parseDuration(args[1]);

        if (duration == -2) {
            plugin.getMessageUtil().send(sender, "invalid-duration");
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().isEmpty() ? "No reason provided" : reasonBuilder.toString().trim();

        plugin.getBanManager().ban(target.getUniqueId(), target.getName(), duration, reason, sender.getName());

        if (target.isOnline() && target.getPlayer() != null) {
            String timeLeft = (duration == -1) ? "Permanent" : TimeUtil.formatDuration(duration);
            target.getPlayer().kickPlayer("§cYou have been banned!\n§7Reason: §f" + reason + "\n§7Expires: §f" + timeLeft);
        }

        plugin.getMessageUtil().send(sender, duration == -1 ? "tempban-success-perm" : "tempban-success",
                "target", target.getName(), "duration", TimeUtil.formatDuration(duration));

        return true;
    }
}