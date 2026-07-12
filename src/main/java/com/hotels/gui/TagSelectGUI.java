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
import java.util.Arrays;
import java.util.List;

public class TagSelectGUI {

    public static final String GUI_NAME = "tag_select";

    public static void open(Player player, HotelRoom room, HotelsPlugin plugin) {
        List<String> presetTags = plugin.getConfig().getStringList("room-tags");
        List<String> currentTags = room.getTags();

        int size = Math.min(54, Math.max(9, ((presetTags.size() / 9) + 2) * 9));
        Inventory inv = Bukkit.createInventory(new GUIHolder(GUI_NAME, room), size, "§8§l✦ 选择标签");

        ItemStack infoItem = new ItemStack(Material.NAME_TAG);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6" + room.getName());
            infoMeta.setLore(Arrays.asList(
                    "§7当前标签: " + room.getTagsDisplay(),
                    "§7还可添加: §e" + (3 - currentTags.size()) + " §7个",
                    "",
                    "§e点击标签切换"
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        inv.setItem(4, infoItem);

        int slot = 9;
        for (String tag : presetTags) {
            boolean hasTag = currentTags.contains(tag);
            boolean canAdd = currentTags.size() < 3;

            Material mat = hasTag ? Material.LIME_DYE : (canAdd ? Material.GRAY_DYE : Material.BARRIER);
            String status = hasTag ? "§a已选择" : (canAdd ? "§7点击添加" : "§c已达上限");

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName((hasTag ? "§a" : "§7") + "§l" + tag);
                meta.setLore(Arrays.asList(
                        "§7状态: " + status,
                        "",
                        hasTag ? "§c点击移除" : (canAdd ? "§a点击添加" : "§c已达上限")
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回房间管理"));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}