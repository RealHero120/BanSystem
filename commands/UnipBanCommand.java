package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnipBanCommand implements CommandExecutor {

    private final BanSystem plugin;

    public UnipBanCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bansystem.unipban")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /unipban <IP>");
            return true;
        }

        String ip = args[0];

        // FIXED: Matches the method name in IpBanManager
        plugin.getIpBanManager().unIpBan(ip);

        plugin.getMessages().send(sender, "unipban-success", "ip", ip);
        return true;
    }
}