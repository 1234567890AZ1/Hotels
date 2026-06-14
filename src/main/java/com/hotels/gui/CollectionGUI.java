package com.hotels.gui;

import com.hotels.HotelsPlugin;
import com.hotels.model.HotelRoom;
import com.hotels.model.RoomCollection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 合集 GUI
 */
public class CollectionGUI {

    private static final String TITLE = "§8§l✦ 酒店合集";

    /**
     * 合集管理主界面
     */
    public static void openManage(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // 边框
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 18; i < 27; i++) inv.setItem(i, border);

        // 浏览所有酒店 - Slot 11
        inv.setItem(11, createItem(Material.ENDER_CHEST, "§d§l◈ 浏览所有酒店",
                "§7查看所有玩家创建的酒店合集",
                "",
                "§8▸ §d点击浏览"));

        // 创建新酒店 - Slot 13
        inv.setItem(13, createItem(Material.CHEST, "§a§l◈ 创建新酒店",
                "§7创建一个新的房间合集",
                "§7创建后可以将自己的房间加入",
                "",
                "§8▸ §a点击创建"));

        // 我的酒店 - Slot 15
        inv.setItem(15, createItem(Material.BOOKSHELF, "§e§l◈ 我的酒店",
                "§7查看和管理你创建的酒店合集",
                "",
                "§8▸ §e点击查看"));

        // 返回 - Slot 22
        inv.setItem(22, createItem(Material.ARROW, "§7§l返回", "§8返回主菜单"));

