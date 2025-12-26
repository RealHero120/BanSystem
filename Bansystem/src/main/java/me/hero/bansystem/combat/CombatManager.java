package me.hero.bansystem.combat;

import me.hero.bansystem.Bansystem;
import me.hero.bansystem.managers.SafeZoneManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple, thread-safe CombatManager implementation.
 * - Stores remaining combat time in seconds per player.
 * - Provides startTicker/stopTicker used by Bansystem to run the per-second decrement.
 * - Provides handleDeath to clear state on player death.
 */
public class CombatManager {

    private final Bansystem plugin;
    private final SafeZoneManager safeZoneManager;

    // remaining seconds in combat per player
    private final Map<UUID, Integer> combatLeft = new ConcurrentHashMap<>();

    // players who attempted to quit while in combat (example usage)
    private final Map<UUID, PendingQuit> pendingQuits = new ConcurrentHashMap<>();

    // snapshots or other metadata (placeholder)
    private final Map<UUID, InventorySnapshot> snapshots = new ConcurrentHashMap<>();

    // players that must die on join (example behavior)
    private final Set<UUID> mustDieOnJoin = new HashSet<>();

    private BukkitTask tickerTask;

    public CombatManager(Bansystem plugin, SafeZoneManager safeZoneManager) {
        this.plugin = plugin;
        this.safeZoneManager = safeZoneManager;
    }

    // Start a repeating task to decrement combat timers (runs on main thread)
    public void startTicker() {
        if (tickerTask != null) return; // already running
        long interval = 20L; // 20 ticks = 1 second
        tickerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                var it = combatLeft.entrySet().iterator();
                while (it.hasNext()) {
                    var e = it.next();
                    int left = e.getValue() - 1;
                    if (left <= 0) {
                        it.remove();
                        // optional: notify player combat ended
                    } else {
                        e.setValue(left);
                    }
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("Combat ticker encountered an error: " + t.getMessage());
            }
        }, interval, interval);
    }

    // Stop the ticker if running
    public void stopTicker() {
        if (tickerTask != null) {
            try {
                tickerTask.cancel();
            } finally {
                tickerTask = null;
            }
        }
    }

    // Called on player death to clear combat state and related data
    public void handleDeath(Player player) {
        if (player == null) return;
        UUID id = player.getUniqueId();
        combatLeft.remove(id);
        pendingQuits.remove(id);
        snapshots.remove(id);
        mustDieOnJoin.remove(id);
    }

    // Tag a player for a number of seconds (overwrites existing)
    public void tag(UUID id, int seconds) {
        if (id == null) return;
        if (seconds <= 0) {
            untag(id);
            return;
        }
        combatLeft.put(id, seconds);
    }

    public void untag(UUID id) {
        if (id == null) return;
        combatLeft.remove(id);
    }

    public boolean isTagged(UUID id) {
        Integer val = combatLeft.get(id);
        return val != null && val > 0;
    }

    public int getTimeLeft(UUID id) {
        Integer val = combatLeft.get(id);
        return val == null ? 0 : Math.max(0, val);
    }

    // Accessors used elsewhere in the plugin
    public Map<UUID, Integer> getCombatLeft() { return combatLeft; }
    public Map<UUID, PendingQuit> getPendingQuits() { return pendingQuits; }
    public Map<UUID, InventorySnapshot> getSnapshots() { return snapshots; }
    public Set<UUID> getMustDieOnJoin() { return mustDieOnJoin; }

    // Placeholder inner classes - replace with real implementations if needed
    public static class PendingQuit {
        // add fields/constructors as required
    }

    public static class InventorySnapshot {
        // add fields/constructors as required
    }
}
