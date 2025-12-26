package me.hero.bansystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.hero.bansystem.Bansystem;

public class IpBanCommand implements CommandExecutor {
    private final Bansystem plugin;

    public IpBanCommand(Bansystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("bansystem.ipban")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /ipban <player|ip>");
            return true;
        }

        String targetIp;
        Player onlineTarget = Bukkit.getPlayer(args[0]);

        if (onlineTarget != null) {
            targetIp = onlineTarget.getAddress().getAddress().getHostAddress();
        } else {
            targetIp = args[0];
        }

        plugin.getIpBanManager().ipBan(targetIp);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getAddress().getAddress().getHostAddress().equals(targetIp)) {
                p.kickPlayer("§cYour IP address has been permanently banned from this server.");
            }
        }

        sender.sendMessage("§aSuccessfully IP-banned: §f" + targetIp);
        return true;
    }
}