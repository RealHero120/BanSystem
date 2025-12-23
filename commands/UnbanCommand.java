package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnbanCommand implements CommandExecutor {
    private final BanSystem plugin;

    public UnbanCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bansystem.tempban")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /unban <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!plugin.getBanManager().isBanned(target.getUniqueId())) {
            sender.sendMessage("§cThat player is not banned.");
            return true;
        }

        plugin.getBanManager().unban(target.getUniqueId());
        sender.sendMessage("§aSuccessfully unbanned §f" + target.getName());
        return true;
    }
}