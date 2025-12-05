package com.minkang.ultimate.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /피씨 -> Pixelmon /pc 한글 별칭
 */
public class PcAliasCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        Player player = (Player) sender;

        // 플레이어 권한 그대로 Pixelmon /pc 실행
        boolean ok = Bukkit.getServer().dispatchCommand(player, "pc");

        if (!ok) {
            player.sendMessage("§c/pc 명령어를 찾을 수 없습니다. Pixelmon 모드가 제대로 설치되어 있는지 확인해주세요.");
        }

        return true;
    }
}
