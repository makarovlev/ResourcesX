package com.gachicraft.resourcesx.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

public final class TreeInstance {
    private final int id;
    private final String name;
    private final Location center;
    private final TreeBlueprint blueprint;
    private final Map<String, TreeBlockDef> blocksByPosition = new HashMap<>();
    private TreeState state = TreeState.ALIVE;

    public TreeInstance(int id, String name, Location center, TreeBlueprint blueprint) {
        this.id = id; this.name = name; this.center = center; this.blueprint = blueprint;
        for (TreeBlockDef def : blueprint.blocks()) blocksByPosition.put(key(locationOf(def)), def);
    }

    public int id() { return id; }
    public String name() { return name; }
    public Location center() { return center; }
    public TreeBlueprint blueprint() { return blueprint; }
    public TreeState state() { return state; }
    public void setState(TreeState state) { this.state = state; }
    public Map<String, TreeBlockDef> blocksByPosition() { return blocksByPosition; }
    public TreeBlockDef blockAt(Location location) { return blocksByPosition.get(key(location)); }
    public Location locationOf(TreeBlockDef def) { return center.clone().add(def.dx(), def.dy(), def.dz()); }
    public int randomDropAmount() { return ThreadLocalRandom.current().nextInt(blueprint.amountMin(), blueprint.amountMax() + 1); }

    public void spawnAll() {
        for (TreeBlockDef def : blueprint.blocks()) locationOf(def).getBlock().setBlockData(def.blockData(), false);
        state = TreeState.ALIVE;
    }

    public void fell() {
        for (TreeBlockDef def : blueprint.blocks()) {
            if (!def.protect()) locationOf(def).getBlock().setType(Material.AIR, false);
        }
        state = TreeState.FELLED;
    }

    public void clearAll() {
        for (TreeBlockDef def : blueprint.blocks()) locationOf(def).getBlock().setType(Material.AIR, false);
        state = TreeState.DISABLED;
    }

    public void restoreBatched(JavaPlugin plugin, int blocksPerTick, Runnable done) {
        new org.bukkit.scheduler.BukkitRunnable() {
            private int index;
            @Override public void run() {
                int placed = 0;
                while (index < blueprint.blocks().size() && placed < blocksPerTick) {
                    TreeBlockDef def = blueprint.blocks().get(index++);
                    locationOf(def).getBlock().setBlockData(def.blockData(), false);
                    placed++;
                }
                if (index >= blueprint.blocks().size()) {
                    state = TreeState.ALIVE;
                    if (done != null) done.run();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public boolean canSpawnAtWorld() {
        for (TreeBlockDef def : blueprint.blocks()) {
            Block block = locationOf(def).getBlock();
            if (block.getType() != Material.AIR && !block.getBlockData().getAsString().equals(def.blockData().getAsString())) return false;
        }
        return true;
    }

    private String key(Location location) { return location.getWorld().getName() + ':' + location.getBlockX() + ':' + location.getBlockY() + ':' + location.getBlockZ(); }
}
