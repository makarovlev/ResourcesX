package com.gachicraft.resourcesx.respawn;

import com.gachicraft.resourcesx.mining.OrePoint;

public final class RespawnEntry implements Comparable<RespawnEntry> {
    private final OrePoint orePoint;
    private final long respawnAtMillis;

    public RespawnEntry(OrePoint orePoint, long respawnAtMillis) {
        this.orePoint = orePoint;
        this.respawnAtMillis = respawnAtMillis;
    }

    public OrePoint orePoint() { return orePoint; }
    public long respawnAtMillis() { return respawnAtMillis; }

    @Override
    public int compareTo(RespawnEntry other) {
        return Long.compare(respawnAtMillis, other.respawnAtMillis);
    }
}
