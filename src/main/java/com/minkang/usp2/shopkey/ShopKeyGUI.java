package com.minkang.usp2.shopkey;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.minkang.ultimate.Main;

public class ShopKeyGUI {

    private static final String TITLE = ChatColor.DARK_AQUA + "상점 목록";

    public static Inventory build(Player viewer, String preferred) {
        File dataFile = new File(Main.getInstance().getDataFolder(), "shops.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);

        // Expect shops under "shops" section (adjust if your format is different).
        Set<String> keys;
        if (cfg.isConfigurationSection("shops")) {
            keys = cfg.getConfigurationSection("shops").getKeys(false);
        } else {
            keys = cfg.getKeys(false);
        }

        int size = Math.min(54, Math.max(9, ((keys.size() + 8) / 9) * 9));
        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        int i = 0;
        for (String k : keys) {
            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + k);
            item.setItemMeta(meta);
            if (i < size) {
                inv.setItem(i++, item);
            }
        }
        return inv;
    }

    public static String getTitle() {
        return TITLE;
    }
}