package com.gachicraft.resourcesx.tree;

public final class TreeRespawnEntry implements Comparable<TreeRespawnEntry> {
    private final TreeInstance tree;
    private final long respawnAtMillis;
    public TreeRespawnEntry(TreeInstance tree, long respawnAtMillis) { this.tree = tree; this.respawnAtMillis = respawnAtMillis; }
    public TreeInstance tree() { return tree; }
    public long respawnAtMillis() { return respawnAtMillis; }
    @Override public int compareTo(TreeRespawnEntry other) { return Long.compare(respawnAtMillis, other.respawnAtMillis); }
}
