package com.gachicraft.resourcesx.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlueprintParser {
    private final JavaPlugin plugin;

    public BlueprintParser(JavaPlugin plugin) { this.plugin = plugin; }

    public TreeBlockDef parse(String blueprintName, String position, String raw) {
        int[] rel = parseRelative(blueprintName, position);
        if (rel == null) return null;
        try {
            String materialPart = raw;
            String statesPart = null;
            int bracket = raw.indexOf('[');
            if (bracket >= 0 && raw.endsWith("]")) {
                materialPart = raw.substring(0, bracket);
                statesPart = raw.substring(bracket + 1, raw.length() - 1);
            }

            Material material = Material.matchMaterial(materialPart.trim().toUpperCase(Locale.ROOT));
            if (material == null || !material.isBlock()) {
                plugin.getLogger().warning("Invalid tree block material '" + materialPart + "' in blueprint '" + blueprintName + "' at " + position);
                return null;
            }

            boolean breakable = false;
            boolean protect = false;
            List<String> realStates = new ArrayList<>();
            if (statesPart != null && !statesPart.isBlank()) {
                for (String token : statesPart.split(",")) {
                    String trimmed = token.trim();
                    String[] kv = trimmed.split("=", 2);
                    if (kv.length == 2 && kv[0].equalsIgnoreCase("break")) breakable = Boolean.parseBoolean(kv[1]);
                    else if (kv.length == 2 && kv[0].equalsIgnoreCase("protect")) protect = Boolean.parseBoolean(kv[1]);
                    else realStates.add(trimmed);
                }
            }

            String blockDataString = material.name().toLowerCase(Locale.ROOT);
            if (!realStates.isEmpty()) blockDataString += "[" + String.join(",", realStates) + "]";
            BlockData data = Bukkit.createBlockData(blockDataString);
            return new TreeBlockDef(rel[0], rel[1], rel[2], data, breakable, protect);
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Invalid tree block data '" + raw + "' in blueprint '" + blueprintName + "' at " + position + ": " + exception.getMessage());
            return null;
        }
    }

    private int[] parseRelative(String blueprintName, String position) {
        String[] parts = position.split(",");
        if (parts.length != 3) {
            plugin.getLogger().warning("Invalid relative tree position '" + position + "' in blueprint '" + blueprintName + "'");
            return null;
        }
        try {
            return new int[] { Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()) };
        } catch (NumberFormatException exception) {
            plugin.getLogger().warning("Invalid relative tree position '" + position + "' in blueprint '" + blueprintName + "'");
            return null;
        }
    }
}
