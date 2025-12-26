package me.hero.bansystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.hero.bansystem.Bansystem;

import java.util.Set;
import java.util.UUID;

public class AltsCommand implements CommandExecutor {
    private final Bansystem plugin;

    public AltsCommand(Bansystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("bansystem.alts")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /alts <player|ip>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        String ip = (target != null) ? target.getAddress().getAddress().getHostAddress() : args[0];

        Set<UUID> alts = plugin.getIpBanManager().getAlts(ip);

        if (alts.isEmpty()) {
            sender.sendMessage("§cNo data found for: §f" + args[0]);
            return true;
        }

        sender.sendMessage("§8§m---------------------------------");
        sender.sendMessage("§6Known accounts for IP: §f" + ip);
        for (UUID uuid : alts) {
            OfflinePlayer alt = Bukkit.getOfflinePlayer(uuid);
            sender.sendMessage(" §8- §7" + (alt.getName() != null ? alt.getName() : uuid.toString()));
        }
        sender.sendMessage("§8§m---------------------------------");

        return true;
    }
}