
package com.minkang.ultimate.commands;

import com.minkang.ultimate.Main;
import com.minkang.ultimate.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class PostCommand implements CommandExecutor {

    public enum Type { TRADE, DEAL }

    private final Main plugin;
    private final Type type;

    public PostCommand(Main plugin, Type type) {
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "플레이어만 사용 가능합니다.");
            return true;
        }
        Player p = (Player) sender;
        if (args.length < 1) {
            p.sendMessage(ChatColor.YELLOW + "사용법: /" + label + " <내용>");
            return true;
        }
        // Build content string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(args[i]);
        }
        String content = sb.toString();

        FileConfiguration cfg = plugin.getConfig();

        // Fee logic
        double fee = 0D;
        String feePath = (type == Type.TRADE) ? "post.fee.trade" : "post.fee.deal";
        if (cfg.isSet(feePath)) {
            try { fee = cfg.getDouble(feePath); } catch (Exception ignored) {}
        }

        EconomyManager eco = plugin.eco();
        boolean hasVault = Bukkit.getPluginManager().getPlugin("Vault") != null;
        if (fee > 0 && hasVault && eco != null) {
            double bal = eco.bal(p);
            if (bal < fee) {
                p.sendMessage(ChatColor.RED + "잔액이 부족합니다. 필요 금액: " + ChatColor.YELLOW + String.format("%,.0f", fee)
                        + ChatColor.WHITE + " / 보유: " + ChatColor.YELLOW + String.format("%,.0f", bal));
                return true;
            }
            boolean ok = false;
            try { ok = eco.withdraw(p, fee); } catch (Throwable ignored) {}
            if (!ok) {
                p.sendMessage(ChatColor.RED + "결제 처리에 실패했습니다.");
                return true;
            }
            p.sendMessage(ChatColor.GOLD + "게시글 이용료 " + ChatColor.WHITE + String.format("%,.0f", fee) + ChatColor.GOLD + "원이 차감되었습니다.");
        }

        // Format message
        String formatPath = (type == Type.TRADE) ? "post.format.trade" : "post.format.deal";
        String msg = cfg.getString(formatPath);
        if (msg == null || msg.trim().isEmpty()) {
            msg = (type == Type.TRADE ? "&6[장사글] &f%player%: &e%content%" : "&b[거래글] &f%player%: &f%content%");
        }
        msg = ChatColor.translateAlternateColorCodes('&', msg
                .replace("%player%", p.getName())
                .replace("%content%", content));

        // Broadcast
        Bukkit.broadcastMessage(msg);

        // Sound
        {
            // Per-type sound with backward compatibility
            String typeKey = "deal";
            if (label != null) {
                if (label.equalsIgnoreCase("장사글") || label.equals("장")) typeKey = "trade";
                else if (label.equalsIgnoreCase("거래글") || label.equals("거")) typeKey = "deal";
            }
            String base = "post.sound." + typeKey + ".";
            boolean enable = cfg.getBoolean(base + "enabled", cfg.getBoolean("post.sound.enabled", true));
            if (enable) {
                String sn = cfg.getString(base + "name", cfg.getString("post.sound.name", "ENTITY_EXPERIENCE_ORB_PICKUP"));
                float vol = (float) cfg.getDouble(base + "volume", cfg.getDouble("post.sound.volume", 1.0));
                float pit = (float) cfg.getDouble(base + "pitch", cfg.getDouble("post.sound.pitch", 1.2));
                Sound sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                try {
                    sound = Sound.valueOf(sn.toUpperCase());
                } catch (Throwable ignored) {}
                for (Player t : Bukkit.getOnlinePlayers()) {
                    try { t.playSound(t.getLocation(), sound, vol, pit); } catch (Throwable ignored) {}
                }
            }
        }

        return true;
    }
}
