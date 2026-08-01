package com.gachicraft.resourcesx.listener;

import com.gachicraft.resourcesx.mining.MiningManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public final class PlayerWorldChangeListener implements Listener {
    private final MiningManager mining;
    public PlayerWorldChangeListener(MiningManager mining) { this.mining = mining; }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) { mining.reset(event.getPlayer().getUniqueId(), true); }
}
