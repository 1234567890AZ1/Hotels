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
import com.hotels.selection.SelectionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * 选区工具监听器 - 木斧左键/右键选点
 */
public class SelectionListener implements Listener {

    private final HotelsPlugin plugin;

    public SelectionListener(HotelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 检查是否手持木斧
        if (player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE) {
            return;
        }

        // 检查是否有 hotels.create 权限
        if (!player.hasPermission("hotels.create") && !player.hasPermission("hotels.admin")) {
            return;
        }

        // 检查是否在选区模式（通过检查物品显示名称或直接检查权限）
        // 这里简单处理：只要拿着木斧且有权限就进入选区模式
        SelectionManager selectionManager = plugin.getSelectionManager();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            // 左键 - 设置 Pos1
            event.setCancelled(true);
            Location loc = event.getAction() == Action.LEFT_CLICK_BLOCK
                    ? event.getClickedBlock().getLocation()
                    : player.getLocation();

            selectionManager.setPos1(player, loc);
            plugin.log(player, "设置选区第1点: (" + (int)loc.getX() + ", " + (int)loc.getY() + ", " + (int)loc.getZ() + ")");
            player.sendMessage(String.format(
                    "§a已设置第 1 点: §e(%.0f, %.0f, %.0f)",
                    loc.getX(), loc.getY(), loc.getZ()
            ));

        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            // 右键 - 设置 Pos2
            event.setCancelled(true);
            Location loc = event.getAction() == Action.RIGHT_CLICK_BLOCK
                    ? event.getClickedBlock().getLocation()
                    : player.getLocation();

            selectionManager.setPos2(player, loc);
            plugin.log(player, "设置选区第2点: (" + (int)loc.getX() + ", " + (int)loc.getY() + ", " + (int)loc.getZ() + ")");
            player.sendMessage(String.format(
                    "§a已设置第 2 点: §e(%.0f, %.0f, %.0f)",
                    loc.getX(), loc.getY(), loc.getZ()
            ));

            // 如果两个点都选好了，显示区域信息
            if (selectionManager.getSelection(player).hasBothPositions()) {
                Location p1 = selectionManager.getSelection(player).getPos1();
                Location p2 = selectionManager.getSelection(player).getPos2();
                long dx = Math.abs((long) Math.ceil(p1.getX()) - (long) Math.ceil(p2.getX())) + 1;
                long dy = Math.abs((long) Math.ceil(p1.getY()) - (long) Math.ceil(p2.getY())) + 1;
                long dz = Math.abs((long) Math.ceil(p1.getZ()) - (long) Math.ceil(p2.getZ())) + 1;
                long volume = dx * dy * dz;
                player.sendMessage("§7区域大小: §e" + dx + " × " + dy + " × " + dz + " §7(§e" + volume + " §7方块)");
                player.sendMessage("§7站在传送点位置，输入 §e/ht create <名称> §7创建房间");
            }
        }
    }
}
