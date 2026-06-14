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

/**
 * 浏览房间 GUI - 显示所有可入住的房间，支持按标签筛选
 */
public class BrowseRoomsGUI {

    private static final String TITLE = "§8§l浏览房间";
    private static final String FILTER_TITLE = "§8§l按标签筛选";

    /**
     * 打开浏览房间（无筛选）
     */
    public static void open(Player player, HotelsPlugin plugin) {
        List<HotelRoom> rooms = plugin.getRoomStorage().getAvailableRooms();
        openWithRooms(player, rooms, plugin, null);
    }

    /**
     * 打开按标签筛选的浏览
     */
    public static void openByTag(Player player, String tag, HotelsPlugin plugin) {
        List<HotelRoom> allRooms = plugin.getRoomStorage().getAvailableRooms();
        List<HotelRoom> filtered = allRooms.stream()
                .filter(r -> r.hasTag(tag))
                .collect(Collectors.toList());
        openWithRooms(player, filtered, plugin, tag);
    }

    /**
     * 打开标签选择界面
     */
    public static void openTagFilter(Player player, HotelsPlugin plugin) {
        List<String> presetTags = plugin.getConfig().getStringList("room-tags");
        int size = Math.min(54, Math.max(9, ((presetTags.size() / 9) + 2) * 9));
        Inventory inv = Bukkit.createInventory(null, size, FILTER_TITLE);

        // 顶行边框
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);

        // 全部房间按钮
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

        // 返回
        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回主菜单"));

        player.openInventory(inv);
    }

    private static void openWithRooms(Player player, List<HotelRoom> rooms, HotelsPlugin plugin, String currentTag) {
        int size = Math.min(54, Math.max(9, ((rooms.size() / 9) + 2) * 9));
        if (size < 9) size = 9;

        String title = currentTag != null ? "§8§l浏览房间 §7- §e" + currentTag : TITLE;
        Inventory inv = Bukkit.createInventory(null, size, title);

        // 顶行 - 筛选按钮
        ItemStack filterItem = new ItemStack(Material.HOPPER);
        ItemMeta filterMeta = filterItem.getItemMeta();
        if (filterMeta != null) {
            filterMeta.setDisplayName("§e§l按标签筛选");
            filterMeta.setLore(java.util.Arrays.asList("§7点击选择标签"));
            filterItem.setItemMeta(filterMeta);
        }
        inv.setItem(4, filterItem);

        // 顶行边框
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        inv.setItem(4, filterItem);

        if (rooms.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§c暂无可用房间");
                empty.setItemMeta(meta);
            }
            inv.setItem(22, empty);
        } else {
            int slot = 9;
            for (HotelRoom room : rooms) {
                inv.setItem(slot++, createRoomItem(room, player));
            }
        }

        // 返回
        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回"));

        player.openInventory(inv);
    }

    private static ItemStack createRoomItem(HotelRoom room, Player viewer) {
        ItemStack item = new ItemStack(Material.OAK_DOOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + room.getName());

            List<String> lore = new ArrayList<>();
            lore.add("§7房主: §f" + room.getOwnerName());
            lore.add("§7价格: §f" + room.getPrice());
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
