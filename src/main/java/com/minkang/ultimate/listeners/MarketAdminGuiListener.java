package com.minkang.ultimate.listeners;

import com.minkang.ultimate.commands.MarketAdminCommand;
import com.minkang.ultimate.managers.DynamicPricingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class MarketAdminGuiListener implements Listener {
    private static class EditState {
        Material mat;
        double min;
        double max;
        int quota;
    }

    private final Map<UUID, Integer> bufferInterval = new HashMap<>();
    private final Map<UUID, EditState> editing = new HashMap<>();

    private DynamicPricingManager mgr(){
        Plugin pl = Bukkit.getPluginManager().getPlugin("UltimateServerPlugin2");
        return DynamicPricingManager.get(pl);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e){
        String title = e.getView().getTitle();
        if (title == null) return;
        String s = ChatColor.stripColor(title);
        if (s.contains("시세 상점 설정") || s.contains("품목 설정")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent e){
        UUID u = ((Player)e.getPlayer()).getUniqueId();
        bufferInterval.remove(u);
        editing.remove(u);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e){
        Inventory inv = e.getInventory();
        if (inv == null) return;
        String title = e.getView().getTitle();
        if (title == null) return;
        String raw = ChatColor.stripColor(title);

        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (raw.contains("시세 상점 설정")){
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it == null || it.getType() == Material.AIR) return;

            int slot = e.getRawSlot();
            DynamicPricingManager m = mgr();
            int cur = bufferInterval.getOrDefault(p.getUniqueId(), m.getIntervalSeconds());

            switch (slot){
                case 10: cur = Math.max(10, cur - 60); bufferInterval.put(p.getUniqueId(), cur); MarketAdminCommand.openRoot(p); return;
                case 11: cur = Math.max(10, cur - 10); bufferInterval.put(p.getUniqueId(), cur); MarketAdminCommand.openRoot(p); return;
                case 12: // save
                    m.setIntervalSecondsAndSave(cur);
                    p.sendMessage(ChatColor.GREEN + "갱신 주기 저장: " + cur + "s");
                    m.reload();
                    MarketAdminCommand.openRoot(p);
                    return;
                case 13: cur = cur + 10; bufferInterval.put(p.getUniqueId(), cur); MarketAdminCommand.openRoot(p); return;
                case 14: cur = cur + 60; bufferInterval.put(p.getUniqueId(), cur); MarketAdminCommand.openRoot(p); return;
                default:
                    // If clicked on a material, open item editor
                    Material mat = it.getType();
                    if (mat != null && m.getItemConfig(mat) != null){
                        openItemEditor(p, mat);
                    }
            }
            return;
        }

        if (raw.contains("품목 설정")){
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it == null || it.getType() == Material.AIR) return;

            EditState st = editing.get(p.getUniqueId());
            if (st == null) return;

            int slot = e.getRawSlot();
            switch (slot){
                // min row
                case 10: st.min = Math.max(0, st.min - 1000); break;
                case 11: st.min = Math.max(0, st.min - 100); break;
                case 12: st.min = Math.max(0, st.min - 10); break;
                case 14: st.min = st.min + 10; break;
                case 15: st.min = st.min + 100; break;
                case 16: st.min = st.min + 1000; break;

                // max row
                case 19: st.max = Math.max(st.min + 0.01, st.max - 1000); break;
                case 20: st.max = Math.max(st.min + 0.01, st.max - 100); break;
                case 21: st.max = Math.max(st.min + 0.01, st.max - 10); break;
                case 23: st.max = st.max + 10; break;
                case 24: st.max = st.max + 100; break;
                case 25: st.max = st.max + 1000; break;

                // quota row
                case 28: st.quota = Math.max(0, st.quota - 64); break;
                case 29: st.quota = Math.max(0, st.quota - 16); break;
                case 30: st.quota = Math.max(0, st.quota - 1); break;
                case 32: st.quota = st.quota + 1; break;
                case 33: st.quota = st.quota + 16; break;
                case 34: st.quota = st.quota + 64; break;

                // save
                case 40:
                    DynamicPricingManager m = mgr();
                    m.setItemConfigAndSave(st.mat, st.min, st.max, st.quota);
                    p.sendMessage(ChatColor.GREEN + "저장 완료: " + st.mat.name()
                            + " min=" + String.format("%,.2f", st.min)
                            + " max=" + String.format("%,.2f", st.max)
                            + " quota=" + st.quota);
                    m.reload();
                    openItemEditor(p, st.mat);
                    return;

                // reroll price now
                case 41:
                    mgr().reroll(st.mat);
                    p.sendMessage(ChatColor.AQUA + "즉시 갱신 완료: " + st.mat.name());
                    openItemEditor(p, st.mat);
                    return;

                // back
                case 42:
                    MarketAdminCommand.openRoot(p);
                    return;
            }
            // refresh screen
            openItemEditor(p, st.mat);
        }
    }

    private void openItemEditor(Player p, Material mat){
        Inventory inv = Bukkit.createInventory(p, 45, ChatColor.DARK_PURPLE + "품목 설정: " + mat.name());
        DynamicPricingManager.ItemConfig ic = mgr().getItemConfig(mat);
        if (ic == null) return;

        // Center displayed item
        {
            ItemStack it = new ItemStack(mat, 1);
            ItemMeta m = it.getItemMeta();
            m.setDisplayName(ChatColor.AQUA + mat.name());
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + "min=" + String.format("%,.2f", ic.min));
            lore.add(ChatColor.GRAY + "max=" + String.format("%,.2f", ic.max));
            lore.add(ChatColor.GRAY + "quota=" + ic.quota);
            it.setItemMeta(m);
            inv.setItem(22, it);
        }

        EditState st = new EditState();
        st.mat = mat; st.min = ic.min; st.max = ic.max; st.quota = ic.quota;
        editing.put(p.getUniqueId(), st);

        // buttons
        put(inv, 10, Material.RED_CONCRETE, ChatColor.RED + "min -1000");
        put(inv, 11, Material.ORANGE_CONCRETE, ChatColor.GOLD + "min -100");
        put(inv, 12, Material.YELLOW_CONCRETE, ChatColor.YELLOW + "min -10");
        put(inv, 14, Material.LIME_CONCRETE, ChatColor.GREEN + "min +10");
        put(inv, 15, Material.GREEN_CONCRETE, ChatColor.DARK_GREEN + "min +100");
        put(inv, 16, Material.GREEN_WOOL, ChatColor.DARK_GREEN + "min +1000");

        put(inv, 19, Material.RED_TERRACOTTA, ChatColor.RED + "max -1000");
        put(inv, 20, Material.ORANGE_TERRACOTTA, ChatColor.GOLD + "max -100");
        put(inv, 21, Material.YELLOW_TERRACOTTA, ChatColor.YELLOW + "max -10");
        put(inv, 23, Material.LIME_TERRACOTTA, ChatColor.GREEN + "max +10");
        put(inv, 24, Material.GREEN_TERRACOTTA, ChatColor.DARK_GREEN + "max +100");
        put(inv, 25, Material.GREEN_CONCRETE_POWDER, ChatColor.DARK_GREEN + "max +1000");

        put(inv, 28, Material.RED_STAINED_GLASS, ChatColor.RED + "quota -64");
        put(inv, 29, Material.ORANGE_STAINED_GLASS, ChatColor.GOLD + "quota -16");
        put(inv, 30, Material.YELLOW_STAINED_GLASS, ChatColor.YELLOW + "quota -1");
        put(inv, 32, Material.LIME_STAINED_GLASS, ChatColor.GREEN + "quota +1");
        put(inv, 33, Material.GREEN_STAINED_GLASS, ChatColor.DARK_GREEN + "quota +16");
        put(inv, 34, Material.GREEN_WOOL, ChatColor.DARK_GREEN + "quota +64");

        put(inv, 40, Material.PAPER, ChatColor.AQUA + "저장");
        put(inv, 41, Material.CLOCK, ChatColor.AQUA + "즉시 갱신(가격/쿼터 재롤) ");
        put(inv, 42, Material.ARROW, ChatColor.GRAY + "뒤로");

        p.openInventory(inv);
    }

    private void put(Inventory inv, int slot, Material mat, String name){
        ItemStack it = new ItemStack(mat, 1);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        it.setItemMeta(m);
        inv.setItem(slot, it);
    }
}