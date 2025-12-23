package me.hero.bansystem.managers;

import me.hero.bansystem.BanSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IpBanManager {

    private final BanSystem plugin;
    private final Map<String, Set<UUID>> ipHistory = new HashMap<>();
    private final Set<String> bannedIps = new HashSet<>();
    private final File file;

    public IpBanManager(BanSystem plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ipdata.yml");
    }

    // FIXED: Renamed to match BanSystem.java call
    public void loadBans() {
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        // Load banned IPs
        bannedIps.addAll(cfg.getStringList("banned-ips"));

        // Load IP History for /alts
        if (cfg.getConfigurationSection("history") != null) {
            for (String ipKey : cfg.getConfigurationSection("history").getKeys(false)) {
                String ip = ipKey.replace("_", ".");
                List<String> uuids = cfg.getStringList("history." + ipKey);
                Set<UUID> uuidSet = new HashSet<>();
                for (String s : uuids) uuidSet.add(UUID.fromString(s));
                ipHistory.put(ip, uuidSet);
            }
        }
    }

    // FIXED: Renamed to match BanSystem.java call
    public void saveBans() {
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("banned-ips", new ArrayList<>(bannedIps));

        for (Map.Entry<String, Set<UUID>> entry : ipHistory.entrySet()) {
            String ipKey = entry.getKey().replace(".", "_");
            List<String> uuidStrings = new ArrayList<>();
            for (UUID u : entry.getValue()) uuidStrings.add(u.toString());
            cfg.set("history." + ipKey, uuidStrings);
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ipdata.yml!");
        }
    }

    public void logJoin(UUID uuid, String name, String ip) {
        ipHistory.computeIfAbsent(ip, k -> new HashSet<>()).add(uuid);
    }

    public Set<UUID> getAlts(String ip) {
        return ipHistory.getOrDefault(ip, Collections.emptySet());
    }

    public boolean isIpBanned(String ip) {
        return bannedIps.contains(ip);
    }

    public void ipBan(String ip) {
        bannedIps.add(ip);
        saveBans();
    }

    public void unIpBan(String ip) {
        bannedIps.remove(ip);
        saveBans();
    }
}