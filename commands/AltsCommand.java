package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class AltsCommand implements CommandExecutor {

    private final BanSystem plugin;

    public AltsCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission from plugin.yml
        if (!sender.hasPermission("bansystem.alts")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /alts <player>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        // Use the player's current IP if online, otherwise we'd need a database of last-known IPs
        String ip = null;
        if (target.isOnline() && target.getPlayer() != null) {
            ip = target.getPlayer().getAddress().getAddress().getHostAddress();
        }

        if (ip == null) {
            sender.sendMessage("§cCould not retrieve IP for " + targetName + ". Player must be online or have joined recently.");
            return true;
        }

        // Fetch alts from the updated IpBanManager
        Set<UUID> alts = plugin.getIpBanManager().getAlts(ip);

        if (alts.isEmpty() || (alts.size() == 1 && alts.contains(target.getUniqueId()))) {
            plugin.getMessages().send(sender, "alts-none");
            return true;
        }

        // Send header from messages.yml
        plugin.getMessages().send(sender, "alts-header", "target", targetName);

        for (UUID altUuid : alts) {
            OfflinePlayer altPlayer = Bukkit.getOfflinePlayer(altUuid);
            String name = altPlayer.getName() != null ? altPlayer.getName() : altUuid.toString();

            // Format each entry using the message key
            plugin.getMessages().send(sender, "alts-entry", "account", name);
        }

        return true;
    }
}