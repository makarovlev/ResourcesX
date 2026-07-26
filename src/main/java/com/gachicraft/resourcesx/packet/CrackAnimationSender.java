package com.gachicraft.resourcesx.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.world.BlockPosition;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import com.gachicraft.resourcesx.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class CrackAnimationSender {
    private final ConfigManager config;
    public CrackAnimationSender(ConfigManager config) { this.config = config; }
    public void sendStage(Player player, Location location, double percent) {
        if (!config.crackAnimation()) return;
        int stage = Math.min(9, Math.max(0, (int) Math.floor(percent / 10.0)));
        send(player, location, stage);
    }
    public void clear(Player player, Location location) { send(player, location, -1); }
    private void send(Player player, Location location, int stage) {
        int entityId = Math.abs((player.getUniqueId().toString() + location.toString()).hashCode());
        WrapperPlayServerBlockBreakAnimation packet = new WrapperPlayServerBlockBreakAnimation(entityId, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), (byte) stage);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}
