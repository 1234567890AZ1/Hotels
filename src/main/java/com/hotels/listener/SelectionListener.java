package com.hotels.listener;

import com.hotels.HotelsPlugin;
import com.hotels.selection.SelectionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * 选区工具监听器 - 木斧左键/右键选点
 */
public class SelectionListener implements Listener {

    private final HotelsPlugin plugin;

    public SelectionListener(HotelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 检查是否手持木斧
        if (player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE) {
            return;
        }

        // 检查是否有 hotels.create 权限
        if (!player.hasPermission("hotels.create") && !player.hasPermission("hotels.admin")) {
            return;
        }

        // 检查是否在选区模式（通过检查物品显示名称或直接检查权限）
        // 这里简单处理：只要拿着木斧且有权限就进入选区模式
        SelectionManager selectionManager = plugin.getSelectionManager();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            // 左键 - 设置 Pos1
            event.setCancelled(true);
            Location loc = event.getAction() == Action.LEFT_CLICK_BLOCK
                    ? event.getClickedBlock().getLocation()
                    : player.getLocation();

            selectionManager.setPos1(player, loc);
            player.sendMessage(String.format(
                    "§a已设置第 1 点: §e(%.0f, %.0f, %.0f)",
                    loc.getX(), loc.getY(), loc.getZ()
            ));

        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            // 右键 - 设置 Pos2
            event.setCancelled(true);
            Location loc = event.getAction() == Action.RIGHT_CLICK_BLOCK
                    ? event.getClickedBlock().getLocation()
                    : player.getLocation();

            selectionManager.setPos2(player, loc);
            player.sendMessage(String.format(
                    "§a已设置第 2 点: §e(%.0f, %.0f, %.0f)",
                    loc.getX(), loc.getY(), loc.getZ()
            ));

            // 如果两个点都选好了，显示区域信息
            if (selectionManager.getSelection(player).hasBothPositions()) {
                Location p1 = selectionManager.getSelection(player).getPos1();
                Location p2 = selectionManager.getSelection(player).getPos2();
                long dx = Math.abs((long) Math.ceil(p1.getX()) - (long) Math.ceil(p2.getX())) + 1;
                long dy = Math.abs((long) Math.ceil(p1.getY()) - (long) Math.ceil(p2.getY())) + 1;
                long dz = Math.abs((long) Math.ceil(p1.getZ()) - (long) Math.ceil(p2.getZ())) + 1;
                long volume = dx * dy * dz;
                player.sendMessage("§7区域大小: §e" + dx + " × " + dy + " × " + dz + " §7(§e" + volume + " §7方块)");
                player.sendMessage("§7现在输入 §e/ht setspawn §7设置传送点，然后 §e/ht create <名称> §7创建房间");
            }
        }
    }
}
