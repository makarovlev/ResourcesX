package com.gachicraft.resourcesx.config;

import org.bukkit.Location;
import org.bukkit.Material;

public final class OrePointConfig {
    private final String name;
    private final int id;
    private final Material ore;
    private final Location location;
    private final int cooldownSeconds;
    private final int[] counts;

    public OrePointConfig(String name, int id, Material ore, Location location, int cooldownSeconds, int[] counts) {
        this.name = name;
        this.id = id;
        this.ore = ore;
        this.location = location;
        this.cooldownSeconds = cooldownSeconds;
        this.counts = counts;
    }

    public String name() { return name; }
    public int id() { return id; }
    public Material ore() { return ore; }
    public Location location() { return location; }
    public int cooldownSeconds() { return cooldownSeconds; }
    public int[] counts() { return counts; }
}
