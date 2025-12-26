package me.hero.bansystem.managers;

import me.hero.bansystem.Bansystem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class SecureOpManager {

    private final Bansystem plugin;
    private final Set<UUID> allowedUUIDs = new HashSet<>();
    private final Set<String> allowedNames = new HashSet<>();

    public SecureOpManager(Bansystem plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the allowed OP lists from the config file.
     */
    public void loadAllowedLists() {
        allowedUUIDs.clear();
        allowedNames.clear();

        try {
            FileConfiguration cfg = plugin.getConfig();
            List<String> names = cfg.getStringList("secureop.allowed-ops");
            if (names != null) {
                for (String name : names) {
                    if (name == null || name.isBlank()) continue;
                    allowedNames.add(name);

                    // Cache UUIDs to maintain security even if names change
                    try {
                        OfflinePlayer op = Bukkit.getOfflinePlayer(name);
                        if (op != null) {
                            UUID id = op.getUniqueId();
                            if (id != null) allowedUUIDs.add(id);
                        }
                    } catch (Throwable ignored) {}
                }
            }

            List<String> uuidStrings = cfg.getStringList("secureop.allowed-uuids");
            if (uuidStrings != null) {
                for (String s : uuidStrings) {
                    if (s == null || s.isBlank()) continue;
                    try {
                        allowedUUIDs.add(UUID.fromString(s));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed to load secureop lists from config", t);
        }
    }

    /**
     * Saves the allowed OP lists back to the config file so changes are persisted.
     */
    public void saveAllowedLists() {
        try {
            FileConfiguration cfg = plugin.getConfig();

            // Save allowed names
            List<String> names = new ArrayList<>(allowedNames);
            cfg.set("secureop.allowed-ops", names);

            // Save allowed UUIDs
            List<String> uuidStrings = new ArrayList<>();
            for (UUID id : allowedUUIDs) {
                if (id != null) uuidStrings.add(id.toString());
            }
            cfg.set("secureop.allowed-uuids", uuidStrings);

            plugin.saveConfig();
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed to save secureop lists to config", t);
        }
    }

    /**
     * Checks if a player is allowed to have OP status.
     */
    public boolean isAllowed(Player player) {
        if (player == null) return false;

        if (plugin.getConfig().getBoolean("secureop.use-permission", false)) {
            String perm = plugin.getConfig().getString("secureop.bypass-permission", "secureop.bypass");
            return player.hasPermission(perm);
        }

        UUID id = player.getUniqueId();
        String name = player.getName();
        return (id != null && allowedUUIDs.contains(id)) || (name != null && allowedNames.contains(name));
    }

    /**
     * Checks if a specific name is in the allowed list.
     */
    public boolean isNameAllowed(String name) {
        if (name == null || name.isBlank()) return false;

        // Check names list
        if (allowedNames.contains(name)) return true;

        // Check if player is online and allowed via UUID
        Player online = Bukkit.getPlayerExact(name);
        return online != null && isAllowed(online);
    }

    /**
     * Scans all online players and removes unauthorized OP status.
     * This method must be run on the main thread.
     */
    public void scanForUnauthorizedOps() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == null) continue;
            try {
                if (p.isOp() && !isAllowed(p)) {
                    processUnauthorizedOp(p, "Periodic Scan");
                }
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Error while scanning player for unauthorized OP: " + (p == null ? "null" : p.getName()), t);
            }
        }
    }

    /**
     * If you call scans from an async task, use this helper to ensure the actual
     * scan runs on the main thread.
     */
    public void scanForUnauthorizedOpsAsync() {
        Bukkit.getScheduler().runTask(plugin, this::scanForUnauthorizedOps);
    }

    /**
     * Handle unauthorized OP: log, remove op, set gamemode and kick.
     */
    public void processUnauthorizedOp(Player p, String reason) {
        if (p == null) return;

        String alert = "[SECURE-OP] Unauthorized OP detected: " + p.getName() + " (Reason: " + reason + ")";
        plugin.getLogger().warning(alert);

        try {
            p.setOp(false);
        } catch (Throwable ignored) {}

        try {
            p.setGameMode(GameMode.SURVIVAL);
        } catch (Throwable ignored) {}

        String raw = plugin.getConfig().getString("secureop.kick-message", "&cUnauthorized OP status.");
        String kickMsg;
        if (plugin.getMessageUtil() != null) {
            kickMsg = plugin.getMessageUtil().color(raw);
        } else {
            kickMsg = raw == null ? "" : raw.replace('&', '\u00A7');
        }

        try {
            p.kickPlayer(kickMsg);
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to kick unauthorized op " + p.getName() + ": " + t.getMessage());
        }
    }

    // Optional management helpers

    public boolean addAllowedName(String name) {
        if (name == null || name.isBlank()) return false;
        return allowedNames.add(name);
    }

    public boolean removeAllowedName(String name) {
        if (name == null) return false;
        return allowedNames.remove(name);
    }

    public boolean addAllowedUUID(UUID id) {
        if (id == null) return false;
        return allowedUUIDs.add(id);
    }

    public boolean removeAllowedUUID(UUID id) {
        if (id == null) return false;
        return allowedUUIDs.remove(id);
    }

    public Set<UUID> getAllowedUUIDs() {
        return Set.copyOf(allowedUUIDs);
    }

    public Set<String> getAllowedNames() {
        return Set.copyOf(allowedNames);
    }
}