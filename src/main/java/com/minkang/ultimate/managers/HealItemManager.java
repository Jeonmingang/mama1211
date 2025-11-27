package com.minkang.ultimate.managers;

import com.minkang.ultimate.Main;
import com.minkang.ultimate.utils.Texts;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class HealItemManager implements Listener {

    private final Main plugin;
    private final NamespacedKey key;

    public HealItemManager(Main plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "heal_item");
    }

    /** 손에 든 아이템을 힐 전용 아이템으로 바꿀 때 사용 */
    public ItemStack makeHealItem(ItemStack base) {
        if (base == null || base.getType() == Material.AIR) return null;
        ItemStack it = base.clone();
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return null;

        meta.setDisplayName(Texts.color("&d포켓몬 힐 패스"));
        List<String> lore = new ArrayList<>();
        lore.add(Texts.color("&7우클릭 시 &b/pokeheal &7이 실행됩니다.")); 
        lore.add(Texts.color("&8- &f포켓몬을 즉시 회복시키는 전용 아이템")); 
        lore.add(Texts.color("&8- &7다른 용도로 사용하지 마세요")); 
        meta.setLore(lore);

        // 반짝이 효과
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.INTEGER, 1);

        it.setItemMeta(meta);
        return it;
    }

    private boolean isHealItem(ItemStack is) {
        if (is == null || is.getType() == Material.AIR) return false;
        if (!is.hasItemMeta()) return false;
        ItemMeta meta = is.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(key, PersistentDataType.INTEGER);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return; // 메인손만 처리
        Action act = e.getAction();
        if (act != Action.RIGHT_CLICK_AIR && act != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack is = e.getItem();
        if (!isHealItem(is)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();

        // Pixelmon pokeheal 실행
        boolean ok = p.performCommand("pokeheal");
        if (!ok) {
            p.sendMessage("§c/pokeheal 명령어를 찾을 수 없습니다. Pixelmon 플러그인을 확인해주세요.");
        } else {
            p.sendMessage("§d힐 아이템 사용! §7포켓몬이 회복되었습니다.");
        }
    }
}
