package com.gachicraft.resourcesx.mining;

import java.util.UUID;
import org.bukkit.scheduler.BukkitTask;

public final class MiningSession {
    private final UUID playerId;
    private final OrePoint orePoint;
    private double damage;
    private long lastHitMillis;
    private BukkitTask idleTask;

    public MiningSession(UUID playerId, OrePoint orePoint) {
        this.playerId = playerId;
        this.orePoint = orePoint;
    }

    public UUID playerId() { return playerId; }
    public OrePoint orePoint() { return orePoint; }
    public double damage() { return damage; }
    public void addDamage(double amount) { damage += amount; }
    public long lastHitMillis() { return lastHitMillis; }
    public void setLastHitMillis(long lastHitMillis) { this.lastHitMillis = lastHitMillis; }
    public BukkitTask idleTask() { return idleTask; }
    public void setIdleTask(BukkitTask idleTask) { this.idleTask = idleTask; }
}
