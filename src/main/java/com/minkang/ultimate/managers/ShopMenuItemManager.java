package com.minkang.ultimate.managers;

import com.minkang.ultimate.Main;
import com.minkang.ultimate.utils.Texts;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ShopMenuItemManager implements Listener {

    private final Main plugin;
    private final NamespacedKey menuItemKey;
    private final NamespacedKey buttonKey;

    private static final String MENU_TITLE = "상점 메뉴";

    public ShopMenuItemManager(Main plugin) {
        this.plugin = plugin;
        this.menuItemKey = new NamespacedKey(plugin, "shop_menu_item");
        this.buttonKey = new NamespacedKey(plugin, "shop_menu_button");
    }

    /** 손에 든 아이템을 상점 메뉴 전용 아이템으로 변환 */
    public ItemStack makeShopMenuItem(ItemStack base) {
        if (base == null || base.getType() == Material.AIR) return null;
        ItemStack it = base.clone();
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return null;

        meta.setDisplayName(Texts.color("&e원클릭 상점 메뉴")); 
        List<String> lore = new ArrayList<>();
        lore.add(Texts.color("&7우클릭 시 &f상점 목록 GUI &7가 열립니다.")); 
        lore.add(Texts.color("&8- &f/상점 생성, /상점 목록 에서 설정한 상점들이 표시됩니다.")); 
        lore.add(Texts.color("&8- &7GUI에서 원하는 상점을 클릭하면 바로 그 상점이 열립니다.")); 
        meta.setLore(lore);

        // 반짝이 효과
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(menuItemKey, PersistentDataType.INTEGER, 1);

        it.setItemMeta(meta);
        return it;
    }

    private boolean isMenuItem(ItemStack is) {
        if (is == null || is.getType() == Material.AIR) return false;
        if (!is.hasItemMeta()) return false;
        ItemMeta meta = is.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(menuItemKey, PersistentDataType.INTEGER);
    }

    private boolean isMenuButton(ItemStack is) {
        if (is == null || is.getType() == Material.AIR) return false;
        if (!is.hasItemMeta()) return false;
        ItemMeta meta = is.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(buttonKey, PersistentDataType.STRING);
    }

    private String getButtonShopKey(ItemStack is) {
        if (is == null || is.getType() == Material.AIR) return null;
        if (!is.hasItemMeta()) return null;
        ItemMeta meta = is.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(buttonKey, PersistentDataType.STRING)) return null;
        return pdc.get(buttonKey, PersistentDataType.STRING);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action act = e.getAction();
        if (act != Action.RIGHT_CLICK_AIR && act != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack is = e.getItem();
        if (!isMenuItem(is)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        openMenu(p);
    }

    private void openMenu(Player p) {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection shopsSec = cfg.getConfigurationSection("shops");
        if (shopsSec == null) {
            p.sendMessage("§c등록된 상점이 없습니다. /상점 생성 으로 상점을 먼저 만들어 주세요.");
            return;
        }
        Set<String> keys = shopsSec.getKeys(false);
        if (keys == null || keys.isEmpty()) {
            p.sendMessage("§c등록된 상점이 없습니다. /상점 생성 으로 상점을 먼저 만들어 주세요.");
            return;
        }

        List<String> shopKeys = new ArrayList<>(keys);
        Collections.sort(shopKeys);

        int size = ((shopKeys.size() - 1) / 9 + 1) * 9;
        if (size < 9) size = 9;
        if (size > 54) size = 54;

        Inventory inv = Bukkit.createInventory(null, size, MENU_TITLE);

        int slot = 0;
        for (String key : shopKeys) {
            if (slot >= size) break;
            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Texts.color("&e상점 &f" + key)); 
                List<String> lore = new ArrayList<>();
                lore.add(Texts.color("&7클릭하면 해당 상점이 열립니다.")); 
                lore.add(Texts.color("&8키: &f" + key));
                meta.setLore(lore);

                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(buttonKey, PersistentDataType.STRING, key);

                icon.setItemMeta(meta);
            }
            inv.setItem(slot, icon);
            slot++;
        }

        p.openInventory(inv);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;

        if (e.getView() == null || e.getView().getTitle() == null) return;
        String title = ChatColor.stripColor(e.getView().getTitle());
        if (!MENU_TITLE.equals(title)) return;

        if (e.getRawSlot() < 0 || e.getRawSlot() >= e.getInventory().getSize()) {
            return; // 바깥 인벤
        }

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (!isMenuButton(clicked)) return;

        String shopKey = getButtonShopKey(clicked);
        if (shopKey == null || shopKey.trim().isEmpty()) return;

        try {
            plugin.shop().open(p, shopKey);
        } catch (Throwable t) {
            p.sendMessage("§c상점을 여는 중 오류가 발생했습니다. 관리자에게 문의하세요.");
        }
    }
}
