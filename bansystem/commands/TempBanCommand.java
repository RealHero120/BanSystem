package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.TimeUtil;
import me.hero.bansystem.managers.BanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
            plugin.getMessages().send(sender, "tempban-usage", "label", label);
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

        // ---- duration + permanent flag (final-friendly) ----
        final boolean permanent;
        final long durationMillis;

        if (durationArg.equalsIgnoreCase("perm") || durationArg.equalsIgnoreCase("permanent")) {
            if (!plugin.allowPermanentBans()) {
                plugin.getMessages().send(sender, "perm-bans-disabled");
                return true;
            }
            permanent = true;
            durationMillis = -1L;
        } else {
            permanent = false;
            long parsed = TimeUtil.parseDuration(durationArg);
            if (parsed <= 0) {
                plugin.getMessages().send(sender, "invalid-duration", "input", durationArg);
                return true;
            }

            String maxStr = plugin.getMaxTempDurationString();
            long maxMillis = TimeUtil.parseDuration(maxStr);
            if (maxMillis > 0 && parsed > maxMillis) {
                plugin.getMessages().send(sender, "tempban-too-long",
                        "max", maxStr,
                        "input", durationArg
                );
                return true;
            }
            durationMillis = parsed;
        }

        // ---- reason + silent ----
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

        BanManager banManager = plugin.getBanManager();
        final String staff = sender.getName();

        banManager.tempBan(
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

        // Notify staff / broadcast if not silent
        if (!silent) {
            final String notifyPerm = plugin.getStaffNotifyPermission();
            final String reasonF = reason;
            final String durationTextF = durationText;

            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission(notifyPerm))
                    .forEach(p -> plugin.getMessages().send(
                            p,
                            permanent ? "tempban-broadcast-perm" : "tempban-broadcast",
                            "target", targetName,
                            "reason", reasonF,
                            "duration", durationTextF,
                            "staff", staff
                    ));
        }

        // If you don't have an immediate-kick helper, you can let BanListener kick on next login.

        return true;
    }
}
