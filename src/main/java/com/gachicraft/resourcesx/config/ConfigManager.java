package com.gachicraft.resourcesx.config;

import com.gachicraft.resourcesx.mining.OrePoint;
import com.gachicraft.resourcesx.mining.OrePointRegistry;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {
    private static final Set<Material> ALLOWED_ORES = Set.of(Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_ORE, Material.DIAMOND_ORE, Material.LAPIS_ORE, Material.COPPER_ORE, Material.EMERALD_ORE);
    private final JavaPlugin plugin;
    private final MessagesConfig messages = new MessagesConfig();
    private final Map<Material, OreTypeConfig> ores = new HashMap<>();
    private final Map<Material, PickaxeConfig> pickaxes = new HashMap<>();
    private OrePointRegistry registry = new OrePointRegistry();
    private Material replacementBlock;
    private int idleResetSeconds;
    private long hitCooldownMillis;
    private int barLength;
    private boolean crackAnimation;

    public ConfigManager(JavaPlugin plugin) { this.plugin = plugin; }

    public void reload() {
        plugin.reloadConfig();
        messages.setConfig(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml")));
        FileConfiguration config = plugin.getConfig();
        replacementBlock = Material.matchMaterial(config.getString("settings.replacement-block", "COBBLESTONE"));
        if (replacementBlock == null || !replacementBlock.isBlock()) replacementBlock = Material.COBBLESTONE;
        idleResetSeconds = Math.max(1, config.getInt("settings.idle-reset-seconds", 2));
        hitCooldownMillis = Math.max(1L, config.getLong("settings.hit-cooldown-millis", 833L));
        barLength = Math.max(1, config.getInt("settings.actionbar-bar-length", 10));
        crackAnimation = config.getBoolean("settings.use-crack-animation", true);
        loadOreTypes(config);
        loadPickaxes(config);
        loadOrePoints(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "ores.yml")));
    }

    private void loadOreTypes(FileConfiguration config) {
        ores.clear();
        ConfigurationSection section = config.getConfigurationSection("protectionOres");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material != null) ores.put(material, new OreTypeConfig(section.getInt(key + ".level"), section.getDouble(key + ".health"), section.getInt(key + ".damage")));
        }
    }

    private void loadPickaxes(FileConfiguration config) {
        pickaxes.clear();
        ConfigurationSection section = config.getConfigurationSection("forcePickaxes");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material != null) pickaxes.put(material, new PickaxeConfig(section.getInt(key + ".level"), section.getDouble(key + ".force")));
        }
    }

    private void loadOrePoints(FileConfiguration oresConfig) {
        OrePointRegistry next = new OrePointRegistry();
        Set<Integer> usedIds = new HashSet<>();
        for (String name : oresConfig.getKeys(false)) {
            int id = oresConfig.getInt(name + ".id");
            if (!usedIds.add(id)) { plugin.getLogger().warning(messages.format("config.duplicate-id", Map.of("id", String.valueOf(id), "name", name))); continue; }
            Material ore = Material.matchMaterial(oresConfig.getString(name + ".ore", ""));
            if (ore == null || !ALLOWED_ORES.contains(ore)) { plugin.getLogger().warning(messages.format("config.invalid-ore-type", Map.of("ore", String.valueOf(oresConfig.getString(name + ".ore")), "name", name))); continue; }
            if (!ores.containsKey(ore)) { plugin.getLogger().warning(messages.format("config.missing-protection-entry", Map.of("ore", ore.name()))); continue; }
            String world = oresConfig.getString(name + ".coordinates.world");
            if (Bukkit.getWorld(world) == null) { plugin.getLogger().warning("World '" + world + "' for ore point '" + name + "' is not loaded, point disabled."); continue; }
            Location location = new Location(Bukkit.getWorld(world), oresConfig.getInt(name + ".coordinates.x"), oresConfig.getInt(name + ".coordinates.y"), oresConfig.getInt(name + ".coordinates.z"));
            int[] counts = parseCounts(oresConfig.getString(name + ".count", "1"));
            OrePoint point = new OrePoint(new OrePointConfig(name, id, ore, location, Math.max(1, oresConfig.getInt(name + ".cooldown", 60)), counts));
            next.register(point);
            point.spawnBlock();
        }
        registry = next;
    }

    private int[] parseCounts(String raw) {
        String[] parts = raw.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) result[i] = Math.max(1, Integer.parseInt(parts[i].trim()));
        return result;
    }

    public MessagesConfig messages() { return messages; }
    public OrePointRegistry registry() { return registry; }
    public OreTypeConfig oreType(Material material) { return ores.get(material); }
    public PickaxeConfig pickaxe(Material material) { return pickaxes.get(material); }
    public Material replacementBlock() { return replacementBlock; }
    public int idleResetSeconds() { return idleResetSeconds; }
    public long hitCooldownMillis() { return hitCooldownMillis; }
    public int barLength() { return barLength; }
    public boolean crackAnimation() { return crackAnimation; }
}
