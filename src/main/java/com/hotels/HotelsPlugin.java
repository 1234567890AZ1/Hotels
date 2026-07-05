package com.hotels;

import com.hotels.command.HotelsCommand;
import com.hotels.listener.ChatInputHandler;
import com.hotels.listener.GUIListener;
import com.hotels.listener.RoomGuardListener;
import com.hotels.listener.SelectionListener;
import com.hotels.model.HotelRoom;
import com.hotels.model.RoomCollection;
import com.hotels.selection.SelectionManager;
import com.hotels.storage.RoomStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Hotels - 酒店房间管理插件
 */
public class HotelsPlugin extends JavaPlugin {

    private static HotelsPlugin instance;

    private RoomStorage roomStorage;
    private SelectionManager selectionManager;
    private EconomyManager economyManager;
    private CheckinHandler checkinHandler;
    private ChatInputHandler chatInputHandler;

    @Override
    public void onEnable() {
        instance = this;

        // 保存默认配置
        saveDefaultConfig();

        // 初始化管理器
        this.roomStorage = new RoomStorage(this);
        this.selectionManager = new SelectionManager();
        this.economyManager = new EconomyManager(this);
        this.checkinHandler = new CheckinHandler(this);
        this.chatInputHandler = new ChatInputHandler(this);

        // 加载数据
        roomStorage.loadAll();

        // 检查超时入住（重启后恢复定时任务）
        checkOverdueCheckins();

        // 注册监听器
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new SelectionListener(this), this);
        getServer().getPluginManager().registerEvents(chatInputHandler, this);
        getServer().getPluginManager().registerEvents(new RoomGuardListener(this), this);

        // 注册命令
        HotelsCommand hotelsCommand = new HotelsCommand(this);
        getCommand("hotels").setExecutor(hotelsCommand);
        getCommand("hotels").setTabCompleter(hotelsCommand);

        getLogger().info("Hotels 已启用 - 酒店房间管理系统");
        getLogger().info("已加载 " + roomStorage.getRoomCount() + " 个房间");
    }

    @Override
    public void onDisable() {
        if (roomStorage != null) {
            roomStorage.saveAll();
        }
        getLogger().info("Hotels 已禁用");
    }

    public static HotelsPlugin getInstance() {
        return instance;
    }

    public RoomStorage getRoomStorage() { return roomStorage; }
    public SelectionManager getSelectionManager() { return selectionManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public CheckinHandler getCheckinHandler() { return checkinHandler; }
    public ChatInputHandler getChatInputHandler() { return chatInputHandler; }

    /**
     * 检查所有已入住的房间，如果超时则自动退房，未超时则重新注册定时任务
     */
    private void checkOverdueCheckins() {
        long now = System.currentTimeMillis();
        int checked = 0;
        int expired = 0;

        for (HotelRoom room : roomStorage.getAllRooms()) {
            if (!room.isOccupied() || room.getCurrentGuest() == null) continue;
            checked++;

            // 获取实际时长
            int duration = room.getDurationMinutes();
            if (duration == -1) {
                // 从合集获取
                for (RoomCollection col : roomStorage.getAllCollections()) {
                    if (col.getRoomIds().contains(room.getId())) {
                        duration = col.getDurationMinutes();
                        break;
                    }
                }
                if (duration == -1) duration = 0;
            }

            if (duration <= 0) continue; // 不限时

            long checkinTime = room.getCheckinTime();
            long expireTime = checkinTime + (duration * 60 * 1000L);

            if (now >= expireTime) {
                // 已超时，自动退房
                Player guest = Bukkit.getPlayer(room.getCurrentGuest());
                if (guest != null && guest.isOnline()) {
                    guest.sendMessage("§c入住时间已到，自动退房");
                }
                checkinHandler.checkout(guest != null ? guest : null);
                expired++;
            } else {
                // 未超时，重新注册定时任务
                long remaining = expireTime - now;
                long remainingTicks = remaining / 50L;
                if (remainingTicks > 0) {
                    Player finalGuest = Bukkit.getPlayer(room.getCurrentGuest());
                    if (finalGuest != null) {
                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            HotelRoom current = roomStorage.getRoom(room.getId());
                            if (current != null && current.isOccupied()
                                    && current.getCurrentGuest() != null
                                    && current.getCurrentGuest().equals(finalGuest.getUniqueId())) {
                                if (finalGuest.isOnline()) {
                                    finalGuest.sendMessage("§c入住时间已到，自动退房");
                                }
                                checkinHandler.checkout(finalGuest);
                            }
                        }, remainingTicks);
                    }
                }
            }
        }

        if (checked > 0) {
            getLogger().info("检查 " + checked + " 个入住房间，已自动退房 " + expired + " 个");
        }
    }
}
