package me.hero.bansystem.listeners;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.combat.CombatManager;
import me.hero.bansystem.managers.SafeZoneManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class CombatRestrictionListener implements Listener {
    private final Bansystem plugin;
    private final CombatManager combat;
    private final SafeZoneManager safeZones;

    public CombatRestrictionListener(Bansystem plugin, CombatManager combat, SafeZoneManager safeZones) {
        this.plugin = plugin;
        this.combat = combat;
        this.safeZones = safeZones;
    }

    @EventHandler
    public void onSafeZoneEntry(PlayerMoveEvent e) {
        if (!plugin.getConfig().getBoolean("restrictions.safezone-entry.enabled", true)) return;
        if (e.getFrom() == null || e.getTo() == null) return;
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        Player p = e.getPlayer();
        UUID id = p.getUniqueId();

        // Use combat map to determine if player is tagged
        if (!combat.getCombatLeft().containsKey(id)) return;

        var zoneState = safeZones.getZoneState(e.getTo());
        if (zoneState != null && zoneState.safe()) {
            e.setCancelled(true);

            String raw = null;
            if (plugin.getMessagesConfig() != null) {
                raw = plugin.getMessagesConfig().getString("restrictions.safezone-entry.message");
            }
            if (raw == null) raw = plugin.getConfig().getString("restrictions.safezone-entry.message", "&cYou cannot enter safe zones while in combat!");
            String msg = plugin.getMessageUtil() != null ? plugin.getMessageUtil().color(raw) : raw.replace("&", "§");

            p.sendMessage(msg);
        }
    }

    @EventHandler
    public void onPearl(PlayerInteractEvent e) {
        if (!plugin.getConfig().getBoolean("restrictions.ender-pearls.enabled", true)) return;
        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (e.getItem() == null || e.getItem().getType() != Material.ENDER_PEARL) return;

        Player p = e.getPlayer();
        UUID id = p.getUniqueId();

        if (!combat.getCombatLeft().containsKey(id)) return;

        e.setCancelled(true);

        String raw = null;
        if (plugin.getMessagesConfig() != null) {
            raw = plugin.getMessagesConfig().getString("restrictions.ender-pearls.message");
        }
        if (raw == null) raw = plugin.getConfig().getString("restrictions.ender-pearls.message", "&cYou cannot use ender pearls while in combat!");
        String msg = plugin.getMessageUtil() != null ? plugin.getMessageUtil().color(raw) : raw.replace("&", "§");

        p.sendMessage(msg);
    }

    @EventHandler
    public void onElytra(EntityToggleGlideEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        UUID id = p.getUniqueId();

        if (!combat.getCombatLeft().containsKey(id)) return;

        if (e.isGliding()) {
            e.setCancelled(true);

            String raw = null;
            if (plugin.getMessagesConfig() != null) {
                raw = plugin.getMessagesConfig().getString("restrictions.elytra.message");
            }
            if (raw == null) raw = plugin.getConfig().getString("restrictions.elytra.message", "&cYou cannot use elytra while in combat!");
            String msg = plugin.getMessageUtil() != null ? plugin.getMessageUtil().color(raw) : raw.replace("&", "§");

            p.sendMessage(msg);
        }
    }
}
