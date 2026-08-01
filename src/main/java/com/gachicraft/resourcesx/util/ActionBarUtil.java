package com.gachicraft.resourcesx.util;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class ActionBarUtil {
    private ActionBarUtil() { }
    public static void send(Player player, String message) { player.sendActionBar(Component.text(message)); }
    public static String bar(double percent, int length) {
        int filled = (int) Math.floor((Math.max(0, Math.min(100, percent)) / 100.0) * length);
        return "§a" + "■".repeat(filled) + "§7" + "□".repeat(length - filled);
    }
}
