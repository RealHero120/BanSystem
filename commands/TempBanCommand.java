package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.util.TimeUtil;
import me.hero.bansystem.managers.BanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TempBanCommand implements CommandExecutor {

    private final BanSystem plugin;

    public TempBanCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("bansystem.tempban")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessages().send(sender, "tempban-usage");
            return true;
        }

        String targetName = args[0];
        String durationArg = args[1];

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID uuid = target.getUniqueId();

        if (uuid == null) {
            plugin.getMessages().send(sender, "player-not-found", "target", targetName);
            return true;
        }

        boolean silent = false;
        long durationMillis = -1;
        String reason = "No reason specified";

        // Handle duration
        if (durationArg.equalsIgnoreCase("perm") || durationArg.equalsIgnoreCase("permanent")) {
            durationMillis = -1;
        } else {
            durationMillis = TimeUtil.parseDuration(durationArg);
            if (durationMillis <= 0) {
                plugin.getMessages().send(sender, "invalid-duration", "input", durationArg);
                return true;
            }
        }

        // Handle Reason and Silent flag
        int reasonStart = 2;
        if (args.length > 2 && args[2].equalsIgnoreCase("-s")) {
            silent = true;
            reasonStart = 3;
        }

        if (args.length > reasonStart) {
            StringBuilder sb = new StringBuilder();
            for (int i = reasonStart; i < args.length; i++) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(args[i]);
            }
            reason = sb.toString();
        }

        boolean permanent = (durationMillis == -1);
        String staff = sender.getName();

        // Perform the ban in manager
        plugin.getBanManager().ban(
                uuid,
                target.getName() == null ? targetName : target.getName(),
                durationMillis,
                reason,
                staff,
                silent
        );

        String durationText = permanent ? "Permanent" : TimeUtil.formatDuration(durationMillis);

        // Sender confirmation
        plugin.getMessages().send(sender,
                permanent ? "tempban-success-perm" : "tempban-success",
                "target", targetName,
                "reason", reason,
                "duration", durationText,
                "staff", staff
        );

        // KICK LOGIC: Remove player from server immediately
        Player targetOnline = Bukkit.getPlayer(uuid);
        if (targetOnline != null && targetOnline.isOnline()) {
            targetOnline.kick(plugin.getMessages().componentFromList(
                    permanent ? "ban-screen-perm" : "ban-screen",
                    "reason", reason,
                    "duration", durationText,
                    "staff", staff
            ));
        }

        // Notify staff / broadcast if not silent
        if (!silent) {
            final String notifyPerm = plugin.getStaffNotifyPermission();
            final String finalReason = reason;
            final String finalDurationText = durationText;

            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission(notifyPerm))
                    .forEach(p -> plugin.getMessages().send(
                            p,
                            permanent ? "tempban-broadcast-perm" : "tempban-broadcast",
                            "target", targetName,
                            "reason", finalReason,
                            "duration", finalDurationText,
                            "staff", staff
                    ));
        }

        return true;
    }
}