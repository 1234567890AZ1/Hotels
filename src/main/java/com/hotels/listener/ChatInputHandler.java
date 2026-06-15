package com.hotels.listener;

import com.hotels.HotelsPlugin;
import com.hotels.model.HotelRoom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天输入处理器 - 接收玩家在聊天框输入的价格/密码等
 */
public class ChatInputHandler implements Listener {

    private final HotelsPlugin plugin;
    private final Map<UUID, String> pendingInputs;

    public ChatInputHandler(HotelsPlugin plugin) {
        this.plugin = plugin;
        this.pendingInputs = new ConcurrentHashMap<>();
    }

    /**
     * 期待玩家输入
     * @param player 玩家
     * @param context 上下文，格式 "action:roomId"，如 "setprice:abc123"
     */
    public void expectInput(Player player, String context) {
        pendingInputs.put(player.getUniqueId(), context);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String context = pendingInputs.get(player.getUniqueId());

        if (context == null) return;

        event.setCancelled(true);
        pendingInputs.remove(player.getUniqueId());

        String message = event.getMessage().trim();

        // 处理非房间上下文（合集创建/删除、房间删除等）
        if (!context.contains(":") ||
            context.startsWith("deletecollection:") ||
            context.startsWith("setcollectionduration:") ||
            context.startsWith("deleteroom:")) {
            handleNonRoomContext(player, context, message);
            return;
        }

        String[] parts = context.split(":", 2);

        if (parts.length < 2) return;

        String action = parts[0];
        String roomId = parts[1];

        HotelRoom room = plugin.getRoomStorage().getRoom(roomId);
        if (room == null) {
            player.sendMessage("§c房间不存在或已删除");
            return;
        }

        // 验证房主
        if (!room.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§c你不是这个房间的房主");
            return;
        }

        switch (action) {
            case "setprice":
                try {
                    double price = Double.parseDouble(message);
                    if (price < 0) {
                        player.sendMessage("§c价格不能为负数");
                        return;
                    }
                    if (price > 1000000) {
                        player.sendMessage("§c价格太高了，最高 1000000");
                        return;
                    }
                    room.setPrice(price);
                    plugin.getRoomStorage().saveRoom(room);
                    player.sendMessage("§a房间价格已设置为: §e" + plugin.getEconomyManager().format(price));
                } catch (NumberFormatException e) {
                    player.sendMessage("§c请输入有效的数字");
                }
                break;

            case "setpassword":
                if (message.length() > 20) {
                    player.sendMessage("§c密码最长 20 个字符");
                    return;
                }
                if (message.isEmpty()) {
                    player.sendMessage("§c密码不能为空");
                    return;
                }
                room.setPassword(message);
                plugin.getRoomStorage().saveRoom(room);
                player.sendMessage("§a房间密码已设置");
                break;
        }
    }

    // ===== 非房间相关的上下文处理 =====

    private void handleNonRoomContext(Player player, String context, String message) {
        if (context.equals("createcollection")) {
            // 创建合集 - 第一步：输入名称
            if (message.length() > 32) {
                player.sendMessage("§c合集名称最长 32 个字符");
                return;
            }
            if (message.isEmpty()) {
                player.sendMessage("§c名称不能为空");
                return;
            }

            // 检查数量限制
            int maxCols = plugin.getConfig().getInt("max-collections-per-player", 5);
            int currentCols = plugin.getRoomStorage().getCollectionsByOwner(player.getUniqueId()).size();
            if (currentCols >= maxCols) {
                player.sendMessage("§c你已达到最大合集数量限制 (" + maxCols + "个)");
                return;
            }

            // 保存名称，然后询问时长
            player.sendMessage("§e请输入使用时长（分钟），输入 0 表示不限时:");
            plugin.getChatInputHandler().expectInput(player, "setcollectionduration:" + message);
            return;
        }

        if (context.startsWith("setcollectionduration:")) {
            // 创建合集 - 第二步：输入时长
            String name = context.substring("setcollectionduration:".length());

            int duration;
            try {
                duration = Integer.parseInt(message);
                if (duration < 0) {
                    player.sendMessage("§c时长不能为负数");
                    return;
                }
                if (duration > 43200) { // 最大30天
                    player.sendMessage("§c时长不能超过 43200 分钟（30天）");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§c请输入有效的数字（分钟）");
                return;
            }

            com.hotels.model.RoomCollection col = new com.hotels.model.RoomCollection();
            col.setName(name);
            col.setDurationMinutes(duration);
            col.setOwner(player.getUniqueId());
            col.setOwnerName(player.getName());
            plugin.getRoomStorage().saveCollection(col);

            if (duration <= 0) {
                player.sendMessage("§a酒店合集 §e" + name + " §a创建成功！不限时");
            } else {
                player.sendMessage("§a酒店合集 §e" + name + " §a创建成功！时长: " + duration + " 分钟");
            }
            player.sendMessage("§7使用 §e/ht §7打开菜单管理合集");
            return;
        }

        if (context.startsWith("deletecollection:")) {
            String colId = context.substring("deletecollection:".length());
            if (message.equalsIgnoreCase("confirm") || message.equalsIgnoreCase("yes") || message.equals("确认")) {
                com.hotels.model.RoomCollection col = plugin.getRoomStorage().getCollection(colId);
                if (col != null) {
                    String name = col.getName();
                    plugin.getRoomStorage().removeCollection(colId);
                    player.sendMessage("§c合集 §e" + name + " §c已删除");
                } else {
                    player.sendMessage("§c合集不存在");
                }
            } else {
                player.sendMessage("§c已取消删除");
            }
            return;
        }

        if (context.startsWith("deleteroom:")) {
            String roomId = context.substring("deleteroom:".length());
            if (message.equalsIgnoreCase("confirm") || message.equalsIgnoreCase("yes") || message.equals("确认")) {
                com.hotels.model.HotelRoom room = plugin.getRoomStorage().getRoom(roomId);
                if (room != null) {
                    String name = room.getName();
                    plugin.getRoomStorage().removeRoom(roomId);
                    player.sendMessage("§c房间 §e" + name + " §c已删除");
                } else {
                    player.sendMessage("§c房间不存在");
                }
            } else {
                player.sendMessage("§c已取消删除");
            }
            return;
        }
    }

    /**
     * 清除玩家的待输入状态
     */
    public void clear(Player player) {
        pendingInputs.remove(player.getUniqueId());
    }
}
