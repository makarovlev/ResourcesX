package com.gachicraft.resourcesx.tree;

import org.bukkit.block.data.BlockData;

public final class TreeBlockDef {
    private final int dx, dy, dz;
    private final BlockData blockData;
    private final boolean breakable;
    private final boolean protect;

    public TreeBlockDef(int dx, int dy, int dz, BlockData blockData, boolean breakable, boolean protect) {
        this.dx = dx; this.dy = dy; this.dz = dz; this.blockData = blockData; this.breakable = breakable; this.protect = protect;
    }

    public int dx() { return dx; }
    public int dy() { return dy; }
    public int dz() { return dz; }
    public BlockData blockData() { return blockData; }
    public boolean breakable() { return breakable; }
    public boolean protect() { return protect; }
}
