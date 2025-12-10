package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            plugin.getMessages().send(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("bansystem.seeinv")) {
            plugin.getMessages().send(player, "no-permission");
            return true;
        }

        if (args.length != 1) {
            plugin.getMessages().send(player, "seeinv-usage", "label", label);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            plugin.getMessages().send(player, "player-not-found", "target", args[0]);
            return true;
        }

        player.openInventory(target.getInventory());
        plugin.getMessages().send(player, "seeinv-opened", "target", target.getName());

        return true;
    }
}
