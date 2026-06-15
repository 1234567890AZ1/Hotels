package com.hotels.listener;

import com.hotels.HotelsPlugin;
import com.hotels.gui.*;
import com.hotels.model.HotelRoom;
import com.hotels.model.RoomCollection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI 点击事件监听器
 */
public class GUIListener implements Listener {

    private final HotelsPlugin plugin;

    public GUIListener(HotelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        if (title.equals("§8§l✦ 酒店系统")) {
            event.setCancelled(true);
            handleMainMenuClick(player, event.getSlot());
        } else if (title.equals("§8§l我的房间")) {
            event.setCancelled(true);
            handleMyRoomsClick(player, event);
        } else if (title.equals("§8§l浏览房间") ||
                   title.startsWith("§8§l浏览房间")) {
            event.setCancelled(true);
            handleBrowseRoomsClick(player, event);
        } else if (title.equals("§8§l按标签筛选")) {
            event.setCancelled(true);
            handleTagFilterClick(player, event);
        } else if (title.equals("§8§l房间管理")) {
            event.setCancelled(true);
            handleRoomManageClick(player, event);
        } else if (title.equals("§8§l✦ 房间排行榜")) {
            event.setCancelled(true);
            handleRankingClick(player, event);
        } else if (title.equals("§8§l✦ 选择标签")) {
            event.setCancelled(true);
            handleTagSelectClick(player, event);
        } else if (title.equals("§8§l✦ 酒店合集") ||
                   title.equals("§8§l✦ 浏览酒店") ||
                   title.equals("§8§l✦ 我的酒店") ||
                   title.startsWith("§8§l✦ ")) {
            // 合集相关 GUI 统一处理
            event.setCancelled(true);
            handleCollectionClick(player, event);
        }
    }

    private void handleMainMenuClick(Player player, int slot) {
        switch (slot) {
            case 11: // 我的房间
                MyRoomsGUI.open(player, plugin);
                break;
            case 13: // 浏览房间
                BrowseRoomsGUI.open(player, plugin);
                break;
            case 15: // 创建新房间
                if (!player.hasPermission("hotels.create")) {
                    player.sendMessage("§c你没有权限创建房间");
                    return;
                }
                player.closeInventory();
                player.sendMessage("§e=== 创建房间 ===");
                player.sendMessage("§71. 手持木斧 §e//wand §7选择区域的两个对角点");
                player.sendMessage("§72. 站在入口位置输入 §e/ht setspawn §7设置传送点");
                player.sendMessage("§73. 输入 §e/ht create <房间名> §7创建房间");
                player.sendMessage("§74. 创建后可用 §e/ht manage <ID> §7管理房间");
                break;
            case 29: // 酒店合集
                CollectionGUI.openManage(player);
                break;
            case 31: // 帮助说明
                sendHelp(player);
                break;
            case 33: // 房间排行榜
                MainMenuGUI.openRanking(player);
                break;
        }
    }

    private void handleRankingClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        int size = event.getInventory().getSize();

