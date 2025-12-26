package me.hero.bansystem.managers;

import me.hero.bansystem.Bansystem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IpBanManager {

    private final Bansystem plugin;
    private final Map<String, Set<UUID>> ipHistory = new HashMap<>();
    private final Set<String> bannedIps = new HashSet<>();
    private final File file;

    public IpBanManager(Bansystem plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ipdata.yml");
    }

    public void loadBans() {
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        List<String> loadedBans = cfg.getStringList("banned-ips");
        if (loadedBans != null) {
            bannedIps.addAll(loadedBans);
        }

        ConfigurationSection historySection = cfg.getConfigurationSection("history");
        if (historySection != null) {
            for (String ipKey : historySection.getKeys(false)) {
                String ip = ipKey.replace("_", ".");
                List<String> uuids = cfg.getStringList("history." + ipKey);
                Set<UUID> uuidSet = new HashSet<>();
                if (uuids != null) {
                    for (String s : uuids) {
                        try {
                            uuidSet.add(UUID.fromString(s));
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
                ipHistory.put(ip, uuidSet);
            }
        }
    }

    public void saveBans() {
        YamlConfiguration cfg = new YamlConfiguration();
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

    public void ipBan(String ip) {
        bannedIps.add(ip);
        saveBans();
    }

    public void unIpBan(String ip) {
        bannedIps.remove(ip);
        saveBans();
    }

    public boolean isIpBanned(String ip) {
        return bannedIps.contains(ip);
    }

    public void logJoin(UUID uuid, String ip) {
        ipHistory.computeIfAbsent(ip, k -> new HashSet<>()).add(uuid);
        saveBans();
    }

    public Set<UUID> getAlts(String ip) {
        return Collections.unmodifiableSet(ipHistory.getOrDefault(ip, Collections.emptySet()));
    }
}