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
package com.hotels.listener;

import com.hotels.HotelsPlugin;
import com.hotels.model.HotelRoom;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * 房间守卫 - 只有入住的客人或房主才能进入房间区域
 */
public class RoomGuardListener implements Listener {

    private final HotelsPlugin plugin;

    public RoomGuardListener(HotelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // 有 bypass 权限的直接放行
        if (player.hasPermission("hotels.bypass") || player.hasPermission("hotels.admin")) {
            return;
        }

        Location to = event.getTo();
        if (to == null) return;

        // 检查玩家是否进入了某个房间的区域
        for (HotelRoom room : plugin.getRoomStorage().getAllRooms()) {
            if (!room.containsLocation(to)) continue;

            // 进入了房间区域，检查是否有权限
            // 房主可以进
            if (room.getOwner().equals(player.getUniqueId())) return;

            // 当前入住的客人可以进
            if (room.getCurrentGuest() != null && room.getCurrentGuest().equals(player.getUniqueId())) return;

            // 房间是空闲状态且没上锁 -> 允许进入（方便看房）
            if (room.isAvailable() && !room.isLocked()) return;

            // 其他情况：推出去
            Location from = event.getFrom();
            // 如果玩家本来就在区域内（from 也在区域内），不反复推
            if (room.containsLocation(from)) {
                // 已经在里面了，但没权限 -> 还是推出去
            }

            plugin.log(player, "尝试进入房间被拒绝: " + room.getName() + " (权限不足)");

            // 把玩家传送到房间入口外面（传送点附近偏移）
            Location spawn = new Location(
                    to.getWorld(),
                    room.getSpawnX() + 2, room.getSpawnY(), room.getSpawnZ() + 2,
                    room.getSpawnYaw(), room.getSpawnPitch()
            );
            player.teleport(spawn);
            player.sendMessage("§c你没有权限进入该房间");

            plugin.debug(player, "玩家被拒绝进入房间: " + player.getName() + " 尝试进入 " + room.getName());

            event.setCancelled(true);
            return;
        }
    }
}
