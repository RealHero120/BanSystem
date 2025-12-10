package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.MuteManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class UnmuteCommand implements CommandExecutor {

    private final BanSystem plugin;

    public UnmuteCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("bansystem.unmute")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length != 1) {
            plugin.getMessages().send(sender, "unmute-usage", "label", label);
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID uuid = target.getUniqueId();
        if (uuid == null) {
            plugin.getMessages().send(sender, "player-not-found", "target", targetName);
            return true;
        }

        MuteManager muteManager = plugin.getMuteManager();

        if (!muteManager.isMuted(uuid)) {
            plugin.getMessages().send(sender, "unmute-not-muted", "target", targetName);
            return true;
        }

        muteManager.unmute(uuid);
        plugin.getMessages().send(sender, "unmute-success",
                "target", targetName,
                "staff", sender.getName()
        );

        if (target.isOnline() && target.getPlayer() != null) {
            plugin.getMessages().send(target.getPlayer(), "unmute-notify");
        }

        return true;
    }
}
