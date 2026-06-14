package com.hotels;

import com.hotels.command.HotelsCommand;
import com.hotels.listener.ChatInputHandler;
import com.hotels.listener.GUIListener;
import com.hotels.listener.RoomGuardListener;
import com.hotels.listener.SelectionListener;
import com.hotels.selection.SelectionManager;
import com.hotels.storage.RoomStorage;
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
}
