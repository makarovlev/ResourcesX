package com.gachicraft.resourcesx.listener;

import com.gachicraft.resourcesx.config.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

public final class BlockBreakGuardListener implements Listener {
    private final ConfigManager config;
    public BlockBreakGuardListener(ConfigManager config) { this.config = config; }
    @EventHandler(ignoreCancelled = true)
    public void onDamage(BlockDamageEvent event) { if (config.registry().contains(event.getBlock().getLocation())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) { if (config.registry().contains(event.getBlock().getLocation())) event.setCancelled(true); }
}
