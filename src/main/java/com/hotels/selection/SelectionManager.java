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
package com.hotels.selection;

import com.hotels.model.PlayerSelection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 选区管理器 - 管理玩家的选点数据
 */
public class SelectionManager {

    private final Map<UUID, PlayerSelection> selections;

    public SelectionManager() {
        this.selections = new ConcurrentHashMap<>();
    }

    /**
     * 获取玩家的选区数据
     */
    public PlayerSelection getSelection(Player player) {
        return selections.computeIfAbsent(
                player.getUniqueId(),
                k -> new PlayerSelection(player.getUniqueId())
        );
    }

    /**
     * 设置第一个点
     */
    public void setPos1(Player player, Location loc) {
        PlayerSelection sel = getSelection(player);
        sel.setPos1(loc);
    }

    /**
     * 设置第二个点
     */
    public void setPos2(Player player, Location loc) {
        PlayerSelection sel = getSelection(player);
        sel.setPos2(loc);
    }

    /**
     * 设置传送点
     */
    public void setSpawnPoint(Player player, Location loc) {
        PlayerSelection sel = getSelection(player);
        sel.setSpawnPoint(loc);
    }

    /**
     * 清除玩家的选区
     */
    public void clearSelection(Player player) {
        selections.remove(player.getUniqueId());
    }

    /**
     * 玩家是否有完整的选区（两个点 + 传送点）
     */
    public boolean hasCompleteSelection(Player player) {
        PlayerSelection sel = selections.get(player.getUniqueId());
        return sel != null && sel.hasBothPositions() && sel.hasSpawnPoint();
    }
}
