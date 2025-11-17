package com.minkang.ultimate.commands;

import com.minkang.ultimate.Main;
import org.bukkit.command.*;

public class LockTokenCommand implements CommandExecutor {
    private final Main plugin;
    public LockTokenCommand(Main p){ this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        s.sendMessage("§c잠금권 기능은 비활성화되었습니다.");
        return true;
    }
}
