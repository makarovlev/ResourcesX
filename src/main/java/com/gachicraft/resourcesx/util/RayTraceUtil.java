package com.gachicraft.resourcesx.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public final class RayTraceUtil {
    private RayTraceUtil() { }
    public static Block targetBlock(Player player) {
        RayTraceResult result = player.rayTraceBlocks(5.0, FluidCollisionMode.NEVER);
        return result == null ? null : result.getHitBlock();
    }
}
