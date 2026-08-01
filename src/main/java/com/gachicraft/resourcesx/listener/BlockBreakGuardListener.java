package com.gachicraft.resourcesx.listener;

import com.gachicraft.resourcesx.config.ConfigManager;
import java.util.Iterator;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class BlockBreakGuardListener implements Listener {
    private final ConfigManager config;
    public BlockBreakGuardListener(ConfigManager config) { this.config = config; }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(BlockDamageEvent event) { if (managed(event.getBlock())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) { if (managed(event.getBlock())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) { removeProtected(event.blockList().iterator()); }
    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) { removeProtected(event.blockList().iterator()); }
    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) { if (event.getBlocks().stream().anyMatch(block -> config.treeRegistry().isProtected(block.getLocation()))) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) { if (event.getBlocks().stream().anyMatch(block -> config.treeRegistry().isProtected(block.getLocation()))) event.setCancelled(true); }

    private boolean managed(Block block) { return config.registry().contains(block.getLocation()) || config.treeRegistry().contains(block.getLocation()); }
    private void removeProtected(Iterator<Block> blocks) { while (blocks.hasNext()) if (config.treeRegistry().isProtected(blocks.next().getLocation())) blocks.remove(); }
}
