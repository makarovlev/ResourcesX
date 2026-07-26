package com.gachicraft.resourcesx.mining;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;

public final class MiningLockService {
    private final Map<String, UUID> locks = new HashMap<>();
    public boolean tryLock(Location location, UUID owner) { UUID current = locks.get(key(location)); if (current == null || current.equals(owner)) { locks.put(key(location), owner); return true; } return false; }
    public Optional<UUID> ownerOf(Location location) { return Optional.ofNullable(locks.get(key(location))); }
    public void unlock(Location location) { locks.remove(key(location)); }
    private String key(Location location) { return location.getWorld().getName() + ':' + location.getBlockX() + ':' + location.getBlockY() + ':' + location.getBlockZ(); }
}
