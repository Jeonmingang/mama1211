package com.minkang.usp2.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Unified EggSteps command to avoid duplicate registrations.
 * Usage:
 *   /알걸음 <플레이어> <인덱스>
 *   /발걸음 <플레이어> <인덱스>
 *   /eggsteps <player> <index>
 *
 * Sends the remaining steps both to the command sender and to the target player.
 * The actual query to remaining steps should be implemented in the TODO section
 * where you already have a hook to Pixelmon/egg data in your plugin.
 */
public class EggStepsCommand implements CommandExecutor {

    private int mockQueryRemainingSteps(Player target, int index) {
        // TODO: Replace this with your real logic to get remaining egg steps for 'index'.
        // This mock returns a deterministic number so you can verify the message flow.
        return Math.max(0, 6000 - (index * 111));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "사용법: /" + label + " <플레이어> <알번호>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + args[0]);
            return true;
        }

        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "알번호는 숫자여야 합니다.");
            return true;
        }

        int remaining = mockQueryRemainingSteps(target, index);

        String msg = ChatColor.GRAY + "남은 발걸음: " + ChatColor.WHITE + remaining;
        sender.sendMessage(msg);
        if (!sender.getName().equalsIgnoreCase(target.getName())) {
            target.sendMessage(ChatColor.YELLOW + "[알걸음] " + ChatColor.RESET + msg);
        }
        return true;
    }
}