package me.hero.bansystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.hero.bansystem.Bansystem;

public class UnipBanCommand implements CommandExecutor {
    private final Bansystem plugin;

    public UnipBanCommand(Bansystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("bansystem.unipban")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unipban <ip>");
            return true;
        }

        String ip = args[0];
        plugin.getIpBanManager().unIpBan(ip);
        sender.sendMessage("§aSuccessfully unbanned IP: §f" + ip);
        return true;
    }
}