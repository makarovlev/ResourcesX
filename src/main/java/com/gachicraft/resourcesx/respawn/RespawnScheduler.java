package com.gachicraft.resourcesx.respawn;

import com.gachicraft.resourcesx.config.ConfigManager;
import com.gachicraft.resourcesx.mining.OrePoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class RespawnScheduler {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final RespawnStorage storage;
    private final PriorityQueue<RespawnEntry> queue = new PriorityQueue<>();
    private BukkitTask task;

    public RespawnScheduler(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.storage = new RespawnStorage(plugin);
    }

    public void start() {
        restoreOnStartup();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void schedule(OrePoint point, int delaySeconds) {
        long respawnAt = System.currentTimeMillis() + delaySeconds * 1000L;
        point.setDepleted(true);
        queue.add(new RespawnEntry(point, respawnAt));
        persist();
    }

    public void shutdown() {
        if (task != null) task.cancel();
        persist();
    }

    private void restoreOnStartup() {
        Map<Integer, Long> saved = storage.load();
        long now = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> entry : saved.entrySet()) {
            OrePoint point = config.registry().getById(entry.getKey());
            if (point == null) continue;
            if (entry.getValue() <= now) {
                respawn(point);
            } else {
                point.setDepleted(true);
                point.location().getBlock().setType(config.replacementBlock(), false);
                queue.add(new RespawnEntry(point, entry.getValue()));
            }
        }
        persist();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        while (!queue.isEmpty() && queue.peek().respawnAtMillis() <= now) {
            respawn(queue.poll().orePoint());
            changed = true;
        }
        if (changed) persist();
    }

    private void respawn(OrePoint point) {
        point.location().getBlock().setType(point.ore(), false);
        point.setDepleted(false);
    }

    private void persist() {
        List<RespawnEntry> entries = new ArrayList<>(queue);
        storage.save(entries);
    }
}
