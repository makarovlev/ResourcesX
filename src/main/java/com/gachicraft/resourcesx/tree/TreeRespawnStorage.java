package com.gachicraft.resourcesx.tree;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class TreeRespawnStorage {
    private final File file;
    public TreeRespawnStorage(JavaPlugin plugin) { this.file = new File(plugin.getDataFolder(), "tree_respawns.yml"); }
    public Map<Integer, Long> load() {
        Map<Integer, Long> result = new HashMap<>();
        if (!file.exists()) return result;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("respawns");
        if (section == null) return result;
        for (String key : section.getKeys(false)) {
            try { result.put(Integer.parseInt(key), section.getLong(key + ".respawnAt")); } catch (NumberFormatException ignored) { }
        }
        return result;
    }
    public void save(Iterable<TreeRespawnEntry> entries) {
        FileConfiguration config = new YamlConfiguration();
        for (TreeRespawnEntry entry : entries) config.set("respawns." + entry.tree().id() + ".respawnAt", entry.respawnAtMillis());
        try { config.save(file); } catch (IOException exception) { throw new IllegalStateException("Unable to save tree respawns", exception); }
    }
}
