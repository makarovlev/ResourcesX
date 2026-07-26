package com.gachicraft.resourcesx.util;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class InventoryUtil {
    private InventoryUtil() { }
    public static boolean giveOrDrop(Player player, ItemStack item, Location dropLocation) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        leftovers.values().forEach(leftover -> dropLocation.getWorld().dropItemNaturally(dropLocation, leftover));
        return !leftovers.isEmpty();
    }
}
