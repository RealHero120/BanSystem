package me.hero.bansystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MessageUtil {

    private final BanSystem plugin;

    // &a, &c, &f etc → Component
    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    public MessageUtil(BanSystem plugin) {
        this.plugin = plugin;
    }

    /** Raw string from messages.yml (no prefix, no color, no replacements) */
    public String getRaw(String path) {
        FileConfiguration cfg = plugin.getMessagesConfig();
        return cfg.getString(path, "&cMissing message: " + path);
    }

    /** Single-line message → Component */
    public Component component(String path, String... replacements) {
        String msg = getRaw(path);
        msg = applyPlaceholders(msg, replacements);
        msg = applyPrefix(msg);
        return SERIALIZER.deserialize(msg);
    }

    /**
     * Multi-line message (list in YAML) → Component joined with '\n'.
     */
    public Component componentFromList(String path, String... replacements) {
        FileConfiguration cfg = plugin.getMessagesConfig();
        List<String> lines = cfg.getStringList(path);

        if (lines == null || lines.isEmpty()) {
            // Fallback to single-line key, if present
            return component(path, replacements);
        }

        List<Component> components = new ArrayList<>();
        for (String line : lines) {
            String processed = applyPlaceholders(line, replacements);
            processed = applyPrefix(processed);
            components.add(SERIALIZER.deserialize(processed));
        }

        Component result = Component.empty();
        for (int i = 0; i < components.size(); i++) {
            result = result.append(components.get(i));
            if (i < components.size() - 1) {
                result = result.append(Component.newline());
            }
        }
        return result;
    }

    /** Send to any CommandSender as Component */
    public void send(CommandSender sender, String path, String... replacements) {
        sender.sendMessage(component(path, replacements));
    }

    /** Legacy-formatted string (if you ever need a String again) */
    public String format(String path, String... replacements) {
        return LegacyComponentSerializer.legacySection().serialize(component(path, replacements));
    }

    // ===== Internal helpers =====

    private String applyPrefix(String msg) {
        String prefix = plugin.getMessagesConfig()
                .getString("prefix", "&8[&cBanSystem&8] &7");
        return msg.replace("%prefix%", prefix);
    }

    private String applyPlaceholders(String msg, String... replacements) {
        if (replacements == null || replacements.length == 0) {
            return msg;
        }

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            String key = "{" + replacements[i] + "}";
            String value = replacements[i + 1];
            msg = msg.replace(key, value);
        }
        return msg;
    }
}
