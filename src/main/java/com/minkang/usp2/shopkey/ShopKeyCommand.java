package com.minkang.usp2.shopkey;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.minkang.ultimate.Main;

import java.util.ArrayList;
import java.util.List;

public class ShopKeyCommand implements CommandExecutor {

    public static final NamespacedKey KEY_FLAG =
            new NamespacedKey(JavaPlugin.getPlugin(Main.class), "shop_key");
    public static final NamespacedKey KEY_PREFERRED =
            new NamespacedKey(JavaPlugin.getPlugin(Main.class), "shop_preferred");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(ChatColor.RED + "사용법: /상점 아이템 <상점이름>");
            return true;
        }

        String shopName = args[0];

        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType().isAir()) {
            p.sendMessage(ChatColor.RED + "손에 아이템을 들고 사용하세요.");
            return true;
        }

        ItemMeta meta = inHand.getItemMeta();
        if (meta == null) {
            p.sendMessage(ChatColor.RED + "이 아이템은 메타데이터를 가질 수 없습니다.");
            return true;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_FLAG, PersistentDataType.INTEGER, 1);
        pdc.set(KEY_PREFERRED, PersistentDataType.STRING, shopName);

        meta.setDisplayName(ChatColor.GOLD + "상점 키 " + ChatColor.GRAY + "(" + shopName + ")");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "허공에 우클릭: 상점 목록 열기");
        lore.add(ChatColor.GRAY + "클릭 후 " + ChatColor.WHITE + shopName + ChatColor.GRAY + " 상점으로 이동");
        meta.setLore(lore);

        inHand.setItemMeta(meta);

        p.sendMessage(ChatColor.YELLOW + "[상점] " + ChatColor.WHITE + "해당 아이템이 상점 키로 설정되었습니다.");
        return true;
    }
}