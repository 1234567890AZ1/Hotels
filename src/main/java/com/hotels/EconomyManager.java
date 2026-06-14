package com.hotels;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

/**
 * 经济管理器 - 对接 Vault
 */
public class EconomyManager {

    private final HotelsPlugin plugin;
    private Economy economy;
    private boolean enabled;

    public EconomyManager(HotelsPlugin plugin) {
        this.plugin = plugin;
        this.enabled = setupEconomy();
    }

    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("未找到 Vault 插件，经济功能已禁用");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer()
                .getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            plugin.getLogger().warning("未找到 Vault 经济实现，经济功能已禁用");
            return false;
        }

        economy = rsp.getProvider();
        plugin.getLogger().info("已对接经济系统: " + economy.getName());
        return true;
    }

    public boolean isEnabled() {
        return enabled && economy != null;
    }

    /**
     * 获取玩家余额
     */
    public double getBalance(OfflinePlayer player) {
        if (!isEnabled()) return 0;
        return economy.getBalance(player);
    }

    /**
     * 扣款
     * @return true 扣款成功
     */
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        if (amount <= 0) return true;

        EconomyResponse resp = economy.withdrawPlayer(player, amount);
        if (!resp.transactionSuccess()) {
            plugin.getLogger().log(Level.WARNING, "扣款失败: " + resp.errorMessage);
            return false;
        }
        return true;
    }

    /**
     * 存款
     */
    public boolean deposit(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        if (amount <= 0) return true;

        EconomyResponse resp = economy.depositPlayer(player, amount);
        return resp.transactionSuccess();
    }

    /**
     * 格式化金额
     */
    public String format(double amount) {
        if (!isEnabled()) return String.format("%.2f", amount);
        return economy.format(amount);
    }

    /**
     * 获取货币名称
     */
    public String currencyNamePlural() {
        if (!isEnabled()) return "货币";
        return economy.currencyNamePlural();
    }
}
