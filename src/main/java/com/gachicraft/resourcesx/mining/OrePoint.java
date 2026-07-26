package com.gachicraft.resourcesx.mining;

import com.gachicraft.resourcesx.config.OrePointConfig;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;

public final class OrePoint {
    private final OrePointConfig config;
    private boolean depleted;
    public OrePoint(OrePointConfig config) { this.config = config; }
    public OrePointConfig config() { return config; }
    public Location location() { return config.location(); }
    public Material ore() { return config.ore(); }
    public boolean depleted() { return depleted; }
    public void setDepleted(boolean depleted) { this.depleted = depleted; }
    public int randomDropCount() {
        int[] counts = config.counts();
        return counts[ThreadLocalRandom.current().nextInt(counts.length)];
    }
}
