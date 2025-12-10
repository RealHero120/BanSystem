package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.BanManager;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("bansystem.unban")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length < 1) {
            plugin.getMessages().send(sender, "usage-unban");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String name = target.getName() != null ? target.getName() : args[0];

        BanManager banManager = plugin.getBanManager();
        if (!banManager.isBanned(target.getUniqueId())) {
            plugin.getMessages().send(sender, "unban-not-banned", "target", name);
            return true;
        }

        banManager.unban(target.getUniqueId());
        plugin.getMessages().send(sender, "unban-success", "target", name);

        return true;
    }
}
