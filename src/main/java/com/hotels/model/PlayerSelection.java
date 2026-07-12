/*
 * Hotels - 酒店房间管理插件
 * Copyright (C) 2024-2026 Hotels
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
