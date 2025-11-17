package com.minkang.ultimate.commands;

import com.minkang.ultimate.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * 잠금권/잠금 기능은 더 이상 사용하지 않습니다.
 * 이 커맨드는 안내 메시지만 보여 줍니다.
 */
public class LockCommand implements CommandExecutor {
    private final Main plugin;
    public LockCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§c잠금권/잠금 기능은 비활성화되었습니다.");
        return true;
    }
}
