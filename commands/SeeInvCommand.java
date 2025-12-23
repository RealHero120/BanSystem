package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SeeInvCommand implements CommandExecutor {

    private final BanSystem plugin;

    public SeeInvCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (!player.hasPermission("bansystem.seeinv")) {
            plugin.getMessages().send(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /seeinv <player> [echest]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        // Check if the staff member specifically asked for the Ender Chest
        if (args.length > 1 && args[1].equalsIgnoreCase("echest")) {
            player.openInventory(target.getEnderChest());
            player.sendMessage(ChatColor.GRAY + "Opening " + ChatColor.LIGHT_PURPLE + target.getName() + "'s Ender Chest.");
        } else {
            // Default to regular inventory
            player.openInventory(target.getInventory());
            player.sendMessage(ChatColor.GRAY + "Opening " + ChatColor.GREEN + target.getName() + "'s Inventory.");
        }

        return true;
    }
}