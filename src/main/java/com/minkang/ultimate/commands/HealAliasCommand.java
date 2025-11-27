package com.minkang.ultimate.commands;

import com.minkang.ultimate.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HealAliasCommand implements CommandExecutor {

    private final Main plugin;

    public HealAliasCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player p = (Player) sender;

        // /힐 아이템  (OP 전용): 손에 든 아이템 → 힐 전용 아이템
        if (args.length >= 1 && "아이템".equalsIgnoreCase(args[0])) {
            if (!p.isOp() && !p.hasPermission("usp.healitem")) {
                p.sendMessage("§c이 명령어는 운영자 전용입니다.");
                return true;
            }
            ItemStack inHand = p.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                p.sendMessage("§c손에 든 아이템이 없습니다.");
                return true;
            }
            if (plugin.healItems() == null) {
                p.sendMessage("§c힐 아이템 시스템이 초기화되지 않았습니다. 관리자에게 문의하세요.");
                return true;
            }
            ItemStack converted = plugin.healItems().makeHealItem(inHand);
            if (converted == null) {
                p.sendMessage("§c이 아이템은 힐 전용 아이템으로 만들 수 없습니다.");
                return true;
            }
            p.getInventory().setItemInMainHand(converted);
            p.sendMessage("§d포켓몬 힐 전용 아이템으로 설정되었습니다! §b우클릭§7 시 /pokeheal 이 실행됩니다.");
            return true;
        }

        // 기본: /힐 → /pokeheal 실행
        boolean ok = Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pokeheal " + p.getName());
        if (!ok) {
            p.sendMessage("§c/pokeheal 명령어를 찾을 수 없습니다. Pixelmon 플러그인을 확인해주세요.");
        }
        return true;
    }
}