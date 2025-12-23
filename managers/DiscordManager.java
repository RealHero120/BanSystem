package me.hero.bansystem.managers;

import me.hero.bansystem.BanSystem;
import org.bukkit.Bukkit;
import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordManager {
    private final BanSystem plugin;

    public DiscordManager(BanSystem plugin) {
        this.plugin = plugin;
    }

    public void sendLog(String title, String message, String color) {
        if (!plugin.isFeatureEnabled("discord-logging")) return;

        String url = plugin.getConfig().getString("discord.webhook-url");
        if (url == null || url.isEmpty() || url.contains("your-webhook-url")) return;

        // Run this in the background so the server doesn't freeze
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("User-Agent", "Java-Webhook");
                connection.setDoOutput(true);

                // Simple JSON for a Discord Embed
                String json = "{\"embeds\":[{\"title\":\"" + title + "\",\"description\":\"" + message + "\",\"color\":" + color + "}]}";

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }
                connection.getResponseCode(); // Execute the request
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord log: " + e.getMessage());
            }
        });
    }
}