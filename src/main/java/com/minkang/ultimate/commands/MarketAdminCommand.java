package com.minkang.ultimate.commands;

import com.minkang.ultimate.managers.DynamicPricingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MarketAdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage(ChatColor.RED + "플레이어만 사용 가능합니다."); return true; }
        if (!sender.hasPermission("usp.market.admin")) { sender.sendMessage(ChatColor.RED + "권한이 없습니다: usp.market.admin"); return true; }

        Player p = (Player) sender;
        openRoot(p);
        return true;
    }

    public static void openRoot(Player p){
        Inventory inv = Bukkit.createInventory(p, 54, ChatColor.DARK_PURPLE + "시세 상점 설정");
        DynamicPricingManager mgr = DynamicPricingManager.get(Bukkit.getPluginManager().getPlugin("UltimateServerPlugin2"));

        // Interval controls
        {
            ItemStack minus60 = new ItemStack(Material.RED_CONCRETE);
            ItemMeta m = minus60.getItemMeta();
            m.setDisplayName(ChatColor.RED + "-60s");
            minus60.setItemMeta(m);
            inv.setItem(10, minus60);

            ItemStack minus10 = new ItemStack(Material.ORANGE_CONCRETE);
            m = minus10.getItemMeta();
            m.setDisplayName(ChatColor.GOLD + "-10s");
            minus10.setItemMeta(m);
            inv.setItem(11, minus10);

            ItemStack disp = new ItemStack(Material.CLOCK);
            m = disp.getItemMeta();
            m.setDisplayName(ChatColor.AQUA + "갱신 주기");
            List<String> lore = new ArrayList<>();
            int sec = mgr.getIntervalSeconds();
            lore.add(ChatColor.GRAY + "현재: " + ChatColor.YELLOW + sec + "s");
            int mm = sec/60, ss = sec%60;
            lore.add(ChatColor.GRAY + "== " + mm + "m " + ss + "s");
            lore.add(ChatColor.DARK_GRAY + "좌클릭: 저장");
            m.setLore(lore);
            disp.setItemMeta(m);
            inv.setItem(12, disp);

            ItemStack plus10 = new ItemStack(Material.LIME_CONCRETE);
            m = plus10.getItemMeta();
            m.setDisplayName(ChatColor.GREEN + "+10s");
            plus10.setItemMeta(m);
            inv.setItem(13, plus10);

            ItemStack plus60 = new ItemStack(Material.GREEN_CONCRETE);
            m = plus60.getItemMeta();
            m.setDisplayName(ChatColor.DARK_GREEN + "+60s");
            plus60.setItemMeta(m);
            inv.setItem(14, plus60);
        }

        // Market items to edit
        int idx = 27;
        for (Material mat : mgr.getMarketItems()){
            if (idx >= 54) break;
            ItemStack it = new ItemStack(mat, 1);
            ItemMeta meta = it.getItemMeta();
            if (meta != null){
                DynamicPricingManager.ItemConfig ic = mgr.getItemConfig(mat);
                meta.setDisplayName(ChatColor.AQUA + mat.name());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "min: " + ChatColor.YELLOW + String.format("%,.2f", ic.min));
                lore.add(ChatColor.GRAY + "max: " + ChatColor.YELLOW + String.format("%,.2f", ic.max));
                lore.add(ChatColor.GRAY + "quota: " + ChatColor.YELLOW + ic.quota);
                lore.add(ChatColor.DARK_GRAY + "클릭: 상세 설정");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            inv.setItem(idx++, it);
        }

        p.openInventory(inv);
    }
}