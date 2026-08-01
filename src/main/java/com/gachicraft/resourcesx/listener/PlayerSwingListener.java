package com.gachicraft.resourcesx.listener;

import com.gachicraft.resourcesx.mining.MiningManager;
import com.gachicraft.resourcesx.util.RayTraceUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;

public final class PlayerSwingListener implements Listener {
    private final MiningManager mining;
    public PlayerSwingListener(MiningManager mining) { this.mining = mining; }
    @EventHandler(ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) { mining.handleSwing(event.getPlayer(), RayTraceUtil.targetBlock(event.getPlayer())); }
}
