
package com.minkang.usp2;

import com.minkang.usp2.commands.*;
import com.minkang.usp2.listeners.*;
import com.minkang.usp2.managers.*;
import org.bukkit.Bukkit;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.CommandExecutor;

public class Main extends JavaPlugin {
    private void logSevere(String msg, Throwable t){
        getLogger().severe(msg + (t!=null? (" :: " + t.getMessage()) : ""));
    }

    
    private void bindCmd(String name, CommandExecutor exec){
        PluginCommand cmd = getCommand(name);
        if (cmd != null){ cmd.setExecutor(exec); }
        else { getLogger().warning("command not found: " + name); }
    }
private EconomyManager economy;
    private BanknoteManager banknote;
    private RepairManager repair;
    private ShopManager shop;
    private LockManager lock;

    public EconomyManager eco(){ return economy; }
    public BanknoteManager bank(){ return banknote; }
    public RepairManager repair(){ return repair; }
    public ShopManager shop(){ return shop; }
    public LockManager lock(){ return lock; }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        economy = new EconomyManager(this);
        banknote = new BanknoteManager(this);
        repair = new RepairManager(this);
        shop = new ShopManager(this);
        lock = new LockManager(this);

        // listeners
        if (getConfig().getBoolean("hunger-disable", true)) {
            Bukkit.getPluginManager().registerEvents(new HungerListener(), this);
        }
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(banknote, this);
        Bukkit.getPluginManager().registerEvents(repair, this);

        Bukkit.getPluginManager().registerEvents(shop, this);
        Bukkit.getPluginManager().registerEvents(lock, this);

        
        
        // Citizens bridge (optional)
        try {
            if (getServer().getPluginManager().getPlugin("Citizens") != null) {
                getServer().getPluginManager().registerEvents(new com.minkang.usp2.listeners.CitizensBridge(shop), this);
                getLogger().info("Citizens bridge enabled.");
            } else {
                getLogger().info("Citizens not found; NPC shop linking disabled.");
            }
        } catch (Throwable t) {
            getLogger().warning("Citizens bridge init failed: " + t.getMessage());
        }
        // commands
        bindCmd("배틀종료", new BattleEndCommand(this));
        bindCmd("돈", new MoneyCommand(this));
        bindCmd("수표", new ChequeCommand(this));
        bindCmd("수리권", new RepairTicketCommand(this));
        ShopCommand shopCmd = new ShopCommand(this);
        bindCmd("상점", shopCmd);
        bindCmd("상점리로드", shopCmd);
        bindCmd("잠금", new LockCommand(this));
        bindCmd("잠금권", new LockTokenCommand(this));
        bindCmd("야투", new YatuCommand(this));
        getServer().getPluginManager().registerEvents(new YatuListener(this, new NamespacedKey(this, "yatu")), this);
getLogger().info("UltimateServerPlugin enabled.");
    }

    @Override
    public void onDisable() {
// TRADE_REMOVED:         trade.closeAll();
        getLogger().info("UltimateServerPlugin disabled.");
    }
}
