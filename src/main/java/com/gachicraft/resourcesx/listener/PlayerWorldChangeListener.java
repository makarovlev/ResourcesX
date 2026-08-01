package com.gachicraft.resourcesx.listener;

import com.gachicraft.resourcesx.mining.MiningManager;
import com.gachicraft.resourcesx.tree.TreeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public final class PlayerWorldChangeListener implements Listener {
    private final MiningManager mining;
    private final TreeManager trees;
    public PlayerWorldChangeListener(MiningManager mining, TreeManager trees) { this.mining = mining; this.trees = trees; }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) { mining.reset(event.getPlayer().getUniqueId(), true); trees.reset(event.getPlayer().getUniqueId(), true); }
}
