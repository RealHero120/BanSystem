package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.IpBanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class AltsCommand implements CommandExecutor {

    // Simple IPv4 pattern; cheap and compiled once
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d?\\d)(\\.|$)){4}$"
    );

    private final BanSystem plugin;

    public AltsCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Permission check
        if (!sender.hasPermission("bansystem.alts")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        // Usage: /alts <player|ip>
        if (args.length != 1) {
            plugin.getMessages().send(sender, "alts-usage", "label", label);
            return true;
        }

        String key = args[0];
        IpBanManager ipBanManager = plugin.getIpBanManager();
        String ip = null;

        // Case 1: argument is already an IP
        if (IP_PATTERN.matcher(key).matches()) {
            ip = key;
        } else {
            // Case 2: argument is a player name/UUID
            OfflinePlayer target = Bukkit.getOfflinePlayer(key);

            // Try live session IP first if they are online
            if (target.isOnline() && target.getPlayer() != null) {
                InetSocketAddress addr = target.getPlayer().getAddress();
                if (addr != null && addr.getAddress() != null) {
                    ip = addr.getAddress().getHostAddress();
                }
            }

            // Fall back to last known IP from our manager
            if (ip == null) {
                ip = ipBanManager.getLastKnownIp(target.getUniqueId());
            }
        }

        // Still no IP? We can't look up alts
        if (ip == null || ip.isEmpty()) {
            plugin.getMessages().send(sender, "ipban-no-ip", "target", key);
            return true;
        }

        // Grab accounts mapped to this IP
        Map<UUID, String> accounts = ipBanManager.getAccountsForIp(ip);

        if (accounts == null || accounts.isEmpty()) {
            plugin.getMessages().send(sender, "alt-none", "key", key);
            return true;
        }

        // Header
        plugin.getMessages().send(sender, "alt-header", "ip", ip);

        // Lines
        for (Map.Entry<UUID, String> entry : accounts.entrySet()) {
            plugin.getMessages().send(sender, "alt-line",
                    "name", entry.getValue(),
                    "uuid", entry.getKey().toString()
            );
        }

        return true;
    }
}
