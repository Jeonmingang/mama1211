package com.minkang.usp2.lock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.minkang.ultimate.Main;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LockStore {

    private final File file;
    private final Map<String, LockEntry> locks = new HashMap<>();

    public LockStore() {
        this.file = new File(JavaPlugin.getPlugin(Main.class).getDataFolder(), "locks.yml");
        load();
    }

    public static String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public LockEntry get(Location loc) {
        return locks.get(key(loc));
    }

    public void put(Location loc, LockEntry entry) {
        locks.put(key(loc), entry);
        saveAsync();
    }

    public void remove(Location loc) {
        locks.remove(key(loc));
        saveAsync();
    }

    public boolean isLocked(Location loc) {
        return get(loc) != null;
    }

    private void load() {
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        locks.clear();
        if (cfg.isConfigurationSection("locks")) {
            for (String k : cfg.getConfigurationSection("locks").getKeys(false)) {
                String base = "locks." + k + ".";
                // v2 format: allowedNames: []
                List<String> allowed = cfg.getStringList(base + "allowedNames");
                if (allowed == null) allowed = new ArrayList<>();
                // v1 fallback: ownerName
                String ownerName = cfg.getString(base + "ownerName", null);
                if (ownerName != null && !ownerName.trim().isEmpty() && allowed.isEmpty()) {
                    allowed = Collections.singletonList(ownerName);
                }
                long created = cfg.getLong(base + "createdAt", System.currentTimeMillis());
                locks.put(k, new LockEntry(allowed, created));
            }
        }
    }

    private void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(Main.class), this::saveNow);
    }

    private synchronized void saveNow() {
        try {
            FileConfiguration cfg = new YamlConfiguration();
            for (Map.Entry<String, LockEntry> e : locks.entrySet()) {
                String base = "locks." + e.getKey() + ".";
                cfg.set(base + "allowedNames", new ArrayList<>(e.getValue().getAllowedNames()));
                cfg.set(base + "createdAt", e.getValue().createdAt);
            }
            cfg.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static class LockEntry {
        private final Set<String> allowedNamesLower;
        public final long createdAt;

        public LockEntry(Collection<String> allowedNames, long createdAt) {
            this.allowedNamesLower = allowedNames == null ? new HashSet<>() :
                    allowedNames.stream().filter(s -> s != null && !s.trim().isEmpty())
                            .map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
            this.createdAt = createdAt;
        }

        public Set<String> getAllowedNames() {
            // Return as original-case not tracked; use lower-cased set
            return new HashSet<>(allowedNamesLower);
        }

        public boolean canOpen(Player p) {
            if (p.isOp() || p.hasPermission("usp.lock.bypass")) return true;
            return allowedNamesLower.contains(p.getName().toLowerCase(Locale.ROOT));
        }

        public String allowedNamesPretty() {
            if (allowedNamesLower.isEmpty()) return "(없음)";
            return String.join(", ", allowedNamesLower);
        }
    }
}