        // 返回按钮（最后一格）
        if (slot == size - 1) {
            MainMenuGUI.open(player);
        }
    }

    private void handleCollectionClick(Player player, InventoryClickEvent event) {
        String title = event.getView().getTitle();
        int slot = event.getSlot();
        int size = event.getInventory().getSize();
        ItemStack item = event.getCurrentItem();

        // 返回按钮（最后一格）
        if (slot == size - 1) {
            MainMenuGUI.open(player);
            return;
        }

        // 合集管理主界面（27格固定布局，返回在 Slot 22）
        if (title.equals("§8§l✦ 酒店合集")) {
            if (slot == 22) {
                MainMenuGUI.open(player);
                return;
            }
            // 合集管理主界面
            switch (slot) {
                case 11: // 浏览所有酒店
                    CollectionGUI.openBrowseAll(player, plugin);
                    break;
                case 13: // 创建新酒店
                    player.closeInventory();
                    player.sendMessage("§e请输入新酒店合集的名称:");
                    plugin.getChatInputHandler().expectInput(player, "createcollection");
                    break;
                case 15: // 我的酒店
                    CollectionGUI.openMyCollections(player, plugin);
                    break;
            }
        } else if (title.equals("§8§l✦ 浏览酒店")) {
            // 浏览所有合集 - 点击合集查看房间列表
            if (item == null || !item.hasItemMeta()) return;
            String colName = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
            for (RoomCollection col : plugin.getRoomStorage().getAllCollections()) {
                if (col.getName().equals(colName)) {
                    CollectionGUI.openCollectionRooms(player, col, plugin);
                    return;
                }
            }
        } else if (title.equals("§8§l✦ 我的酒店")) {
            // 我的合集列表
            if (item == null || !item.hasItemMeta()) return;
            String colName = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
            for (RoomCollection col : plugin.getRoomStorage().getCollectionsByOwner(player.getUniqueId())) {
                if (col.getName().equals(colName)) {
                    if (event.isLeftClick()) {
                        // 左键 - 管理合集
                        CollectionGUI.openManageCollection(player, col, plugin);
                    } else if (event.isRightClick()) {
                        // 右键 - 删除合集
                        player.closeInventory();
                        player.sendMessage("§c确认删除合集 §e" + col.getName() + "§c？在聊天框输入 §e确认 §c或 §e取消");
                        plugin.getChatInputHandler().expectInput(player, "deletecollection:" + col.getId());
                    }
                    return;
                }
            }
        } else if (title.startsWith("§8§l✦ ")) {
            // 管理合集内房间 或 浏览合集房间列表 或 管理员管理
            // 从 Slot 4 获取合集信息
            ItemStack infoItem = event.getInventory().getItem(4);
            if (infoItem == null || !infoItem.hasItemMeta()) return;
            String infoName = org.bukkit.ChatColor.stripColor(infoItem.getItemMeta().getDisplayName());

            // 判断是否是管理员管理界面
            if (title.startsWith("§8§l✦ 管理员:")) {
                // 管理员管理 - 点击玩家切换管理员
                if (item == null || !item.hasItemMeta()) return;
                String playerName = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
                Player target = Bukkit.getPlayer(playerName);
                if (target == null) return;

                // 查找合集
                String colName = title.substring("§8§l✦ 管理员: ".length());
                for (RoomCollection col : plugin.getRoomStorage().getAllCollections()) {
                    if (col.getName().equals(colName)) {
                        // 只有房主可以管理管理员
                        if (!col.getOwner().equals(player.getUniqueId())) {
                            player.sendMessage("§c只有房主可以管理管理员");
                            return;
                        }
                        if (col.getOwner().equals(target.getUniqueId())) {
                            player.sendMessage("§c不能操作房主");
                            return;
                        }
                        if (col.getAdmins().contains(target.getUniqueId().toString())) {
                            col.removeAdmin(target.getUniqueId());
                            player.sendMessage("§c已移除管理员 §e" + target.getName());
                        } else {
                            col.addAdmin(target.getUniqueId());
                            player.sendMessage("§a已添加 §e" + target.getName() + " §a为管理员");
                        }
                        plugin.getRoomStorage().saveCollection(col);
                        CollectionGUI.openAdminManage(player, col, plugin);
                        return;
                    }
                }
                return;
            }

            // 查找合集
            for (RoomCollection col : plugin.getRoomStorage().getAllCollections()) {
                if (col.getName().equals(infoName)) {
                    if (item == null || !item.hasItemMeta()) return;

                    // 检查是否点击了管理员管理按钮
                    String itemName = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
                    if (itemName.contains("管理员管理")) {
                        // 只有房主或管理员可以管理管理员
                        if (!col.canManage(player.getUniqueId())) {
                            player.sendMessage("§c你没有权限管理此合集");
                            return;
                        }
                        CollectionGUI.openAdminManage(player, col, plugin);
                        return;
                    }

                    // 检查点击的是不是房间
                    String roomName = itemName;
                    for (HotelRoom room : plugin.getRoomStorage().getRoomsByOwner(player.getUniqueId())) {
                        if (room.getName().equals(roomName)) {
                            // 检查是否有权限管理此合集
                            if (!col.canManage(player.getUniqueId())) {
                                player.sendMessage("§c你没有权限管理此合集");
                                return;
                            }
                            // 切换加入/移出
                            if (col.getRoomIds().contains(room.getId())) {
                                col.removeRoom(room.getId());
                                player.sendMessage("§c已从合集移出房间 §e" + room.getName());
                            } else {
                                col.addRoom(room.getId());
                                player.sendMessage("§a已添加房间 §e" + room.getName() + " §a到合集");
                            }
                            plugin.getRoomStorage().saveCollection(col);
                            CollectionGUI.openManageCollection(player, col, plugin);
                            return;
                        }
                    }

                    // 不是自己的房间 - 尝试入住（浏览模式）
                    for (HotelRoom room : plugin.getRoomStorage().getCollectionRooms(col.getId())) {
                        if (room.getName().equals(roomName)) {
                            player.closeInventory();
                            plugin.getCheckinHandler().attemptCheckin(player, room);
                            return;
                        }
                    }
                    return;
                }
            }
        }
    }

    private void handleMyRoomsClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.startsWith("§e")) return;

        // 从显示名称提取房间名
        String roomName = ChatColor.stripColor(displayName);

        // 查找房间
        for (HotelRoom room : plugin.getRoomStorage().getRoomsByOwner(player.getUniqueId())) {
            if (room.getName().equals(roomName)) {
                if (event.isLeftClick()) {
                    // 左键 - 管理房间
                    RoomManageGUI.open(player, room, plugin);
                } else if (event.isRightClick()) {
                    // 右键 - 删除房间
                    player.closeInventory();
                    player.sendMessage("§c确认删除房间 §e" + room.getName() + "§c？在聊天框输入 §e确认 §c或 §e取消");
                    plugin.getChatInputHandler().expectInput(player, "deleteroom:" + room.getId());
                }
                return;
            }
        }
    }

    private void handleBrowseRoomsClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        int size = event.getInventory().getSize();
        ItemStack item = event.getCurrentItem();

        // 返回按钮
        if (slot == size - 1) {
            MainMenuGUI.open(player);
            return;
        }

        // 筛选按钮（Slot 4）
        if (slot == 4) {
            BrowseRoomsGUI.openTagFilter(player, plugin);
            return;
        }

        if (item == null || !item.hasItemMeta()) return;

        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.startsWith("§e")) return;

        String roomName = ChatColor.stripColor(displayName);

        // 查找房间并尝试入住
        for (HotelRoom room : plugin.getRoomStorage().getAvailableRooms()) {
            if (room.getName().equals(roomName)) {
                player.closeInventory();
                plugin.getCheckinHandler().attemptCheckin(player, room);
                return;
            }
        }
    }

    private void handleTagFilterClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        int size = event.getInventory().getSize();
        ItemStack item = event.getCurrentItem();

        // 返回按钮
        if (slot == size - 1) {
            MainMenuGUI.open(player);
            return;
        }

        // 全部房间按钮（Slot 4）
        if (slot == 4) {
            BrowseRoomsGUI.open(player, plugin);
            return;
        }

        if (item == null || !item.hasItemMeta()) return;

        String tagName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        // 按标签筛选
        BrowseRoomsGUI.openByTag(player, tagName, plugin);
    }

    private void handleRoomManageClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();

        // 从当前 GUI 获取房间信息（Slot 4 是房间信息物品）
        ItemStack infoItem = event.getInventory().getItem(4);
        if (infoItem == null || !infoItem.hasItemMeta()) return;

        String infoName = ChatColor.stripColor(infoItem.getItemMeta().getDisplayName());

        // 查找房间
        HotelRoom targetRoom = null;
        for (HotelRoom room : plugin.getRoomStorage().getAllRooms()) {
            if (room.getName().equals(infoName) && room.getOwner().equals(player.getUniqueId())) {
                targetRoom = room;
                break;
            }
        }

        if (targetRoom == null) {
            player.sendMessage("§c房间数据异常");
            player.closeInventory();
            return;
        }

        switch (slot) {
            case 10: // 设置价格
                player.closeInventory();
                player.sendMessage("§e请输入新价格（数字）:");
                plugin.getChatInputHandler().expectInput(player, "setprice:" + targetRoom.getId());
                break;
            case 11: // 设置标签
                TagSelectGUI.open(player, targetRoom, plugin);
                break;
            case 12: // 设置密码
                player.closeInventory();
                if (targetRoom.hasPassword()) {
                    targetRoom.setPassword(null);
                    plugin.getRoomStorage().saveRoom(targetRoom);
                    player.sendMessage("§a已清除房间密码");
                } else {
                    player.sendMessage("§e请输入房间密码:");
                    plugin.getChatInputHandler().expectInput(player, "setpassword:" + targetRoom.getId());
                }
                break;
            case 13: // 切换锁定
                targetRoom.setLocked(!targetRoom.isLocked());
                plugin.getRoomStorage().saveRoom(targetRoom);
                player.sendMessage("§a房间已" + (targetRoom.isLocked() ? "锁定" : "解锁"));
                RoomManageGUI.open(player, targetRoom, plugin);
                break;
            case 14: // 切换状态
                switch (targetRoom.getStatus()) {
                    case AVAILABLE:
                        targetRoom.setStatus(HotelRoom.RoomStatus.MAINTENANCE);
                        break;
                    case MAINTENANCE:
                        targetRoom.setStatus(HotelRoom.RoomStatus.AVAILABLE);
                        break;
                    case OCCUPIED:
                        player.sendMessage("§c房间已入住，无法切换状态");
                        return;
                }
                plugin.getRoomStorage().saveRoom(targetRoom);
                player.sendMessage("§a房间状态已更新");
                RoomManageGUI.open(player, targetRoom, plugin);
                break;
            case 21: // 传送
                Location loc = new Location(
                        Bukkit.getWorld(targetRoom.getWorldName()),
                        targetRoom.getSpawnX(), targetRoom.getSpawnY(), targetRoom.getSpawnZ(),
                        targetRoom.getSpawnYaw(), targetRoom.getSpawnPitch()
                );
                player.teleport(loc);
                player.sendMessage("§a已传送到房间");
                break;
            case 23: // 踢出客人
                if (targetRoom.isOccupied()) {
                    Player guest = plugin.getServer().getPlayer(targetRoom.getCurrentGuest());
                    if (guest != null && guest.isOnline()) {
                        guest.sendMessage("§c你被房主从房间 " + targetRoom.getName() + " 中踢出");
                    }
                    targetRoom.setCurrentGuest(null);
                    targetRoom.setCurrentGuestName(null);
                    targetRoom.setStatus(HotelRoom.RoomStatus.AVAILABLE);
                    targetRoom.setCheckinTime(0);
                    plugin.getRoomStorage().saveRoom(targetRoom);
                    player.sendMessage("§a已踢出客人");
                    RoomManageGUI.open(player, targetRoom, plugin);
                }
                break;
            case 26: // 返回
                MyRoomsGUI.open(player, plugin);
                break;
        }
    }

    private void handleTagSelectClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        int size = event.getInventory().getSize();
        ItemStack item = event.getCurrentItem();

        // 返回按钮
        if (slot == size - 1) {
            // 从 Slot 4 获取房间信息
            ItemStack infoItem = event.getInventory().getItem(4);
            if (infoItem == null || !infoItem.hasItemMeta()) return;
            String roomName = org.bukkit.ChatColor.stripColor(infoItem.getItemMeta().getDisplayName());

            for (HotelRoom room : plugin.getRoomStorage().getAllRooms()) {
                if (room.getName().equals(roomName) && room.getOwner().equals(player.getUniqueId())) {
                    RoomManageGUI.open(player, room, plugin);
                    return;
                }
            }
            return;
        }

        if (item == null || !item.hasItemMeta()) return;

        // 获取点击的标签名
        String tagName = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());

        // 从 Slot 4 获取房间信息
        ItemStack infoItem = event.getInventory().getItem(4);
        if (infoItem == null || !infoItem.hasItemMeta()) return;
        String roomName = org.bukkit.ChatColor.stripColor(infoItem.getItemMeta().getDisplayName());

        for (HotelRoom room : plugin.getRoomStorage().getAllRooms()) {
            if (room.getName().equals(roomName) && room.getOwner().equals(player.getUniqueId())) {
                if (room.hasTag(tagName)) {
                    room.removeTag(tagName);
                    player.sendMessage("§c已移除标签 §e" + tagName);
                } else {
                    if (room.getTags().size() >= 3) {
                        player.sendMessage("§c标签已达上限（最多3个）");
                    } else {
                        room.addTag(tagName);
                        player.sendMessage("§a已添加标签 §e" + tagName);
                    }
                }
                plugin.getRoomStorage().saveRoom(room);
                TagSelectGUI.open(player, room, plugin);
                return;
            }
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== 酒店系统帮助 ===");
        player.sendMessage("§e/ht §7- 打开酒店菜单");
        player.sendMessage("§e/ht create <名称> §7- 创建房间（需先选点）");
        player.sendMessage("§e/ht manage <ID> §7- 管理房间");
        player.sendMessage("§e/ht remove <ID> §7- 删除房间");
        player.sendMessage("§e/ht list §7- 查看你的房间列表");
        player.sendMessage("§e/ht setspawn §7- 设置房间传送点");
        player.sendMessage("§e//wand §7- 获取选区工具（木斧）");
        player.sendMessage("§e/ht admin §7- 管理命令");
    }
}
