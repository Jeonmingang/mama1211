package com.minkang.ultimate.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /전설시간, /메가보스, /메가보스시간, /일반포켓몬 전용 래퍼
 *
 * Pixelmon 1.21.1 의 /checkspawns 서브 명령을 대신 실행해 준다.
 * - /전설시간 -> /checkspawns legendary
 * - /메가보스, /메가보스시간 -> /checkspawns megaboss
 * - /일반포켓몬 -> /checkspawns
 *
 * 권한 체크 없이 누구나 사용 가능해야 하므로 별도의 permission 검사 없이
 * 잠시 OP 로 올렸다가 다시 돌려놓는 방식으로 실행한다.
 */
public class CheckSpawnsAliasCommand implements CommandExecutor {
    private final String mode; // null 또는 빈 문자열이면 일반 체크(/checkspawns)

    public CheckSpawnsAliasCommand(String mode) {
        this.mode = mode;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player p = (Player) sender;

        // 권한은 전혀 요구하지 않도록 별도의 permission 체크를 하지 않는다.
        boolean wasOp = p.isOp();
        try {
            if (!wasOp) {
                // Pixelmon 쪽에서 위치/바이옴 기준으로 계산하므로
                // 플레이어 컨텍스트 + OP 권한으로 실행한다.
                p.setOp(true);
            }

            // mode 가 비어 있으면 기본 /checkspawns, 그 외에는 서브 모드 포함
            String m = (mode == null) ? "" : mode.trim();
            if (m.isEmpty()) {
                p.performCommand("checkspawns");
            } else {
                p.performCommand("checkspawns " + m);
            }
        } finally {
            if (!wasOp) {
                p.setOp(false);
            }
        }
        return true;
    }
}
