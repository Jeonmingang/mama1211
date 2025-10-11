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

public class MarketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "플레이어만 사용 가능합니다.");
            return true;
        }
        Player p = (Player) sender;
        Inventory inv = Bukkit.createInventory(p, 54, ChatColor.DARK_AQUA + "시세 상점");
        // Fill items from dynamic config (use all materials that have state)
        DynamicPricingManager mgr = DynamicPricingManager.get(Bukkit.getPluginManager().getPlugin("UltimateServerPlugin2"));
        for (Material m : Material.values()) {
            DynamicPricingManager.PriceState st = mgr.get(m);
            if (st == null) continue;
            ItemStack it = new ItemStack(m, 1);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + m.name());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "현재 시세: " + ChatColor.YELLOW + String.format("%,.2f", st.price));
                long leftMs = mgr.getMillisUntilNext(m);
                long s = leftMs / 1000;
                long mm = s / 60; long ss = s % 60;
                lore.add(ChatColor.GRAY + "다음 갱신까지: " + ChatColor.GREEN + mm + "m " + ss + "s");
                lore.add(ChatColor.GRAY + "남은 판매량: " + ChatColor.GOLD + st.quotaLeft);
                lore.add(ChatColor.DARK_GRAY + "좌클릭: 1개 판매  / 쉬프트+좌클릭: 가능한 최대 판매");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            inv.addItem(it);
        }
        p.openInventory(inv);
        return true;
    }
}