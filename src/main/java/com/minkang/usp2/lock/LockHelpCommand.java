package com.minkang.usp2.lock;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LockHelpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.DARK_GRAY + "---------------- " + ChatColor.GOLD + "잠금 도움말" + ChatColor.DARK_GRAY + " ----------------");
        sender.sendMessage(ChatColor.YELLOW + "① " + ChatColor.WHITE + "/잠금권 지급 " + ChatColor.GRAY + "[플레이어] [수량]");
        sender.sendMessage(ChatColor.DARK_GRAY + "   └ " + ChatColor.GRAY + "전용 표지판을 지급합니다.");
        sender.sendMessage(ChatColor.YELLOW + "② " + ChatColor.WHITE + "전용표지판" + ChatColor.GRAY + "을 들고 상자/배럴에 " + ChatColor.AQUA + "벽걸이 표지판" + ChatColor.GRAY + " 부착");
        sender.sendMessage(ChatColor.DARK_GRAY + "   └ 1줄: " + ChatColor.GOLD + "[ 잠금 ]");
        sender.sendMessage(ChatColor.DARK_GRAY + "   └ 2줄/3줄: " + ChatColor.WHITE + "닉네임,닉네임  " + ChatColor.DARK_GRAY + "(쉼표로 여러 명 가능)");
        sender.sendMessage(ChatColor.DARK_GRAY + "   └ 해당 닉네임들이 공용으로 열 수 있습니다. (오피/권한 " + ChatColor.WHITE + "usp.lock.bypass" + ChatColor.DARK_GRAY + " 는 예외)");
        sender.sendMessage(ChatColor.DARK_GRAY + "-----------------------------------------------------");
        return true;
    }
}