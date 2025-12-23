package me.hero.bansystem.commands;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.managers.BanManager;
import me.hero.bansystem.managers.MuteManager;
import me.hero.bansystem.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class HistoryCommand implements CommandExecutor {

    private final BanSystem plugin;

    public HistoryCommand(BanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // 1. Permission Check based on plugin.yml
        if (!sender.hasPermission("bansystem.history")) {
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        // 2. Usage Check
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /history <player>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID uuid = target.getUniqueId(); // IDE fix: uuid is never null here

        // 3. Fetch active punishments from managers
        BanManager.BanEntry ban = plugin.getBanManager().getBan(uuid);
        MuteManager.MuteEntry mute = plugin.getMuteManager().getMute(uuid);

        if (ban == null && mute == null) {
            plugin.getMessages().send(sender, "history-none", "target", targetName);
            return true;
        }

        // 4. Send header from messages.yml
        plugin.getMessages().send(sender, "history-header", "target", targetName);

        // 5. Display Ban History if active
        if (ban != null) {
            String duration = (ban.expiry() == -1) ? "Permanent" : TimeUtil.formatDuration(ban.expiry() - System.currentTimeMillis());
            plugin.getMessages().send(sender, "history-entry",
                    "type", "BAN",
                    "duration", duration,
                    "reason", ban.reason(),
                    "staff", ban.staff());
        }

        // 6. Display Mute History if active
        if (mute != null) {
            String duration = (mute.expiry() == -1) ? "Permanent" : TimeUtil.formatDuration(mute.expiry() - System.currentTimeMillis());
            plugin.getMessages().send(sender, "history-entry",
                    "type", "MUTE",
                    "duration", duration,
                    "reason", mute.reason(),
                    "staff", mute.staff());
        }

        return true;
    }
}