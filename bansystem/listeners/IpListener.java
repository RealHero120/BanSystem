package me.hero.bansystem.listeners;

import me.hero.bansystem.BanSystem;
import me.hero.bansystem.TimeUtil;
import me.hero.bansystem.managers.BanManager;
import me.hero.bansystem.managers.IpBanManager;
import me.hero.bansystem.managers.IpBanManager.IpBanRecord;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;

public class IpListener implements Listener {

    private final BanSystem plugin;
    private final IpBanManager ipBanManager;
    private final BanManager banManager;

    public IpListener(BanSystem plugin) {
        this.plugin = plugin;
        this.ipBanManager = plugin.getIpBanManager();
        this.banManager = plugin.getBanManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.isFeatureEnabled("ip-bans-enabled")) {
            return;
        }

        Player player = e.getPlayer();
        InetSocketAddress addr = player.getAddress();
        if (addr == null || addr.getAddress() == null) {
            return;
        }

        String ip = addr.getAddress().getHostAddress();
        String name = player.getName();
        UUID uuid = player.getUniqueId();

        ipBanManager.logJoin(uuid, name, ip);

        IpBanRecord rec = ipBanManager.getIpBan(ip);
        if (rec != null) {
            String duration = rec.isPermanent()
                    ? "Permanent"
                    : TimeUtil.formatDuration(rec.getUntil() - System.currentTimeMillis());

            Component screen = plugin.getMessages().componentFromList(
                    "ipban-screen",
                    "ip", ip,
                    "duration", duration,
                    "reason", rec.getReason(),
                    "staff", rec.getStaff()
            );

            player.kick(screen);
            return;
        }

        Map<UUID, String> accounts = ipBanManager.getAccountsForIp(ip);
        accounts.remove(uuid);

        if (!accounts.isEmpty()) {
            int bannedCount = 0;
            for (UUID other : accounts.keySet()) {
                if (banManager.isBanned(other)) {
                    bannedCount++;
                }
            }

            if (bannedCount > 0) {
                String notifyPerm = plugin.getStaffNotifyPermission();
                String countStr = String.valueOf(accounts.size());

                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission(notifyPerm))
                        .forEach(p -> plugin.getMessages().send(
                                p,
                                "alt-alert-join",
                                "player", name,
                                "count", countStr
                        ));
            }
        }
    }
}
