package com.gachicraft.resourcesx.respawn;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class RespawnStorage {
    private final File file;

    public RespawnStorage(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "respawns.yml");
    }

    public Map<Integer, Long> load() {
        Map<Integer, Long> result = new HashMap<>();
        if (!file.exists()) return result;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("respawns");
        if (section == null) return result;
        for (String key : section.getKeys(false)) {
            try {
                result.put(Integer.parseInt(key), section.getLong(key + ".respawnAt"));
            } catch (NumberFormatException ignored) {
                // Ignore invalid persisted keys; config validation handles active ore points.
            }
        }
        return result;
    }

    public void save(Iterable<RespawnEntry> entries) {
        FileConfiguration config = new YamlConfiguration();
        for (RespawnEntry entry : entries) {
            String path = "respawns." + entry.orePoint().config().id();
            config.set(path + ".respawnAt", entry.respawnAtMillis());
            config.set(path + ".world", entry.orePoint().location().getWorld().getName());
            config.set(path + ".x", entry.orePoint().location().getBlockX());
            config.set(path + ".y", entry.orePoint().location().getBlockY());
            config.set(path + ".z", entry.orePoint().location().getBlockZ());
            config.set(path + ".ore", entry.orePoint().ore().name());
        }
        try {
            config.save(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save respawn storage", exception);
        }
    }
}
