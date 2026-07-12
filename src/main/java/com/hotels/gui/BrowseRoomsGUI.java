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
package com.hotels.gui;

import com.hotels.HotelsPlugin;
import com.hotels.model.HotelRoom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BrowseRoomsGUI {

    public static final String GUI_NAME = "browse_rooms";
    private static final int PAGE_SIZE = 45;

    public static void open(Player player, HotelsPlugin plugin) {
        List<HotelRoom> rooms = plugin.getRoomStorage().getAvailableRooms();
        openWithRooms(player, rooms, plugin, null, 0);
    }

    public static void openByTag(Player player, String tag, HotelsPlugin plugin) {
        List<HotelRoom> allRooms = plugin.getRoomStorage().getAvailableRooms();
        List<HotelRoom> filtered = allRooms.stream()
                .filter(r -> r.hasTag(tag))
                .collect(Collectors.toList());
        openWithRooms(player, filtered, plugin, tag, 0);
    }

    public static void openTagFilter(Player player, HotelsPlugin plugin) {
        List<String> presetTags = plugin.getConfig().getStringList("room-tags");
        int size = Math.min(54, Math.max(9, ((presetTags.size() / 9) + 2) * 9));
        Inventory inv = Bukkit.createInventory(new GUIHolder("tag_filter"), size, "§8§l按标签筛选");

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);

        ItemStack allItem = new ItemStack(Material.COMPASS);
        ItemMeta allMeta = allItem.getItemMeta();
        if (allMeta != null) {
            allMeta.setDisplayName("§e§l全部房间");
            allMeta.setLore(java.util.Arrays.asList("§7显示所有可入住的房间"));
            allItem.setItemMeta(allMeta);
        }
        inv.setItem(4, allItem);

        int slot = 9;
        for (String tag : presetTags) {
            long count = plugin.getRoomStorage().getAvailableRooms().stream()
                    .filter(r -> r.hasTag(tag)).count();

            ItemStack item = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + tag);
                meta.setLore(java.util.Arrays.asList(
                        "§7当前 §e" + count + " §7个房间有此标签",
                        "",
                        "§e点击筛选"
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回主菜单"));

        player.openInventory(inv);
    }

    public static void openWithRooms(Player player, List<HotelRoom> rooms, HotelsPlugin plugin, String currentTag, int page) {
        int totalPages = (int) Math.ceil((double) rooms.size() / PAGE_SIZE);
        if (page < 0) page = 0;
        if (page >= totalPages) page = Math.max(0, totalPages - 1);

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, rooms.size());
        List<HotelRoom> pageRooms = rooms.subList(start, end);

        String tagDisplay = currentTag != null && !currentTag.equals("all") ? " §7- §e" + currentTag : "";
        String title = "§8§l🔍 浏览房间" + tagDisplay;
        Inventory inv = Bukkit.createInventory(new GUIHolder(GUI_NAME + ":" + (currentTag != null ? currentTag : "all") + ":" + page), 54, title);

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName("§8 ");
            border.setItemMeta(borderMeta);
        }

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
        }

        ItemStack titleItem = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta titleMeta = titleItem.getItemMeta();
        if (titleMeta != null) {
            titleMeta.setDisplayName("§e§l🔍 浏览房间");
            List<String> lore = new ArrayList<>();
            lore.add("§7共 §e" + rooms.size() + " §7个可入住房间");
            if (currentTag != null && !currentTag.equals("all")) {
                lore.add("§7标签筛选: §e" + currentTag);
            }
            titleMeta.setLore(lore);
            titleItem.setItemMeta(titleMeta);
        }
        inv.setItem(3, titleItem);

        ItemStack filterItem = new ItemStack(Material.HOPPER);
        ItemMeta filterMeta = filterItem.getItemMeta();
        if (filterMeta != null) {
            filterMeta.setDisplayName("§e§l⚙ 按标签筛选");
            filterMeta.setLore(java.util.Arrays.asList("§7点击选择标签"));
            filterItem.setItemMeta(filterMeta);
        }
        inv.setItem(5, filterItem);

        int slot = 9;
        if (rooms.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§c暂无可用房间");
                empty.setItemMeta(meta);
            }
            inv.setItem(22, empty);
        } else {
            for (HotelRoom room : pageRooms) {
                inv.setItem(slot++, createRoomItem(room, player));
            }
        }

        ItemStack back = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§c§l← 返回");
            backMeta.setLore(java.util.Arrays.asList("§7返回主菜单"));
            back.setItemMeta(backMeta);
        }
        inv.setItem(45, back);

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName("§7上一页");
                prev.setItemMeta(prevMeta);
            }
            inv.setItem(47, prev);
        }

        inv.setItem(49, createItem(Material.PAPER, "§7第 " + (page + 1) + " / " + Math.max(1, totalPages) + " 页"));

        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§7下一页");
                next.setItemMeta(nextMeta);
            }
            inv.setItem(51, next);
        }

        for (int i = 45; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, border);
            }
        }

        player.openInventory(inv);
    }

    private static ItemStack createRoomItem(HotelRoom room, Player viewer) {
        ItemStack item = new ItemStack(Material.OAK_DOOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + room.getName());

            List<String> lore = new ArrayList<>();
            lore.add("§7房主: §f" + room.getOwnerName());
            if (room.hasActiveDiscount()) {
                lore.add("§7价格: §m§f" + room.getPrice() + "§r §a§l¥" + room.getDiscountPrice());
                lore.add(room.getDiscountDisplay());
            } else {
                lore.add("§7价格: §f" + room.getPrice());
            }
            lore.add("§7标签: " + room.getTagsDisplay());
            lore.add("§7世界: §f" + room.getWorldName());
            if (room.hasPassword()) {
                lore.add("§c🔑 需要密码");
            }
            lore.add("");
            lore.add("§e左键 §7入住此房间");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(java.util.Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}