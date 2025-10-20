package com.minkang.usp2.lock;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.stream.Collectors;

public class LockListener implements Listener {

    private static final LockStore STORE = new LockStore();

    private static List<String> parseNames(String line) {
        if (line == null) return Collections.emptyList();
        String raw = ChatColor.stripColor(line).replace(" ", "");
        if (raw.isEmpty()) return Collections.emptyList();
        String[] parts = raw.split("[,，/\\\\|]+"); // comma or similar separators
        List<String> names = new ArrayList<>();
        for (String p : parts) {
            if (p != null) {
                String s = p.trim();
                if (!s.isEmpty()) names.add(s);
            }
        }
        return names;
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent e) {
        boolean byTicket = LockUtils.isOurTicket(e.getPlayer().getInventory().getItemInMainHand())
                || LockUtils.isOurTicket(e.getPlayer().getInventory().getItemInOffHand());
        if (!byTicket) return;

        String header = e.getLine(0) != null ? ChatColor.stripColor(e.getLine(0)).replace(" ", "") : "";
        if (!header.equalsIgnoreCase("[잠금]")) return;

        List<String> names = new ArrayList<>();
        names.addAll(parseNames(e.getLine(1)));
        names.addAll(parseNames(e.getLine(2)));
        names.addAll(parseNames(e.getLine(3))); // optional 3줄 추가 닉네임

        List<String> unique = names.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.toList());

        if (unique.isEmpty()) {
            e.getPlayer().sendMessage(ChatColor.RED + "2~3줄에 닉네임을 콤마(,)로 구분해 입력하세요.");
            return;
        }

        Block signBlock = e.getBlock();
        Block target = LockUtils.getAttachedBlock(signBlock);
        if (target == null || !LockUtils.isLockable(target)) {
            e.getPlayer().sendMessage(ChatColor.RED + "이 표지판은 반드시 상자/배럴에 붙여야 합니다.");
            return;
        }

        STORE.put(target.getLocation(), new LockStore.LockEntry(unique, System.currentTimeMillis()));

        e.setLine(0, ChatColor.GOLD + "[ 잠금 ]");
        e.getPlayer().sendMessage(ChatColor.YELLOW + "잠금 완료: " + ChatColor.WHITE + String.join(", ", unique) + ChatColor.GRAY + " 전용 상자");
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        Inventory inv = e.getInventory();
        if (inv == null || inv.getLocation() == null) return;
        Block b = inv.getLocation().getBlock();
        if (!LockUtils.isLockable(b)) return;

        LockStore.LockEntry entry = STORE.get(b.getLocation());
        if (entry == null) return;

        Player p = (Player) e.getPlayer();
        if (!entry.canOpen(p)) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "이 상자는 " + ChatColor.WHITE + entry.allowedNamesPretty() + ChatColor.RED + " 전용입니다.");
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (LockUtils.isLockable(b)) {
            LockStore.LockEntry entry = STORE.get(b.getLocation());
            if (entry != null) {
                Player p = e.getPlayer();
                if (!entry.canOpen(p)) {
                    e.setCancelled(true);
                    p.sendMessage(ChatColor.RED + "잠금된 상자를 파괴할 수 없습니다.");
                } else {
                    STORE.remove(b.getLocation());
                    p.sendMessage(ChatColor.GRAY + "이 상자의 잠금이 해제되었습니다.");
                }
            }
        }
    }
}