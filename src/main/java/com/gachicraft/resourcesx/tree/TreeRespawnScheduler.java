package com.gachicraft.resourcesx.tree;

import com.gachicraft.resourcesx.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class TreeRespawnScheduler {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final TreeRespawnStorage storage;
    private final PriorityQueue<TreeRespawnEntry> queue = new PriorityQueue<>();
    private BukkitTask task;

    public TreeRespawnScheduler(JavaPlugin plugin, ConfigManager config) { this.plugin = plugin; this.config = config; this.storage = new TreeRespawnStorage(plugin); }
    public void start() { restore(); task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L); }
    public void reload() { queue.clear(); restore(); }
    public void schedule(TreeInstance tree) { tree.fell(); queue.add(new TreeRespawnEntry(tree, System.currentTimeMillis() + tree.blueprint().cooldownSeconds() * 1000L)); persist(); }
    public void cancel(TreeInstance tree) { queue.removeIf(entry -> entry.tree().id() == tree.id()); persist(); }
    public void shutdown() { if (task != null) task.cancel(); persist(); }

    private void restore() {
        Map<Integer, Long> saved = storage.load();
        long now = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> entry : saved.entrySet()) {
            TreeInstance tree = config.treeRegistry().getById(entry.getKey());
            if (tree == null) continue;
            if (entry.getValue() <= now) restoreTree(tree); else { tree.fell(); queue.add(new TreeRespawnEntry(tree, entry.getValue())); }
        }
        persist();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        while (!queue.isEmpty() && queue.peek().respawnAtMillis() <= now) { restoreTree(queue.poll().tree()); changed = true; }
        if (changed) persist();
    }

    private void restoreTree(TreeInstance tree) {
        if (!tree.canSpawnAtWorld()) {
            plugin.getLogger().warning("Tree id=" + tree.id() + " cannot respawn because its area is occupied; retrying in 10 seconds.");
            queue.add(new TreeRespawnEntry(tree, System.currentTimeMillis() + 10_000L));
            return;
        }
        tree.restoreBatched(plugin, 10, this::persist);
    }

    private void persist() { List<TreeRespawnEntry> entries = new ArrayList<>(queue); storage.save(entries); }
}
