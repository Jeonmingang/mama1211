package com.minkang.ultimate.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /알걸음 <1~6>
 *
 * Pixelmon eggsteps 명령을 무권한 별칭으로 실행한다.
 * 요청한 대로 플레이어에게 OP를 잠깐 부여했다가 원상복구한 뒤,
 * 플레이어 컨텍스트로 /eggsteps <플레이어> <슬롯> 을 수행한다.
 */
public class EggStepsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length != 1) {
            p.sendMessage(ChatColor.YELLOW + "사용법: /알걸음 <1~6>");
            return true;
        }

        int slot;
        try {
            slot = Integer.parseInt(args[0]);
        } catch (Exception ex) {
            p.sendMessage(ChatColor.RED + "숫자를 입력하세요! (1~6)");
            return true;
        }

        if (slot < 1 || slot > 6) {
            p.sendMessage(ChatColor.RED + "슬롯은 1~6 입니다.");
            return true;
        }

        boolean wasOp = p.isOp();
        try {
            if (!wasOp) p.setOp(true);

            // Primary: /eggsteps <player> <slot>
            boolean dispatched = p.performCommand("eggsteps " + p.getName() + " " + slot);

            // Some environments may have a typo/alternate command name registered.
            if (!dispatched) {
                p.performCommand("eggesteps " + p.getName() + " " + slot);
            }
        } finally {
            if (!wasOp) {
                p.setOp(false);
            }
        }
        return true;
    }
}
