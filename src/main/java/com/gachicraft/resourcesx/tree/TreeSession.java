package com.gachicraft.resourcesx.tree;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

public final class TreeSession {
    private final java.util.UUID playerId;
    private final TreeInstance tree;
    private double damage;
    private long lastHitMillis;
    private Location crackLocation;
    private BukkitTask idleTask;

    public TreeSession(java.util.UUID playerId, TreeInstance tree) { this.playerId = playerId; this.tree = tree; }
    public java.util.UUID playerId() { return playerId; }
    public TreeInstance tree() { return tree; }
    public double damage() { return damage; }
    public void addDamage(double amount) { damage += amount; }
    public long lastHitMillis() { return lastHitMillis; }
    public void setLastHitMillis(long lastHitMillis) { this.lastHitMillis = lastHitMillis; }
    public Location crackLocation() { return crackLocation; }
    public void setCrackLocation(Location crackLocation) { this.crackLocation = crackLocation; }
    public BukkitTask idleTask() { return idleTask; }
    public void setIdleTask(BukkitTask idleTask) { this.idleTask = idleTask; }
}
