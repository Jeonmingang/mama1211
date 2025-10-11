package com.minkang.ultimate.listeners;

import com.minkang.ultimate.managers.DynamicPricingManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MarketGuiListener implements Listener {

    private Economy getEconomy() {
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            return (rsp != null) ? rsp.getProvider() : null;
        } catch (Throwable t) { return null; }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv == null) return;
        String title = e.getView().getTitle();
        if (title == null || !ChatColor.stripColor(title).contains("시세 상점")) return;

        // prevent taking items out
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Material mat = clicked.getType();
        DynamicPricingManager mgr = DynamicPricingManager.get(Bukkit.getPluginManager().getPlugin("UltimateServerPlugin2"));
        DynamicPricingManager.PriceState st = mgr.get(mat);
        if (st == null) { p.sendMessage(ChatColor.RED + "이 품목은 현재 시세 대상이 아닙니다."); return; }

        int have = count(p.getInventory(), mat);
        if (have <= 0) { p.sendMessage(ChatColor.RED + "인벤토리에 " + mat.name() + " 이(가) 없습니다."); return; }

        int want = (e.getClick() == ClickType.SHIFT_LEFT) ? Math.min(have, st.quotaLeft) : Math.min(1, Math.min(have, st.quotaLeft));
        if (want <= 0) { p.sendMessage(ChatColor.RED + "현재 시세에서 판매 가능한 수량이 소진되었습니다. 다음 갱신까지 기다려 주세요!"); return; }

        double money = mgr.trySell(mat, want);
        if (money < 0) { p.sendMessage(ChatColor.RED + "현재 시세에서 판매 가능한 수량이 소진되었습니다."); return; }

        remove(p.getInventory(), mat, want);

        Economy econ = getEconomy();
        if (econ == null) { p.sendMessage(ChatColor.RED + "Vault 연동을 찾을 수 없습니다."); return; }
        econ.depositPlayer(p, money);
        p.sendMessage(ChatColor.GREEN + "판매 완료! " + mat.name() + " x" + want + " → " + String.format("%,.2f", money));
    }

    private int count(PlayerInventory inv, Material m) {
        int c = 0;
        for (ItemStack it : inv.getContents()) {
            if (it != null && it.getType() == m) c += it.getAmount();
        }
        return c;
    }

    private void remove(PlayerInventory inv, Material m, int qty) {
        for (int i = 0; i < inv.getSize() && qty > 0; i++) {
            ItemStack it = inv.getItem(i);
            if (it == null || it.getType() != m) continue;
            int take = Math.min(qty, it.getAmount());
            it.setAmount(it.getAmount() - take);
            if (it.getAmount() <= 0) inv.setItem(i, null);
            qty -= take;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e){
        String title = e.getView().getTitle();
        if (title == null) return;
        if (ChatColor.stripColor(title).contains("시세 상점")){
            e.setCancelled(true);
        }
    }
}
