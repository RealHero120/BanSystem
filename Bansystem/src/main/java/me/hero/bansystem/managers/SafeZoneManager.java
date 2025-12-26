package me.hero.bansystem.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import me.hero.bansystem.Bansystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SafeZoneManager {

    private final Bansystem plugin;

    public SafeZoneManager(Bansystem plugin) {
        this.plugin = plugin;
    }

    public ZoneState getZoneState(Location loc) {
        // Defaults if WG is missing
        if (loc == null || Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            return ZoneState.DEFAULT;
        }

        try {
            var container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            var query = container.createQuery();
            var wgLoc = BukkitAdapter.adapt(loc);

            // We query with "null" local player (global flags still apply)
            StateFlag.State safeState = query.queryState(wgLoc, null, Bansystem.COMBATGUARD_SAFE_FLAG);
            StateFlag.State tagState  = query.queryState(wgLoc, null, Bansystem.COMBATGUARD_TAG_FLAG);

            boolean safe = safeState == StateFlag.State.ALLOW;

            // Tagging allowed unless explicitly DENY
            boolean tagAllowed = tagState != StateFlag.State.DENY;

            return new ZoneState(safe, tagAllowed);
        } catch (Throwable t) {
            return ZoneState.DEFAULT;
        }
    }

    public record ZoneState(boolean safe, boolean taggingAllowed) {
        public static final ZoneState DEFAULT = new ZoneState(false, true);
    }
}