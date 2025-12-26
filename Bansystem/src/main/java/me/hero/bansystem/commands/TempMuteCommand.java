package me.hero.bansystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.hero.bansystem.Bansystem;
import me.hero.bansystem.util.TimeUtil;

public class TempMuteCommand implements CommandExecutor {
    private final Bansystem plugin;

    public TempMuteCommand(Bansystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("bansystem.tempmute")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /tempmute <player> <duration> [reason]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        long duration = TimeUtil.parseDuration(args[1]);

        if (duration == -2) {
            plugin.getMessageUtil().send(sender, "invalid-duration");
            return true;
        }

        String reason = (args.length > 2) ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "No reason provided";

        plugin.getMuteManager().mute(target.getUniqueId(), target.getName(), duration, reason, sender.getName());
        plugin.getMessageUtil().send(sender, "tempmute-success", "target", target.getName(), "duration", TimeUtil.formatDuration(duration));
        return true;
    }
}