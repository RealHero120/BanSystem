// src/main/java/me/hero/bansystem/listeners/CombatListener.java
package me.hero.bansystem.listeners;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.combat.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CombatListener implements Listener {

    private final Bansystem plugin;
    private final CombatManager combat;

    public CombatListener(Bansystem plugin, CombatManager combat) {
        this.plugin = plugin;
        this.combat = combat;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (attacker.getUniqueId().equals(victim.getUniqueId())) return;

        long seconds = 10L;
        try {
            seconds = plugin.getConfig().getLong("combat.duration-seconds", 10L);
            if (seconds < 0) seconds = 10L;
        } catch (Throwable ignored) {}

        long expiryEpochLong = System.currentTimeMillis() / 1000L + seconds;
        int expiryEpochSec;
        if (expiryEpochLong > Integer.MAX_VALUE) {
            expiryEpochSec = Integer.MAX_VALUE;
        } else {
            expiryEpochSec = (int) expiryEpochLong;
        }

        try {
            combat.getCombatLeft().put(attacker.getUniqueId(), expiryEpochSec);
            combat.getCombatLeft().put(victim.getUniqueId(), expiryEpochSec);
        } catch (Throwable ignored) {
            // best-effort; don't throw from event handler
        }
    }
}
