package com.gachicraft.resourcesx.config;

import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public final class MessagesConfig {
    private FileConfiguration config;
    public void setConfig(FileConfiguration config) { this.config = config; }
    public String format(String key) { return format(key, Map.of()); }
    public String format(String key, Map<String, String> placeholders) {
        String text = config.getString(key, key);
        if (!key.equals("prefix") && !key.startsWith("mining.progress-bar")) {
            text = config.getString("prefix", "") + text;
        }
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace('%' + entry.getKey() + '%', entry.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
