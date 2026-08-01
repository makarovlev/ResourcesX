package com.gachicraft.resourcesx.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;

public final class TreeRegistry {
    private final Map<Integer, TreeInstance> byId = new HashMap<>();
    private final Map<String, TreeInstance> byPosition = new HashMap<>();
    private final Set<String> protectedPositions = new HashSet<>();

    public boolean register(TreeInstance tree) {
        for (String key : tree.blocksByPosition().keySet()) if (byPosition.containsKey(key)) return false;
        byId.put(tree.id(), tree);
        for (Map.Entry<String, TreeBlockDef> entry : tree.blocksByPosition().entrySet()) {
            byPosition.put(entry.getKey(), tree);
            if (entry.getValue().protect()) protectedPositions.add(entry.getKey());
        }
        return true;
    }

    public TreeInstance get(Location location) { return byPosition.get(key(location)); }
    public TreeInstance getById(int id) { return byId.get(id); }
    public Collection<TreeInstance> all() { return byId.values(); }
    public boolean contains(Location location) { return byPosition.containsKey(key(location)); }
    public boolean isProtected(Location location) { return protectedPositions.contains(key(location)); }

    public void unregister(TreeInstance tree) {
        byId.remove(tree.id());
        for (String key : tree.blocksByPosition().keySet()) {
            byPosition.remove(key);
            protectedPositions.remove(key);
        }
    }

    private String key(Location location) { return location.getWorld().getName() + ':' + location.getBlockX() + ':' + location.getBlockY() + ':' + location.getBlockZ(); }
}
