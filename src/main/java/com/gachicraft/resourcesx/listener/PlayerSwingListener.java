package com.gachicraft.resourcesx.listener;

import com.gachicraft.resourcesx.mining.MiningManager;
import com.gachicraft.resourcesx.tree.TreeManager;
import com.gachicraft.resourcesx.util.RayTraceUtil;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;

public final class PlayerSwingListener implements Listener {
    private final TreeManager trees;
    private final MiningManager mining;

    public PlayerSwingListener(TreeManager trees, MiningManager mining) { this.trees = trees; this.mining = mining; }

    @EventHandler(ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) {
        Block target = RayTraceUtil.targetBlock(event.getPlayer());
        if (!trees.handleSwing(event.getPlayer(), target)) mining.handleSwing(event.getPlayer(), target);
    }
}
