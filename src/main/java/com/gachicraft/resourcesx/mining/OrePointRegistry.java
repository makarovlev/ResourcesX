package com.gachicraft.resourcesx.mining;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Location;

public final class OrePointRegistry {
    private final Map<String, OrePoint> points = new HashMap<>();
    public void register(OrePoint point) { points.put(key(point.location()), point); }
    public Optional<OrePoint> get(Location location) { return Optional.ofNullable(points.get(key(location))); }
    public boolean contains(Location location) { return points.containsKey(key(location)); }
    private String key(Location location) { return location.getWorld().getName() + ':' + location.getBlockX() + ':' + location.getBlockY() + ':' + location.getBlockZ(); }
}
