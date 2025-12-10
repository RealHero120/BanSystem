package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.IpBanManager;
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

        if (!plugin.isFeatureEnabled("ip-bans-enabled")) {
            plugin.getMessages().send(sender, "ipban-disabled");
            return true;
        }

        if (args.length != 1) {
            plugin.getMessages().send(sender, "unipban-usage", "label", label);
            return true;
        }

        String ip = args[0];
        IpBanManager manager = plugin.getIpBanManager();

        if (!manager.isIpBanned(ip)) {
            plugin.getMessages().send(sender, "unipban-not-banned", "ip", ip);
            return true;
        }

        manager.unbanIp(ip);
        plugin.getMessages().send(sender, "unipban-success",
                "ip", ip,
                "staff", sender.getName()
        );

        return true;
    }
}
