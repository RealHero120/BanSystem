package me.hero.bansystem.managers;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IpBanManager {

    public static class IpBanRecord {
        private final String ip;
        private final String staff;
        private final String reason;
        private final long until; // -1 = permanent

        public IpBanRecord(String ip, String staff, String reason, long until) {
            this.ip = ip;
            this.staff = staff;
            this.reason = reason;
            this.until = until;
        }

        public String getIp() {
            return ip;
        }

        public String getStaff() {
            return staff;
        }

        public String getReason() {
            return reason;
        }

        public long getUntil() {
            return until;
        }

        public boolean isPermanent() {
            return until < 0;
        }
    }

    private final BanSystem plugin;

    // Thread-safe map for potential async access in the future
    private final Map<String, IpBanRecord> ipBans = new ConcurrentHashMap<>();

    private File ipBansFile;
    private FileConfiguration ipBansConfig;

    private File ipsFile;
    private FileConfiguration ipsConfig;

    public IpBanManager(BanSystem plugin) {
        this.plugin = plugin;
        createFiles();
        loadIpBans();
        loadIpMap();
    }

    private void createFiles() {
        // ipbans.yml
        ipBansFile = new File(plugin.getDataFolder(), "ipbans.yml");
        if (!ipBansFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                ipBansFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create ipbans.yml!");
                e.printStackTrace();
            }
        }
        ipBansConfig = YamlConfiguration.loadConfiguration(ipBansFile);

        // ips.yml
        ipsFile = new File(plugin.getDataFolder(), "ips.yml");
        if (!ipsFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                ipsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create ips.yml!");
                e.printStackTrace();
            }
        }
        ipsConfig = YamlConfiguration.loadConfiguration(ipsFile);
    }

    // ===== IP bans =====

    public void loadIpBans() {
        ipBans.clear();
        if (!ipBansConfig.isConfigurationSection("ipbans")) {
            return;
        }

        final long now = System.currentTimeMillis();

        ConfigurationSection section = ipBansConfig.getConfigurationSection("ipbans");
        if (section == null) {
            return;
        }

        for (String ip : section.getKeys(false)) {
            String path = "ipbans." + ip;
            String staff = ipBansConfig.getString(path + ".staff", "Console");
            String reason = ipBansConfig.getString(path + ".reason", "No reason specified");
            long until = ipBansConfig.getLong(path + ".until", -1L);

            if (until >= 0 && until <= now) {
                // expired temp IP ban; skip loading
                continue;
            }

            ipBans.put(ip, new IpBanRecord(ip, staff, reason, until));
        }
    }

    public void saveIpBans() {
        final long now = System.currentTimeMillis();

        // Build config in main thread
        ipBansConfig.set("ipbans", null);
        for (IpBanRecord rec : ipBans.values()) {
            long until = rec.getUntil();
            if (until >= 0 && until <= now) {
                continue;
            }

            String path = "ipbans." + rec.getIp();
            ipBansConfig.set(path + ".staff", rec.getStaff());
            ipBansConfig.set(path + ".reason", rec.getReason());
            ipBansConfig.set(path + ".until", until);
        }

        // IO off the main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                ipBansConfig.save(ipBansFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save ipbans.yml!");
                e.printStackTrace();
            }
        });
    }

    public void banIp(String ip, String staff, String reason, long durationMillis) {
        long until = (durationMillis < 0) ? -1L : System.currentTimeMillis() + durationMillis;
        IpBanRecord rec = new IpBanRecord(ip, staff, reason, until);
        ipBans.put(ip, rec);
        saveIpBans();
    }

    public void unbanIp(String ip) {
        ipBans.remove(ip);
        saveIpBans();
    }

    public IpBanRecord getIpBan(String ip) {
        IpBanRecord rec = ipBans.get(ip);
        if (rec == null) {
            return null;
        }

        if (!rec.isPermanent() && System.currentTimeMillis() > rec.getUntil()) {
            ipBans.remove(ip);
            saveIpBans();
            return null;
        }
        return rec;
    }

    public boolean isIpBanned(String ip) {
        return getIpBan(ip) != null;
    }

    // ===== IP map / alt tracking =====

    public void loadIpMap() {
        // On-demand read via ipsConfig; no heavy caching needed
    }

    public void saveIpMap() {
        // Call this from onDisable or a scheduled task to avoid frequent disk writes.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                ipsConfig.save(ipsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save ips.yml!");
                e.printStackTrace();
            }
        });
    }

    public void logJoin(UUID uuid, String name, String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }

        String base = "ips." + ip;
        ipsConfig.set(base + "." + uuid, name);
        // also store last known IP
        ipsConfig.set("lastip." + uuid, ip);
        // No immediate disk save here to reduce IO; rely on periodic or shutdown saveIpMap()
    }

    public String getLastKnownIp(UUID uuid) {
        return ipsConfig.getString("lastip." + uuid, null);
    }

    public Map<UUID, String> getAccountsForIp(String ip) {
        Map<UUID, String> result = new HashMap<>();

        String base = "ips." + ip;
        if (!ipsConfig.isConfigurationSection(base)) {
            return result;
        }

        ConfigurationSection sec = ipsConfig.getConfigurationSection(base);
        if (sec == null) {
            return result;
        }

        for (String key : sec.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String name = sec.getString(key, "Unknown");
                result.put(uuid, name);
            } catch (IllegalArgumentException ignored) {
                // malformed UUID in config; skip
            }
        }

        return result;
    }
}
