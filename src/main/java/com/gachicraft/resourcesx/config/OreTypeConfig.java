package com.gachicraft.resourcesx.config;

public final class OreTypeConfig {
    private final int level;
    private final double health;
    private final int damage;

    public OreTypeConfig(int level, double health, int damage) {
        this.level = level;
        this.health = health;
        this.damage = damage;
    }

    public int level() { return level; }
    public double health() { return health; }
    public int damage() { return damage; }
}
