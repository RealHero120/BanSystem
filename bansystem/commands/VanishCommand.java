package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {

    private final BanSystem plugin;

    public VanishCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            plugin.getMessages().send(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("bansystem.vanish")) {
            plugin.getMessages().send(player, "no-permission");
            return true;
        }

        Player target;
        boolean self = (args.length == 0);

        if (self) {
            target = player;
        } else {
            if (!player.hasPermission("bansystem.vanish.others")) {
                plugin.getMessages().send(player, "no-permission");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null || !target.isOnline()) {
                plugin.getMessages().send(player, "player-not-found", "target", args[0]);
                return true;
            }
        }

        VanishManager vanishManager = plugin.getVanishManager();
        boolean currently = vanishManager.isVanished(target.getUniqueId());
        boolean newState = !currently;

        vanishManager.setVanished(target, newState);

        if (self) {
            plugin.getMessages().send(player,
                    newState ? "vanish-enabled" : "vanish-disabled");
        } else {
            plugin.getMessages().send(player,
                    newState ? "vanish-enabled-other" : "vanish-disabled-other",
                    "target", target.getName()
            );
            if (!target.equals(player)) {
                plugin.getMessages().send(target,
                        newState ? "vanish-notify-on" : "vanish-notify-off",
                        "staff", player.getName()
                );
            }
        }

        return true;
    }
}
