package com.gachicraft.resourcesx.config;

import com.gachicraft.resourcesx.mining.OrePoint;
import com.gachicraft.resourcesx.mining.OrePointRegistry;
import com.gachicraft.resourcesx.tree.BlueprintParser;
import com.gachicraft.resourcesx.tree.TreeBlockDef;
import com.gachicraft.resourcesx.tree.TreeBlueprint;
import com.gachicraft.resourcesx.tree.TreeInstance;
import com.gachicraft.resourcesx.tree.TreeProtectionConfig;
import com.gachicraft.resourcesx.tree.TreeRegistry;
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
    private final Map<Material, PickaxeConfig> axes = new HashMap<>();
    private final Map<String, TreeProtectionConfig> trees = new HashMap<>();
    private OrePointRegistry registry = new OrePointRegistry();
    private TreeRegistry treeRegistry = new TreeRegistry();
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
        loadAxes(config);
        loadTreeProtections(config);
        loadOrePoints(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "ores.yml")));
        loadTrees(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "trees.yml")));
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


    private void loadAxes(FileConfiguration config) {
        axes.clear();
        ConfigurationSection section = config.getConfigurationSection("forceAxes");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material != null) axes.put(material, new PickaxeConfig(section.getInt(key + ".level"), section.getDouble(key + ".force")));
        }
    }

    private void loadTreeProtections(FileConfiguration config) {
        trees.clear();
        ConfigurationSection section = config.getConfigurationSection("protectionTrees");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            trees.put(key, new TreeProtectionConfig(section.getInt(key + ".level"), section.getDouble(key + ".health"), section.getInt(key + ".damage")));
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


    private void loadTrees(FileConfiguration treesConfig) {
        TreeRegistry next = new TreeRegistry();
        Map<String, TreeBlueprint> blueprints = loadBlueprints(treesConfig);
        Set<Integer> usedIds = new HashSet<>();
        ConfigurationSection section = treesConfig.getConfigurationSection("trees");
        if (section == null) { treeRegistry = next; return; }
        for (String name : section.getKeys(false)) {
            int id = section.getInt(name + ".id");
            if (!usedIds.add(id)) { plugin.getLogger().warning("Duplicate tree id=" + id + " (" + name + ") disabled."); continue; }
            String blueprintName = section.getString(name + ".blueprint", "");
            TreeBlueprint blueprint = blueprints.get(blueprintName);
            if (blueprint == null) { plugin.getLogger().warning("Unknown tree blueprint '" + blueprintName + "' for tree '" + name + "', disabled."); continue; }
            String world = section.getString(name + ".center.world");
            if (Bukkit.getWorld(world) == null) { plugin.getLogger().warning("World '" + world + "' for tree '" + name + "' is not loaded, tree disabled."); continue; }
            Location center = new Location(Bukkit.getWorld(world), section.getInt(name + ".center.x"), section.getInt(name + ".center.y"), section.getInt(name + ".center.z"));
            TreeInstance tree = new TreeInstance(id, name, center, blueprint);
            if (conflictsWithOre(tree)) { plugin.getLogger().warning("Tree '" + name + "' conflicts with ore point coordinates, disabled."); continue; }
            if (!tree.canSpawnAtWorld()) { plugin.getLogger().warning("Tree '" + name + "' target area is occupied by another block, disabled."); continue; }
            if (!next.register(tree)) { plugin.getLogger().warning("Tree '" + name + "' overlaps another tree, disabled."); continue; }
            tree.spawnAll();
        }
        treeRegistry = next;
    }

    private Map<String, TreeBlueprint> loadBlueprints(FileConfiguration treesConfig) {
        Map<String, TreeBlueprint> result = new HashMap<>();
        BlueprintParser parser = new BlueprintParser(plugin);
        ConfigurationSection section = treesConfig.getConfigurationSection("blueprints");
        if (section == null) return result;
        for (String name : section.getKeys(false)) {
            TreeProtectionConfig protection = trees.get(name);
            if (protection == null) { plugin.getLogger().warning("Missing protectionTrees entry for tree blueprint '" + name + "', blueprint disabled."); continue; }
            ConfigurationSection build = section.getConfigurationSection(name + ".build");
            if (build == null) continue;
            java.util.List<TreeBlockDef> blocks = new java.util.ArrayList<>();
            for (String position : build.getKeys(false)) {
                TreeBlockDef def = parser.parse(name, position, build.getString(position, ""));
                if (def != null) blocks.add(def);
            }
            if (blocks.isEmpty()) { plugin.getLogger().warning("Tree blueprint '" + name + "' has no valid blocks, disabled."); continue; }
            int[] amount = parseAmount(section.getString(name + ".amount", "1"));
            result.put(name, new TreeBlueprint(name, protection.level(), protection.health(), protection.damage(), Math.max(1, section.getInt(name + ".cooldown", 60)), amount[0], amount[1], blocks));
        }
        return result;
    }

    private int[] parseAmount(String raw) {
        String[] parts = raw.split("-", 2);
        int min = Math.max(1, Integer.parseInt(parts[0].trim()));
        int max = parts.length == 1 ? min : Math.max(min, Integer.parseInt(parts[1].trim()));
        return new int[] { min, max };
    }

    private boolean conflictsWithOre(TreeInstance tree) {
        for (TreeBlockDef def : tree.blueprint().blocks()) if (registry.contains(tree.locationOf(def))) return true;
        return false;
    }

    private int[] parseCounts(String raw) {
        String[] parts = raw.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) result[i] = Math.max(1, Integer.parseInt(parts[i].trim()));
        return result;
    }

    public MessagesConfig messages() { return messages; }
    public OrePointRegistry registry() { return registry; }
    public TreeRegistry treeRegistry() { return treeRegistry; }
    public OreTypeConfig oreType(Material material) { return ores.get(material); }
    public PickaxeConfig pickaxe(Material material) { return pickaxes.get(material); }
    public PickaxeConfig axe(Material material) { return axes.get(material); }
    public Material replacementBlock() { return replacementBlock; }
    public int idleResetSeconds() { return idleResetSeconds; }
    public long hitCooldownMillis() { return hitCooldownMillis; }
    public int barLength() { return barLength; }
    public boolean crackAnimation() { return crackAnimation; }
}
