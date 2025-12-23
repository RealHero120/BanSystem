package me.hero.bansystem.util;

import me.hero.bansystem.BanSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class MessageUtil {

    private final BanSystem plugin;
    private FileConfiguration messages;
    private final File file;

    public MessageUtil(BanSystem plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        reload();
    }

    public void reload() {
        this.messages = YamlConfiguration.loadConfiguration(file);
    }

    // This is the "send" method your commands are missing
    public void send(CommandSender sender, String key, String... placeholders) {
        String message = messages.getString(key);
        if (message == null) return;

        // Replace the %prefix% tag
        String prefix = messages.getString("prefix", "");
        message = message.replace("%prefix%", prefix);

        // Handle placeholders (target, reason, staff, etc.)
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
        }

        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    // Helper for the kick screen lists in messages.yml
    public Component componentFromList(String key, String... placeholders) {
        List<String> lines = messages.getStringList(key);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            for (int i = 0; i < placeholders.length; i += 2) {
                line = line.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
            sb.append(line).append("\n");
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(sb.toString());
    }
}