        player.openInventory(inv);
    }

    /**
     * 浏览所有合集
     */
    public static void openBrowseAll(Player player, HotelsPlugin plugin) {
        List<RoomCollection> allCols = new ArrayList<>(plugin.getRoomStorage().getAllCollections());
        int size = Math.min(54, Math.max(9, ((allCols.size() / 9) + 2) * 9));
        Inventory inv = Bukkit.createInventory(null, size, "§8§l✦ 浏览酒店");

        // 顶行边框
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);

        int slot = 9;
        for (RoomCollection col : allCols) {
            List<HotelRoom> rooms = plugin.getRoomStorage().getCollectionRooms(col.getId());
            long available = rooms.stream().filter(r -> r.isAvailable() && !r.isLocked()).count();

            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + col.getName());
                List<String> lore = new ArrayList<>();
                if (col.getDescription() != null) {
                    lore.add("§7" + col.getDescription());
                }
                lore.add("§7房主: §f" + col.getOwnerName());
                lore.add("§7房间: §f" + col.getRoomCount() + " §7间 (空闲 §a" + available + "§7)");
                lore.add("§7时长: " + col.getDurationDisplay());
                lore.add("");
                lore.add("§e左键 §7查看房间列表");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        // 返回
        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回"));

        player.openInventory(inv);
    }

    /**
     * 我的合集列表
     */
    public static void openMyCollections(Player player, HotelsPlugin plugin) {
        List<RoomCollection> myCols = plugin.getRoomStorage().getCollectionsByOwner(player.getUniqueId());
        int size = Math.min(54, Math.max(9, ((myCols.size() / 9) + 2) * 9));
        Inventory inv = Bukkit.createInventory(null, size, "§8§l✦ 我的酒店");

        // 顶行边框
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);

        int slot = 9;
        for (RoomCollection col : myCols) {
            List<HotelRoom> rooms = plugin.getRoomStorage().getCollectionRooms(col.getId());

            ItemStack item = new ItemStack(Material.BOOKSHELF);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + col.getName());
                List<String> lore = new ArrayList<>();
                if (col.getDescription() != null) {
                    lore.add("§7" + col.getDescription());
                }
                lore.add("§7房间: §f" + col.getRoomCount() + " §7间");
                lore.add("");
                lore.add("§e左键 §7管理合集");
                lore.add("§c右键 §7删除合集");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        // 返回
        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回"));

        player.openInventory(inv);
    }

    /**
     * 管理单个合集（添加/移除房间）
     */
    public static void openManageCollection(Player player, RoomCollection col, HotelsPlugin plugin) {
        List<HotelRoom> myRooms = plugin.getRoomStorage().getRoomsByOwner(player.getUniqueId());
        List<HotelRoom> inCol = plugin.getRoomStorage().getCollectionRooms(col.getId());

        int size = Math.min(54, Math.max(9, ((myRooms.size() / 9) + 2) * 9));
        Inventory inv = Bukkit.createInventory(null, size, "§8§l✦ " + col.getName());

        // 顶行 - 合集信息
        ItemStack infoItem = new ItemStack(Material.CHEST);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6" + col.getName());
            infoMeta.setLore(Arrays.asList(
                    "§7房间: §f" + inCol.size() + " / " + myRooms.size(),
                    "§7管理员: §f" + col.getAdminCount() + " 人",
                    "§e点击房间添加/移除"
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // 顶行边框
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        inv.setItem(4, infoItem); // 覆盖回信息

        // 管理员管理按钮 - 最后一行的第4列
        int bottomSlot = size - 3;
        if (bottomSlot > 0) {
            inv.setItem(bottomSlot, createItem(Material.PLAYER_HEAD, "§d§l◈ 管理员管理",
                    "§7添加或移除合集管理员",
                    "§7当前 " + col.getAdminCount() + " 位管理员",
                    "",
                    "§8▸ §d点击管理"));
        }

        int slot = 9;
        for (HotelRoom room : myRooms) {
            boolean isInCol = col.getRoomIds().contains(room.getId());

            Material mat = isInCol ? Material.GREEN_WOOL : Material.RED_WOOL;
            String status = isInCol ? "§a已加入" : "§c未加入";

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + room.getName());
                meta.setLore(Arrays.asList(
                        "§7ID: §f" + room.getId(),
                        "§7状态: " + status,
                        "",
                        "§e点击" + (isInCol ? "移出" : "加入") + "合集"
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        // 返回
        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回"));

        player.openInventory(inv);
    }

    /**
     * 查看合集内的房间列表（浏览模式）
     */
    public static void openCollectionRooms(Player player, RoomCollection col, HotelsPlugin plugin) {
        List<HotelRoom> rooms = plugin.getRoomStorage().getCollectionRooms(col.getId());
        int size = Math.min(54, Math.max(9, ((rooms.size() / 9) + 2) * 9));
        Inventory inv = Bukkit.createInventory(null, size, "§8§l✦ " + col.getName());

        // 顶行 - 合集信息
        ItemStack infoItem = new ItemStack(Material.CHEST);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6" + col.getName());
            infoMeta.setLore(Arrays.asList(
                    "§7房主: §f" + col.getOwnerName(),
                    "§7房间: §f" + rooms.size() + " §7间"
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // 顶行边框
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        inv.setItem(4, infoItem);

        int slot = 9;
        for (HotelRoom room : rooms) {
            Material mat;
            switch (room.getStatus()) {
                case AVAILABLE: mat = Material.GREEN_WOOL; break;
                case OCCUPIED: mat = Material.RED_WOOL; break;
                default: mat = Material.GRAY_WOOL;
            }

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + room.getName());
                meta.setLore(Arrays.asList(
                        "§7房主: §f" + room.getOwnerName(),
                        "§7价格: §f" + room.getPrice(),
                        "§7状态: " + getStatusDisplay(room.getStatus()),
                        "",
                        "§e左键 §7入住"
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        // 返回
        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回"));

        player.openInventory(inv);
    }

    /**
     * 管理员管理界面
     */
    public static void openAdminManage(Player player, RoomCollection col, HotelsPlugin plugin) {
        // 获取所有在线玩家
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int size = Math.min(54, Math.max(9, ((onlinePlayers.size() / 9) + 2) * 9));
        Inventory inv = Bukkit.createInventory(null, size, "§8§l✦ 管理员: " + col.getName());

        // 顶行 - 信息
        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6管理员管理");
            infoMeta.setLore(Arrays.asList(
                    "§7房主: §f" + col.getOwnerName(),
                    "§7管理员: §f" + col.getAdminCount() + " 人",
                    "§e点击在线玩家添加/移除管理员"
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // 顶行边框
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        inv.setItem(4, infoItem);

        int slot = 9;
        for (Player online : onlinePlayers) {
            boolean isOwner = col.getOwner().equals(online.getUniqueId());
            boolean isAdmin = col.getAdmins().contains(online.getUniqueId().toString());

            Material mat;
            String status;
            if (isOwner) {
                mat = Material.GOLD_BLOCK;
                status = "§6房主";
            } else if (isAdmin) {
                mat = Material.EMERALD_BLOCK;
                status = "§a管理员";
            } else {
                mat = Material.STONE;
                status = "§7普通";
            }

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + online.getName());
                meta.setLore(Arrays.asList(
                        "§7状态: " + status,
                        "",
                        isOwner ? "§7房主不可操作" :
                        (isAdmin ? "§c点击移除管理员" : "§a点击添加为管理员")
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        // 返回
        inv.setItem(size - 1, createItem(Material.ARROW, "§7§l返回", "§8返回"));

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
