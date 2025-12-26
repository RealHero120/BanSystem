// java
package me.hero.bansystem.listeners;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.combat.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.UUID;

public class CommandListener implements Listener {

    private final Bansystem plugin;
    private final CombatManager combat;

    public CommandListener(Bansystem plugin, CombatManager combat) {
        this.plugin = plugin;
        this.combat = combat;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        // Use the CombatManager's combat map accessor to determine if player is tagged
        if (!combat.getCombatLeft().containsKey(id)) return;

        // Allow bypass for specific staff members
        if (player.hasPermission("combatguard.bypass.commands")) return;

        String message = event.getMessage().toLowerCase();
        String cmd = message.split(" ")[0].replace("/", "");

        // Check against the blocked-commands list in config.yml
        List<String> blocked = plugin.getConfig().getStringList("combat.blocked-commands");
        if (blocked != null && blocked.contains(cmd)) {
            event.setCancelled(true);

            String raw = null;
            if (plugin.getMessagesConfig() != null) {
                raw = plugin.getMessagesConfig().getString("combat.command-blocked");
            }
            if (raw == null) raw = plugin.getConfig().getString("combat.command-blocked", "&cYou cannot use that command in combat!");

            String msg = plugin.getMessageUtil() != null ? plugin.getMessageUtil().color(raw) : raw.replace("&", "§");
            player.sendMessage(msg);
        }
    }
}
