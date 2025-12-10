package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.TimeUtil;
import me.hero.bansystem.managers.MuteManager;
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

        if (!sender.hasPermission("bansystem.tempmute")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessages().send(sender, "tempmute-usage", "label", label);
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

        long durationMillis = TimeUtil.parseDuration(durationArg);
        if (durationMillis <= 0) {
            plugin.getMessages().send(sender, "invalid-duration", "input", durationArg);
            return true;
        }

        String reason = "No reason specified";
        if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(args[i]);
            }
            reason = sb.toString();
        }

        MuteManager muteManager = plugin.getMuteManager();
        muteManager.mute(uuid, durationMillis);

        String durationText = TimeUtil.formatDuration(durationMillis);
        String staff = sender.getName();

        plugin.getMessages().send(sender, "tempmute-success",
                "target", targetName,
                "duration", durationText,
                "reason", reason,
                "staff", staff
        );

        if (target.isOnline() && target.getPlayer() != null) {
            plugin.getMessages().send(target.getPlayer(), "tempmute-notify",
                    "duration", durationText,
                    "reason", reason
            );
        }

        return true;
    }
}
