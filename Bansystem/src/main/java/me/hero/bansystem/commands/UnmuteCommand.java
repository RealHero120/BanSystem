package me.hero.bansystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.hero.bansystem.Bansystem;

public class UnmuteCommand implements CommandExecutor {
    private final Bansystem plugin;

    public UnmuteCommand(Bansystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("bansystem.unmute")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unmute <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!plugin.getMuteManager().isMuted(target.getUniqueId())) {
            plugin.getMessageUtil().send(sender, "unmute-failed", "target", target.getName());
            return true;
        }

        plugin.getMuteManager().unmute(target.getUniqueId());
        plugin.getMessageUtil().send(sender, "unmute-success", "target", target.getName());
        return true;
    }
}