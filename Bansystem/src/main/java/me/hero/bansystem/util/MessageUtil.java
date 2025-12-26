package me.hero.bansystem.util;

import me.hero.bansystem.Bansystem;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MessageUtil {

    private final Bansystem plugin;

    public MessageUtil(Bansystem plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration getMsg() {
        return plugin.getMessagesConfig();
    }

    /**
     * Translates '&' color codes into Minecraft color codes.
     */
    public String color(String input) {
        return input == null ? "" : ChatColor.translateAlternateColorCodes('&', input);
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        String message = getMsg().getString(key);
        if (message == null) return;

        String prefix = getMsg().getString("prefix", "");
        message = message.replace("%prefix%", prefix);

        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
        }

        sender.sendMessage(color(message));
    }

    public List<String> getList(String key, String... placeholders) {
        List<String> lines = getMsg().getStringList(key);
        List<String> formatted = new ArrayList<>();

        if (lines == null) return formatted;

        for (String line : lines) {
            for (int j = 0; j < placeholders.length; j += 2) {
                if (j + 1 < placeholders.length) {
                    line = line.replace("{" + placeholders[j] + "}", placeholders[j + 1]);
                }
            }
            formatted.add(color(line));
        }

        return formatted;
    }
}