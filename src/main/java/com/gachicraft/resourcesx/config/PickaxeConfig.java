package com.gachicraft.resourcesx.config;

public final class PickaxeConfig {
    private final int level;
    private final double force;

    public PickaxeConfig(int level, double force) {
        this.level = level;
        this.force = force;
    }

    public int level() { return level; }
    public double force() { return force; }
}
