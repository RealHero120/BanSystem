package me.hero.bansystem.commands;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.combat.CombatManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CombatCommand implements CommandExecutor {

    private final Bansystem plugin;
    private final CombatManager combat;

    public CombatCommand(Bansystem plugin, CombatManager combat) {
        this.plugin = plugin;
        this.combat = combat;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("status"))) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can check their own combat status.");
                return true;
            }

            if (!player.hasPermission("combatguard.status")) {
                sendMsg(player, "errors.no-permission", Map.of());
                return true;
            }

            boolean tagged = combat.isTagged(player.getUniqueId());
            int timeLeft = combat.getTimeLeft(player.getUniqueId());

            sendMsg(player, "combat.status-self", Map.of(
                    "state", tagged ? "§cTAGGED" : "§aCLEAR",
                    "time", String.valueOf(timeLeft)
            ));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
            if (!sender.hasPermission("combatguard.status.others")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            boolean tagged = combat.isTagged(target.getUniqueId());
            int timeLeft = combat.getTimeLeft(target.getUniqueId());

            sender.sendMessage("§8§m---------------------------------");
            sender.sendMessage("§eCombat Status: §f" + target.getName());
            sender.sendMessage("§eState: " + (tagged ? "§cTAGGED" : "§aCLEAR"));
            sender.sendMessage("§eTime Left: §f" + timeLeft + "s");
            sender.sendMessage("§8§m---------------------------------");
            return true;
        }

        sender.sendMessage("§cUsage: /combat status [player]");
        return true;
    }

    private void sendMsg(Player player, String key, Map<String, Object> placeholders) {
        String message = plugin.getMessagesConfig().getString(key, "");
        if (message == null || message.isEmpty()) return;

        String prefix = plugin.getMessagesConfig().getString("prefix", "");
        message = message.replace("%prefix%", prefix != null ? prefix : "");

        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            Object val = entry.getValue();
            message = message.replace("{" + entry.getKey() + "}", val != null ? val.toString() : "");
        }

        player.sendMessage(message.replace("&", "§"));
    }
}
