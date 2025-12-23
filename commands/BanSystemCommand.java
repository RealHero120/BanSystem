package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BanSystemCommand implements CommandExecutor {

    private final BanSystem plugin;

    public BanSystemCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            plugin.getMessages().send(sender, "bansystem-help-header");
            plugin.getMessages().send(sender, "bansystem-help-reload", "label", label);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bansystem.reload")) {
                plugin.getMessages().send(sender, "no-permission");
                return true;
            }

            plugin.reloadConfig();
            plugin.reloadMessages();
            plugin.getMessages().send(sender, "reload-success");
            return true;
        }

        plugin.getMessages().send(sender, "bansystem-unknown-sub", "label", label);
        return true;
    }
}
