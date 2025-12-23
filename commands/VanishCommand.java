package me.hero.bansystem.commands;

import me.hero.bansystem.managers.VanishManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {

    private final VanishManager vanishManager;

    public VanishCommand(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        // Standardized to bansystem permission
        if (!player.hasPermission("bansystem.vanish")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        boolean newState = !vanishManager.isVanished(player.getUniqueId());
        vanishManager.setVanished(player, newState);

        player.sendMessage(ChatColor.GRAY + "Vanish " + (newState ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
        return true;
    }
}