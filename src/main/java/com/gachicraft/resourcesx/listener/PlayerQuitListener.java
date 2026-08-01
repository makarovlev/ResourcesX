package com.gachicraft.resourcesx.listener;

import com.gachicraft.resourcesx.mining.MiningManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {
    private final MiningManager mining;
    public PlayerQuitListener(MiningManager mining) { this.mining = mining; }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) { mining.reset(event.getPlayer().getUniqueId(), true); }
}
