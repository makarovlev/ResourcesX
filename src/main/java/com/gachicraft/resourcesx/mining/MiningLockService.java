package com.gachicraft.resourcesx.mining;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;

public final class MiningLockService {
    private final Map<String, UUID> locks = new HashMap<>();

    public boolean tryLock(String key, UUID owner) {
        UUID current = locks.get(key);
        if (current == null || current.equals(owner)) { locks.put(key, owner); return true; }
        return false;
    }

    public Optional<UUID> ownerOf(String key) { return Optional.ofNullable(locks.get(key)); }
    public void unlock(String key) { locks.remove(key); }

    public boolean tryLock(Location location, UUID owner) { return tryLock(locationKey(location), owner); }
    public Optional<UUID> ownerOf(Location location) { return ownerOf(locationKey(location)); }
    public void unlock(Location location) { unlock(locationKey(location)); }

    public static String oreKey(Location location) { return "ore:" + locationKey(location); }
    public static String treeKey(int treeId) { return "tree:" + treeId; }

    private static String locationKey(Location location) { return location.getWorld().getName() + ':' + location.getBlockX() + ':' + location.getBlockY() + ':' + location.getBlockZ(); }
}
