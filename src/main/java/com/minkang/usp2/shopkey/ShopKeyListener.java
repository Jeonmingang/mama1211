package com.minkang.usp2.shopkey;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;

public class ShopKeyListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer flag = pdc.get(ShopKeyCommand.KEY_FLAG, PersistentDataType.INTEGER);
        if (flag == null || flag != 1) return;

        String preferred = pdc.get(ShopKeyCommand.KEY_PREFERRED, PersistentDataType.STRING);

        e.setCancelled(true);
        e.getPlayer().openInventory(ShopKeyGUI.build(e.getPlayer(), preferred));
    }

    @EventHandler
    public void onShopListClick(InventoryClickEvent e) {
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!ShopKeyGUI.getTitle().equals(e.getView().getTitle())) return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        String shopName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
        if (shopName == null || shopName.isEmpty()) return;

        Player p = (Player) e.getWhoClicked();
        p.closeInventory();
        // Either directly run the command or dispatch via Bukkit
        p.performCommand("상점 열기 " + shopName);
        // Alternatively: Bukkit.dispatchCommand(p, "상점 열기 " + shopName);
    }
}