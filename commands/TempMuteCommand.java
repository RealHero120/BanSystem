package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class TempMuteCommand implements CommandExecutor {

    private final BanSystem plugin;

    public TempMuteCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // 1. Permission Check
        if (!sender.hasPermission("bansystem.tempmute")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        // 2. Usage Check
        if (args.length < 2) {
            plugin.getMessages().send(sender, "tempmute-usage");
            return true;
        }

        String targetName = args[0];
        String durationArg = args[1];

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID uuid = target.getUniqueId(); // IDE: uuid is never null here in modern versions

        // 3. Duration Parsing
        long durationMillis;
        if (durationArg.equalsIgnoreCase("perm") || durationArg.equalsIgnoreCase("permanent")) {
            durationMillis = -1;
        } else {
            durationMillis = TimeUtil.parseDuration(durationArg);
            if (durationMillis <= 0) {
                plugin.getMessages().send(sender, "invalid-duration", "input", durationArg);
                return true;
            }
        }

        // 4. Handle Reason and Silent Flag
        boolean silent = false;
        int reasonStart = 2;

        if (args.length > 2 && args[2].equalsIgnoreCase("-s")) {
            silent = true;
            reasonStart = 3;
        }

        String reason = "No reason specified";
        if (args.length > reasonStart) {
            StringBuilder sb = new StringBuilder();
            for (int i = reasonStart; i < args.length; i++) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(args[i]);
            }
            reason = sb.toString();
        }

        // 5. Execute Mute in Manager
        String staff = sender.getName();
        plugin.getMuteManager().mute(
                uuid,
                target.getName() != null ? target.getName() : targetName,
                durationMillis,
                reason,
                staff
        );

        // 6. Feedback to Sender
        String durationText = (durationMillis == -1) ? "Permanent" : TimeUtil.formatDuration(durationMillis);
        plugin.getMessages().send(sender, "tempmute-success",
                "target", targetName,
                "duration", durationText,
                "reason", reason);

        // 7. Broadcast to Staff/Server if not silent
        if (!silent) {
            String notifyPerm = plugin.getStaffNotifyPermission();
            String finalReason = reason;
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission(notifyPerm))
                    .forEach(p -> plugin.getMessages().send(p, "tempmute-broadcast",
                            "staff", staff,
                            "target", targetName,
                            "duration", durationText,
                            "reason", finalReason));
        }

        return true;
    }
}