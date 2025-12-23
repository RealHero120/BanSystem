package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class UnmuteCommand implements CommandExecutor {

    private final BanSystem plugin;

    public UnmuteCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bansystem.unmute")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /unmute <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = target.getUniqueId();

        if (!plugin.getMuteManager().isMuted(uuid)) {
            plugin.getMessages().send(sender, "unmute-failed", "target", args[0]);
            return true;
        }

        plugin.getMuteManager().unmute(uuid);
        plugin.getMessages().send(sender, "unmute-success", "target", args[0]);
        return true;
    }
}