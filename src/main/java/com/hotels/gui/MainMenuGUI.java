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
import java.util.stream.Collectors;

/**
 * 主菜单 GUI - 深色系风格
 */
public class MainMenuGUI {

    private static final String TITLE = "§8§l✦ 酒店系统";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE);

        HotelsPlugin plugin = HotelsPlugin.getInstance();

        // ===== 装饰边框 =====
        // 顶行 - 黑紫渐变
        ItemStack borderTop = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, borderTop);
        }
        // 标题位置
        inv.setItem(4, createItem(Material.ENDER_PEARL, "§5§l✦ 酒 店 系 统", "§8欢迎使用"));

        // 底行 - 深紫色
        ItemStack borderBottom = createItem(Material.PURPLE_STAINED_GLASS_PANE, "§8 ");
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, borderBottom);
        }

        // 左右边框 - 黑色
        ItemStack borderSide = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 9; i < 36; i += 9) {
            inv.setItem(i, borderSide);
            inv.setItem(i + 8, borderSide);
        }

        // ===== 功能按钮 =====

        // 我的房间 - Slot 11（第2行第3列）
        inv.setItem(11, createItem(Material.OAK_DOOR, "§d§l我的房间",
                "§7查看和管理你拥有的房间",
                "§7你共有: §f" + countPlayerRooms(player) + " §7个房间",
                "",
                "§8▸ §d点击查看"));

        // 浏览房间 - Slot 13（第2行第5列）
        inv.setItem(13, createItem(Material.COMPASS, "§d§l浏览房间",
                "§7查看所有可入住的房间",
                "§7当前空闲: §f" + countAvailableRooms() + " §7间",
                "",
                "§8▸ §d点击浏览"));

        // 创建新房间 - Slot 15（第2行第7列）
        inv.setItem(15, createItem(Material.EMERALD_BLOCK, "§a§l创建新房间",
                "§7使用木斧选择区域后创建",
                "§7① 获取木斧选区",
                "§7② 设置传送点",
                "§7③ 命名创建",
                "",
                "§8▸ §a点击开始"));

        // 酒店合集 - Slot 29（第4行第3列）
        inv.setItem(29, createItem(Material.CHEST, "§d§l酒店合集",
                "§7创建和管理房间合集",
                "§7浏览所有玩家创建的酒店",
                "",
                "§8▸ §d点击进入"));

        // 帮助说明 - Slot 31（第4行第5列）
        inv.setItem(31, createItem(Material.BOOK, "§b§l帮助说明",
                "§7查看酒店系统使用指南",
                "§7命令列表 & 玩法说明",
                "",
                "§8▸ §b点击查看"));

        // 房间排行榜 - Slot 33（第4行第7列）
        inv.setItem(33, createItem(Material.GOLD_BLOCK, "§6§l房间排行榜",
                "§7查看最大的房间排名",
                "",
                "§8▸ §6点击查看"));

        player.openInventory(inv);
    }

    /**
     * 统计玩家拥有的房间数
     */
    private static int countPlayerRooms(Player player) {
        HotelsPlugin plugin = HotelsPlugin.getInstance();
        if (plugin == null) return 0;
        return plugin.getRoomStorage().getRoomsByOwner(player.getUniqueId()).size();
    }

    /**
     * 统计空闲房间数
     */
    private static int countAvailableRooms() {
        HotelsPlugin plugin = HotelsPlugin.getInstance();
        if (plugin == null) return 0;
        return plugin.getRoomStorage().getAvailableRooms().size();
    }

    /**
     * 打开排行榜 GUI
     */
    public static void openRanking(Player player) {
        HotelsPlugin plugin = HotelsPlugin.getInstance();
        if (plugin == null) {
            player.sendMessage("§c插件未就绪");
            return;
        }

        List<HotelRoom> allRooms = new ArrayList<>(plugin.getRoomStorage().getAllRooms());
        allRooms.sort((a, b) -> Long.compare(b.getVolume(), a.getVolume()));

        List<HotelRoom> top = allRooms.stream().limit(10).collect(Collectors.toList());

        int size = Math.min(54, Math.max(9, ((top.size() / 9) + 2) * 9));
        Inventory inv = Bukkit.createInventory(null, size, "§8§l✦ 房间排行榜");

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
        }

        int slot = 9;
        int rank = 1;
        for (HotelRoom room : top) {
            Material icon;
            String rankColor;
            if (rank == 1) {
                icon = Material.GOLD_BLOCK;
                rankColor = "§6";
            } else if (rank == 2) {
                icon = Material.IRON_BLOCK;
                rankColor = "§7";
            } else if (rank == 3) {
                icon = Material.COPPER_BLOCK;
                rankColor = "§c";
            } else {
                icon = Material.STONE;
                rankColor = "§8";
            }

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(rankColor + "§l#" + rank + " §f" + room.getName());
                meta.setLore(Arrays.asList(
                        "§7房主: §f" + room.getOwnerName(),
                        "§7大小: §f" + room.getVolume() + " §7方块",
                        "§7状态: " + getStatusDisplay(room.getStatus()),
                        "§7世界: §f" + room.getWorldName()
                ));
                item.setItemMeta(meta);
            }

            inv.setItem(slot++, item);
            rank++;
        }

        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回主菜单"));

        player.openInventory(inv);
    }

    private static String getStatusDisplay(HotelRoom.RoomStatus status) {
        switch (status) {
            case AVAILABLE: return "§a空闲";
            case OCCUPIED: return "§c已入住";
            case MAINTENANCE: return "§7维护中";
            default: return "§7未知";
        }
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
