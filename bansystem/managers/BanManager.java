package me.hero.bansystem.managers;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BanManager {

    public static class BanRecord {
        private final UUID uuid;
        private final String name;
        private final String reason;
        private final String staff;
        private final long until; // -1 = permanent

        public BanRecord(UUID uuid, String name, String reason, String staff, long until) {
            this.uuid = uuid;
            this.name = name;
            this.reason = reason;
            this.staff = staff;
            this.until = until;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public String getReason() {
            return reason;
        }

        public String getStaff() {
            return staff;
        }

        public long getUntil() {
            return until;
        }
    }

    public static class BanHistoryEntry {
        private final long time;
        private final String name;
        private final String reason;
        private final String staff;
        private final long durationMillis;
        private final boolean permanent;
        private final boolean silent;

        public BanHistoryEntry(long time, String name, String reason, String staff,
                               long durationMillis, boolean permanent, boolean silent) {
            this.time = time;
            this.name = name;
            this.reason = reason;
            this.staff = staff;
            this.durationMillis = durationMillis;
            this.permanent = permanent;
            this.silent = silent;
        }

        public long getTime() {
            return time;
        }

        public String getName() {
            return name;
        }

        public String getReason() {
            return reason;
        }

        public String getStaff() {
            return staff;
        }

        public long getDurationMillis() {
            return durationMillis;
        }

        public boolean isPermanent() {
            return permanent;
        }

        public boolean isSilent() {
            return silent;
        }

        public String formatDate() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(time));
        }
    }

    private final BanSystem plugin;
    private final Map<UUID, BanRecord> bans = new ConcurrentHashMap<>();

    private File bansFile;
    private FileConfiguration bansConfig;

    private File historyFile;
    private FileConfiguration historyConfig;

    public BanManager(BanSystem plugin) {
        this.plugin = plugin;
        createFiles();
    }

    private void createFiles() {
        // bans.yml
        bansFile = new File(plugin.getDataFolder(), "bans.yml");
        if (!bansFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                bansFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create bans.yml!");
                e.printStackTrace();
            }
        }
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);

        // history.yml
        historyFile = new File(plugin.getDataFolder(), "history.yml");
        if (!historyFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                historyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create history.yml!");
                e.printStackTrace();
            }
        }
        historyConfig = YamlConfiguration.loadConfiguration(historyFile);
    }

    // ===== Active bans =====

    public void loadBans() {
        bans.clear();
        if (!bansConfig.isConfigurationSection("bans")) {
            return;
        }

        final long now = System.currentTimeMillis();
        for (String key : bansConfig.getConfigurationSection("bans").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String path = "bans." + key;

            long until = bansConfig.getLong(path + ".until", 0L);

            if (until >= 0 && until <= now) {
                // expired temp ban; don't load
                continue;
            }

            String name = bansConfig.getString(path + ".name", "Unknown");
            String reason = bansConfig.getString(path + ".reason", "No reason specified");
            String staff = bansConfig.getString(path + ".staff", "Console");

            bans.put(uuid, new BanRecord(uuid, name, reason, staff, until));
        }
    }

    public void saveBans() {
        final long now = System.currentTimeMillis();
        bansConfig.set("bans", null);

        for (BanRecord record : bans.values()) {
            long until = record.getUntil();
            if (until >= 0 && until <= now) {
                continue; // expired temp ban
            }

            String path = "bans." + record.getUuid();
            bansConfig.set(path + ".name", record.getName());
            bansConfig.set(path + ".reason", record.getReason());
            bansConfig.set(path + ".staff", record.getStaff());
            bansConfig.set(path + ".until", until);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                bansConfig.save(bansFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save bans.yml!");
                e.printStackTrace();
            }
        });
    }

    /**
     * durationMillis < 0 == permanent ban.
     */
    public void tempBan(UUID uuid, String name, long durationMillis, String reason,
                        String staff, boolean silent) {

        long until = (durationMillis < 0) ? -1L : System.currentTimeMillis() + durationMillis;
        BanRecord record = new BanRecord(uuid, name, reason, staff, until);
        bans.put(uuid, record);
        saveBans();

        recordHistory(uuid, name, reason, staff, durationMillis, durationMillis < 0, silent);
    }

    public boolean isBanned(UUID uuid) {
        BanRecord record = bans.get(uuid);
        if (record == null) {
            return false;
        }

        long until = record.getUntil();
        if (until < 0) {
            // permanent ban
            return true;
        }

        long now = System.currentTimeMillis();
        if (now > until) {
            bans.remove(uuid);
            saveBans();
            return false;
        }

        return true;
    }

    public BanRecord getRecord(UUID uuid) {
        return isBanned(uuid) ? bans.get(uuid) : null;
    }

    public long getRemaining(UUID uuid) {
        BanRecord record = bans.get(uuid);
        if (record == null) {
            return 0L;
        }
        if (record.getUntil() < 0) {
            return -1L; // permanent
        }

        long diff = record.getUntil() - System.currentTimeMillis();
        return Math.max(diff, 0L);
    }

    public void unban(UUID uuid) {
        BanRecord record = bans.remove(uuid);
        saveBans();

        if (record != null) {
            // Optional: record unban event
        }
    }

    public Collection<BanRecord> getAllActiveBans() {
        return Collections.unmodifiableCollection(bans.values());
    }

    // ===== History =====

    public void recordHistory(UUID uuid, String name, String reason, String staff,
                              long durationMillis, boolean permanent, boolean silent) {

        if (!plugin.isFeatureEnabled("history-enabled")) {
            return;
        }

        long now = System.currentTimeMillis();
        String base = "history." + uuid + "." + now;

        historyConfig.set(base + ".name", name);
        historyConfig.set(base + ".reason", reason);
        historyConfig.set(base + ".staff", staff);
        historyConfig.set(base + ".duration", durationMillis);
        historyConfig.set(base + ".permanent", permanent);
        historyConfig.set(base + ".silent", silent);

        saveHistory();
    }

    private void saveHistory() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                historyConfig.save(historyFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save history.yml!");
                e.printStackTrace();
            }
        });
    }

    public List<BanHistoryEntry> getHistory(UUID uuid) {
        List<BanHistoryEntry> list = new ArrayList<>();

        String base = "history." + uuid;
        if (!historyConfig.isConfigurationSection(base)) {
            return list;
        }

        for (String key : historyConfig.getConfigurationSection(base).getKeys(false)) {
            String path = base + "." + key;
            long time;
            try {
                time = Long.parseLong(key);
            } catch (NumberFormatException ex) {
                continue;
            }

            String name = historyConfig.getString(path + ".name", "Unknown");
            String reason = historyConfig.getString(path + ".reason", "No reason specified");
            String staff = historyConfig.getString(path + ".staff", "Console");
            long duration = historyConfig.getLong(path + ".duration", 0L);
            boolean permanent = historyConfig.getBoolean(path + ".permanent", false);
            boolean silent = historyConfig.getBoolean(path + ".silent", false);

            list.add(new BanHistoryEntry(time, name, reason, staff, duration, permanent, silent));
        }

        // newest first
        list.sort(Comparator.comparingLong(BanHistoryEntry::getTime).reversed());
        return list;
    }
}
