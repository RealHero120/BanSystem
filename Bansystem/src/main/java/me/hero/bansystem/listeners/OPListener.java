package me.hero.bansystem.listeners;

import me.hero.bansystem.Bansystem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.net.InetSocketAddress;
import java.util.Locale;

public class OPListener implements Listener {

    private final Bansystem plugin;

    public OPListener(Bansystem plugin) {
        this.plugin = plugin;
    }

    private void deopAndKick(Player p, String reason) {
        if (p == null) return;

        String alertMessage = "[SECURE-OP] Unauthorized OP detected: " + p.getName() + " (Reason: " + reason + ")";
        Bukkit.getLogger().warning(alertMessage);

        try { p.setOp(false); } catch (Throwable ignored) {}
        try { p.setGameMode(GameMode.SURVIVAL); } catch (Throwable ignored) {}

        String kickMsg = plugin.getMessageUtil().color(plugin.getConfig().getString("secureop.kick-message", "&cYou are not allowed to be OP."));
        try {
            p.kickPlayer(kickMsg);
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to kick unauthorized op " + p.getName() + ": " + t.getMessage());
        }

        // Attempt to ban IP if available (guarded against null addresses)
        try {
            InetSocketAddress addr = p.getAddress();
            if (addr != null && addr.getAddress() != null) {
                plugin.getIpBanManager().ipBan(addr.getAddress().getHostAddress());
            }
        } catch (Throwable t) {
            plugin.getLogger().log(java.util.logging.Level.FINE, "Failed to ip-ban unauthorized op " + p.getName(), t);
        }
    }

    private boolean isOpCommand(String cmd) {
        return cmd.matches("^/(?:[a-z0-9_]+:)?op\\s+.+");
    }

    private boolean isDeopCommand(String cmd) {
        return cmd.matches("^/(?:[a-z0-9_]+:)?deop\\s+.+");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() && !plugin.getSecureOpManager().isAllowed(p)) {
            deopAndKick(p, "Join while OP");
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        String raw = e.getMessage();
        String lower = raw.toLowerCase(Locale.ROOT);
        Player sender = e.getPlayer();

        if (isOpCommand(lower)) {
            if (!plugin.getSecureOpManager().isAllowed(sender)) {
                e.setCancelled(true);
                sender.sendMessage(plugin.getMessageUtil().color("&cYou are not authorized to use this command."));

                String alert = "[SECURE-OP] " + sender.getName() + " attempted to use /op command!";
                Bukkit.getLogger().warning(alert);
                return;
            }

            String[] parts = raw.split("\\s+");
            if (parts.length >= 2 && !plugin.getSecureOpManager().isNameAllowed(parts[1])) {
                e.setCancelled(true);
                sender.sendMessage(plugin.getMessageUtil().color("&cThat player is not in the allowed-ops list."));

                String alert = "[SECURE-OP] " + sender.getName() + " tried to OP unauthorized player: " + parts[1];
                Bukkit.getLogger().warning(alert);
            }
            return;
        }

        if (isDeopCommand(lower)) {
            if (!plugin.getSecureOpManager().isAllowed(sender)) {
                e.setCancelled(true);
                sender.sendMessage(plugin.getMessageUtil().color("&cYou are not authorized to use this command."));
            }
        }
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent e) {
        String raw = e.getCommand();
        String lower = raw.toLowerCase(Locale.ROOT);

        if (isOpCommand(lower)) {
            String[] parts = raw.split("\\s+");
            if (parts.length >= 2 && !plugin.getSecureOpManager().isNameAllowed(parts[1])) {
                e.setCancelled(true);
                String alert = "[SECURE-OP] Console attempted to OP unauthorized player: " + parts[1];
                Bukkit.getLogger().warning(alert);
            }
        }
    }
}