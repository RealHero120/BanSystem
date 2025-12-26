package me.hero.bansystem.commands;

import me.hero.bansystem.Bansystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SecureOpCommand implements CommandExecutor {
    private final Bansystem plugin;

    public SecureOpCommand(Bansystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("secureop.reload")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getSecureOpManager().loadAllowedLists();
            sender.sendMessage("§a[SecureOP] Config and allowed lists reloaded!");
            return true;
        }

        sender.sendMessage("§eUsage: /secureop reload");
        return true;
    }
}