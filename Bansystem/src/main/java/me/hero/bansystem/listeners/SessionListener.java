package me.hero.bansystem.listeners;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.combat.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class SessionListener implements Listener {

    private final CombatManager combat;

    public SessionListener(Bansystem plugin, CombatManager combat) {
        this.combat = combat;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID id = player.getUniqueId();

        // If player was marked to "must die on join", perform that action and clear the mark.
        try {
            if (combat.getMustDieOnJoin().remove(id)) {
                try {
                    player.setHealth(0.0);
                } catch (Throwable ignored) { /* best-effort kill */ }
            }
        } catch (Throwable ignored) {}

        // Clear any pending quit record for this player (they rejoined).
        try {
            combat.getPendingQuits().remove(id);
        } catch (Throwable ignored) {}

        // Remove any stored inventory snapshot for this player (restore logic belongs in CombatManager).
        try {
            combat.getSnapshots().remove(id);
        } catch (Throwable ignored) {}
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID id = player.getUniqueId();

        // If player is currently in combat, register a pending quit entry so CombatManager can handle it.
        try {
            if (combat.getCombatLeft().containsKey(id)) {
                combat.getPendingQuits().put(id, new CombatManager.PendingQuit());
            }
        } catch (Throwable ignored) {}
    }
}
