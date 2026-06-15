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

/**
 * 我的房间列表 GUI
 */
public class MyRoomsGUI {

    private static final String TITLE = "§8§l我的房间";

    public static void open(Player player, HotelsPlugin plugin) {
        List<HotelRoom> rooms = plugin.getRoomStorage().getRoomsByOwner(player.getUniqueId());

        int size = Math.min(54, Math.max(9, ((rooms.size() / 9) + 1) * 9));
        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        int slot = 0;
        for (HotelRoom room : rooms) {
            inv.setItem(slot++, createRoomItem(room));
        }

        player.openInventory(inv);
    }

    private static ItemStack createRoomItem(HotelRoom room) {
        Material material;
        switch (room.getStatus()) {
            case AVAILABLE:
                material = Material.GREEN_WOOL;
                break;
            case OCCUPIED:
                material = Material.RED_WOOL;
                break;
            case MAINTENANCE:
                material = Material.GRAY_WOOL;
                break;
            default:
                material = Material.WHITE_WOOL;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + room.getName());

            List<String> lore = new ArrayList<>();
            lore.add("§7ID: §f" + room.getId());
            lore.add("§7状态: " + getStatusDisplay(room.getStatus()));
            lore.add("§7价格: §f" + room.getPrice() + " 每晚");
            lore.add("§7区域: §f" + room.getVolume() + " 方块");
            if (room.isLocked()) {
                lore.add("§c🔒 已上锁");
            }
            if (room.hasPassword()) {
                lore.add("§c🔑 需要密码");
            }
            if (room.isOccupied()) {
                lore.add("§7客人: §f" + room.getCurrentGuestName());
            }
            lore.add("");
            lore.add("§e左键 §7管理房间");
            lore.add("§c右键 §7删除房间");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String getStatusDisplay(HotelRoom.RoomStatus status) {
        switch (status) {
            case AVAILABLE: return "§a空闲";
            case OCCUPIED: return "§c已入住";
            case MAINTENANCE: return "§7维护中";
            default: return "§7未知";
        }
    }
}
