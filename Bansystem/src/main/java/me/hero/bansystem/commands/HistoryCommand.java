package me.hero.bansystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.hero.bansystem.Bansystem;
import me.hero.bansystem.managers.BanManager;
import me.hero.bansystem.managers.MuteManager;
import me.hero.bansystem.util.TimeUtil;

public class HistoryCommand implements CommandExecutor {
    private final Bansystem plugin;

    public HistoryCommand(Bansystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("bansystem.history")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /history ");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        sender.sendMessage("§8§m---------------------------------");
        sender.sendMessage("§6Punishment History for: §f" + target.getName());

        if (plugin.getBanManager().isBanned(target.getUniqueId())) {
            BanManager.BanEntry ban = plugin.getBanManager().getBan(target.getUniqueId());
            String time = (ban.expiry() == -1L) ? "Permanent" : TimeUtil.formatDuration(ban.expiry() - System.currentTimeMillis());
            sender.sendMessage(" §c§lBANNED §8- §7Expires: §f" + time + " §8| §7Staff: §f" + ban.staff());
            sender.sendMessage("  §7Reason: §f" + ban.reason());
        } else {
            sender.sendMessage(" §a§lNOT BANNED");
        }

        if (plugin.getMuteManager().isMuted(target.getUniqueId())) {
            MuteManager.MuteEntry mute = plugin.getMuteManager().getMute(target.getUniqueId());
            long expiry = (mute != null) ? mute.expiryMillis() : -1L; // use generated accessor
            String time = (expiry == -1L) ? "Permanent" : TimeUtil.formatDuration(expiry - System.currentTimeMillis());
            sender.sendMessage(" §e§lMUTED §8- §7Expires: §f" + time + " §8| §7Staff: §f" + (mute != null ? mute.staff() : "Unknown"));
            sender.sendMessage("  §7Reason: §f" + (mute != null ? mute.reason() : "No reason provided"));
        } else {
            sender.sendMessage(" §a§lNOT MUTED");
        }

        return true;
    }
}