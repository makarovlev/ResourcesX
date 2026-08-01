package com.gachicraft.resourcesx.tree;

public final class TreeProtectionConfig {
    private final int level;
    private final double health;
    private final int damage;

    public TreeProtectionConfig(int level, double health, int damage) {
        this.level = level;
        this.health = health;
        this.damage = damage;
    }

    public int level() { return level; }
    public double health() { return health; }
    public int damage() { return damage; }
}
