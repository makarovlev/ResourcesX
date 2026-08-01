package com.gachicraft.resourcesx.tree;

import java.util.List;

public final class TreeBlueprint {
    private final String name;
    private final int level;
    private final double health;
    private final int axeDamage;
    private final int cooldownSeconds;
    private final int amountMin;
    private final int amountMax;
    private final List<TreeBlockDef> blocks;

    public TreeBlueprint(String name, int level, double health, int axeDamage, int cooldownSeconds, int amountMin, int amountMax, List<TreeBlockDef> blocks) {
        this.name = name; this.level = level; this.health = health; this.axeDamage = axeDamage; this.cooldownSeconds = cooldownSeconds; this.amountMin = amountMin; this.amountMax = amountMax; this.blocks = blocks;
    }

    public String name() { return name; }
    public int level() { return level; }
    public double health() { return health; }
    public int axeDamage() { return axeDamage; }
    public int cooldownSeconds() { return cooldownSeconds; }
    public int amountMin() { return amountMin; }
    public int amountMax() { return amountMax; }
    public List<TreeBlockDef> blocks() { return blocks; }
}
