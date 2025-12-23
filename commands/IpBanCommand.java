package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IpBanCommand implements CommandExecutor {

    private final BanSystem plugin;

    public IpBanCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bansystem.ipban")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /ipban <player/IP>");
            return true;
        }

        String input = args[0];
        String ip;

        // Check if input is a player name or an IP directly
        Player target = Bukkit.getPlayer(input);
        if (target != null) {
            ip = target.getAddress().getAddress().getHostAddress();
        } else {
            // Assume input is a raw IP address
            ip = input;
        }

        plugin.getIpBanManager().ipBan(ip);

        // Kick the player if they are online
        if (target != null) {
            target.kickPlayer("§cYour IP has been permanently banned.");
        }

        plugin.getMessages().send(sender, "ipban-success", "target", input);
        return true;
    }
}