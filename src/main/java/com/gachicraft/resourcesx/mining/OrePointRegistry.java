package com.gachicraft.resourcesx.mining;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Location;

public final class OrePointRegistry {
    private final Map<String, OrePoint> pointsByLocation = new HashMap<>();
    private final Map<Integer, OrePoint> pointsById = new HashMap<>();

    public void register(OrePoint point) {
        pointsByLocation.put(key(point.location()), point);
        pointsById.put(point.config().id(), point);
    }

    public Optional<OrePoint> get(Location location) { return Optional.ofNullable(pointsByLocation.get(key(location))); }
    public OrePoint getById(int id) { return pointsById.get(id); }
    public Collection<OrePoint> all() { return pointsById.values(); }
    public boolean contains(Location location) { return pointsByLocation.containsKey(key(location)); }

    private String key(Location location) {
        return location.getWorld().getName() + ':' + location.getBlockX() + ':' + location.getBlockY() + ':' + location.getBlockZ();
    }
}
