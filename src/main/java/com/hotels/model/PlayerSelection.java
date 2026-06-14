package com.hotels.model;

import org.bukkit.Location;

import java.util.UUID;

/**
 * 玩家选区数据（类似 WorldEdit 的选点）
 */
public class PlayerSelection {

    private final UUID playerUUID;
    private Location pos1;
    private Location pos2;
    private Location spawnPoint;

    public PlayerSelection(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() { return playerUUID; }

    public Location getPos1() { return pos1; }
    public void setPos1(Location pos1) { this.pos1 = pos1; }

    public Location getPos2() { return pos2; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }

    public Location getSpawnPoint() { return spawnPoint; }
    public void setSpawnPoint(Location spawnPoint) { this.spawnPoint = spawnPoint; }

    public boolean hasBothPositions() {
        return pos1 != null && pos2 != null;
    }

    public boolean hasSpawnPoint() {
        return spawnPoint != null;
    }

    public void clear() {
        pos1 = null;
        pos2 = null;
        spawnPoint = null;
    }
}
