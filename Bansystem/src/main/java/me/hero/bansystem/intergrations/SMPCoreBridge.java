package me.hero.bansystem.integrations;

import me.hero.bansystem.Bansystem;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Best-effort reflection-based bridge to SMPCore 2.3.
 *
 * - Detects the SMPCore plugin instance at runtime
 * - Attempts to locate common moderation methods via reflection
 * - Exposes small moderation methods that return boolean success
 *
 * NOTE: This is intentionally defensive — replace lookups with concrete API calls
 * once you have SMPCore API documentation or a dependency jar.
 */
public class SMPCoreBridge {

    private final Bansystem plugin;
    private final Plugin smpcore;
    private Method banMethod;
    private Method unbanMethod;
    private Method isBannedMethod;
    private Method tempMuteMethod;
    private Method unmuteMethod;
    private Method isMutedMethod;

    public SMPCoreBridge(Bansystem plugin) {
        this.plugin = plugin;
        this.smpcore = Bukkit.getPluginManager().getPlugin("SMPCore");
        if (smpcore == null) {
            plugin.getLogger().info("SMPCore not found.");
            return;
        }
        plugin.getLogger().info("SMPCore detected; attempting reflection bindings...");
        try {
            for (Method m : smpcore.getClass().getMethods()) {
                String name = m.getName().toLowerCase();
                // Ban-related
                if ((name.contains("tempban") || name.equals("ban") || name.contains("banplayer")) && banMethod == null) {
                    banMethod = m;
                } else if ((name.contains("unban") || name.contains("pardon")) && unbanMethod == null) {
                    unbanMethod = m;
                } else if ((name.contains("isbanned") || name.contains("checkban")) && isBannedMethod == null) {
                    isBannedMethod = m;
                }

                // Mute-related
                if ((name.contains("tempmute") || name.equals("mute") || name.contains("muteplayer")) && tempMuteMethod == null) {
                    tempMuteMethod = m;
                } else if (name.contains("unmute") && unmuteMethod == null) {
                    unmuteMethod = m;
                } else if ((name.contains("ismuted") || name.contains("checkmute")) && isMutedMethod == null) {
                    isMutedMethod = m;
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed to bind to SMPCore via reflection", t);
        }

        plugin.getLogger().info("SMPCore reflection bridge initialized: ban=" + (banMethod != null)
                + " unban=" + (unbanMethod != null) + " mute=" + (tempMuteMethod != null)
                + " unmute=" + (unmuteMethod != null));
    }

    public boolean isAvailable() {
        return smpcore != null;
    }

    // Example wrappers. They attempt to call the found methods, trying a few common param patterns.
    public boolean ban(UUID target, String targetName, long durationMillis, String reason, String staff) {
        if (banMethod == null) return false;
        try {
            // Try (UUID, long, String, String)
            tryInvoke(banMethod, new Object[]{ target, durationMillis, reason, staff });
            return true;
        } catch (Throwable t) {
            try {
                // Try (String, long, String, String)
                tryInvoke(banMethod, new Object[]{ targetName, durationMillis, reason, staff });
                return true;
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.FINE, "SMPCore ban invocation failed", ex);
                return false;
            }
        }
    }

    public boolean unban(UUID target) {
        if (unbanMethod == null) return false;
        try {
            tryInvoke(unbanMethod, new Object[]{ target });
            return true;
        } catch (Throwable t) {
            try {
                tryInvoke(unbanMethod, new Object[]{ target.toString() });
                return true;
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.FINE, "SMPCore unban invocation failed", ex);
                return false;
            }
        }
    }

    public boolean isBanned(UUID target) {
        if (isBannedMethod == null) return false;
        try {
            Object res = tryInvoke(isBannedMethod, new Object[]{ target });
            if (res instanceof Boolean) return (Boolean) res;
            if (res instanceof Integer) return ((Integer) res) != 0;
            return false;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "SMPCore isBanned invocation failed", t);
            return false;
        }
    }

    public boolean mute(UUID target, String targetName, long durationMillis, String reason, String staff) {
        if (tempMuteMethod == null) return false;
        try {
            tryInvoke(tempMuteMethod, new Object[]{ target, durationMillis, reason, staff });
            return true;
        } catch (Throwable t) {
            try {
                tryInvoke(tempMuteMethod, new Object[]{ targetName, durationMillis, reason, staff });
                return true;
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.FINE, "SMPCore mute invocation failed", ex);
                return false;
            }
        }
    }

    public boolean unmute(UUID target) {
        if (unmuteMethod == null) return false;
        try {
            tryInvoke(unmuteMethod, new Object[]{ target });
            return true;
        } catch (Throwable t) {
            try {
                tryInvoke(unmuteMethod, new Object[]{ target.toString() });
                return true;
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.FINE, "SMPCore unmute invocation failed", ex);
                return false;
            }
        }
    }

    public boolean isMuted(UUID target) {
        if (isMutedMethod == null) return false;
        try {
            Object res = tryInvoke(isMutedMethod, new Object[]{ target });
            if (res instanceof Boolean) return (Boolean) res;
            if (res instanceof Integer) return ((Integer) res) != 0;
            return false;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "SMPCore isMuted invocation failed", t);
            return false;
        }
    }

    private Object tryInvoke(Method m, Object[] args) throws Exception {
        m.setAccessible(true);
        Object instance = (java.lang.reflect.Modifier.isStatic(m.getModifiers())) ? null : smpcore;
        return m.invoke(instance, args);
    }
}