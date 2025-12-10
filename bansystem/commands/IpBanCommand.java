package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.TimeUtil;
import me.hero.bansystem.managers.IpBanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.regex.Pattern;

public class IpBanCommand implements CommandExecutor {

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d?\\d)(\\.|$)){4}$"
    );

    private final BanSystem plugin;

    public IpBanCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("bansystem.ipban")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (!plugin.isFeatureEnabled("ip-bans-enabled")) {
            plugin.getMessages().send(sender, "ipban-disabled");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessages().send(sender, "ipban-usage", "label", label);
            return true;
        }

        String key = args[0];
        String durationArg = args[1];
        String ip = null;

        // resolve IP
        if (IP_PATTERN.matcher(key).matches()) {
            ip = key;
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(key);
            UUID uuid = target.getUniqueId();

            if (uuid != null && target.isOnline() && target.getPlayer() != null) {
                InetSocketAddress addr = target.getPlayer().getAddress();
                if (addr != null && addr.getAddress() != null) {
                    ip = addr.getAddress().getHostAddress();
                }
            }

            if (ip == null && uuid != null) {
                ip = plugin.getIpBanManager().getLastKnownIp(uuid);
            }

            if (ip == null) {
                plugin.getMessages().send(sender, "ipban-no-ip", "target", key);
                return true;
            }
        }

        // duration + permanent flag
        final boolean permanent;
        final long durationMillis;

        if (durationArg.equalsIgnoreCase("perm") || durationArg.equalsIgnoreCase("permanent")) {
            permanent = true;
            durationMillis = -1L;
        } else {
            permanent = false;
            long parsed = TimeUtil.parseDuration(durationArg);
            if (parsed <= 0) {
                plugin.getMessages().send(sender, "invalid-duration", "input", durationArg);
                return true;
            }
            durationMillis = parsed;
        }

        // reason + silent
        boolean silent = false;
        String reason = "No reason specified";

        if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-s")) {
                    silent = true;
                    continue;
                }
                if (sb.length() > 0) sb.append(' ');
                sb.append(args[i]);
            }
            if (sb.length() > 0) {
                reason = sb.toString();
            }
        }

        IpBanManager ipBanManager = plugin.getIpBanManager();
        final String staff = sender.getName();

        ipBanManager.banIp(ip, staff, reason, durationMillis);

        String durationText = permanent ? "Permanent" : TimeUtil.formatDuration(durationMillis);

        plugin.getMessages().send(sender,
                permanent ? "ipban-success-perm" : "ipban-success",
                "ip", ip,
                "reason", reason,
                "duration", durationText,
                "staff", staff
        );

        if (!silent) {
            final String notifyPerm = plugin.getStaffNotifyPermission();
            final String ipF = ip;
            final String reasonF = reason;
            final String durationTextF = durationText;

            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission(notifyPerm))
                    .forEach(p -> plugin.getMessages().send(
                            p,
                            permanent ? "ipban-broadcast-perm" : "ipban-broadcast",
                            "ip", ipF,
                            "reason", reasonF,
                            "duration", durationTextF,
                            "staff", staff
                    ));
        }

        // Kick any currently-online player with that IP
        final String ipKick = ip;
        final String reasonKick = reason;
        final String durationKick = durationText;

        for (Player online : Bukkit.getOnlinePlayers()) {
            InetSocketAddress addr = online.getAddress();
            if (addr != null && addr.getAddress() != null) {
                String onlineIp = addr.getAddress().getHostAddress();
                if (ipKick.equals(onlineIp)) {
                    online.kick(plugin.getMessages().componentFromList(
                            "ipban-screen",
                            "ip", ipKick,
                            "duration", durationKick,
                            "reason", reasonKick,
                            "staff", staff
                    ));
                }
            }
        }

        return true;
    }
}
