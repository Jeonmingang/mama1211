package com.minkang.ultimate.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Simple dynamic pricing + quota manager per MATERIAL.
 * Config path (shops.yml):
 *
 * market:
 *   interval-seconds: 1800
 *   items:
 *     DIAMOND:
 *       min: 100.0
 *       max: 250.0
 *       quota: 128
 *     IRON_INGOT:
 *       min: 10.0
 *       max: 30.0
 *       quota: 512
 */
public final class DynamicPricingManager {
    public static class ItemConfig { public double min; public double max; public int quota; }
    private static DynamicPricingManager instance;
    public static DynamicPricingManager get(Plugin plugin) {
        if (instance == null) instance = new DynamicPricingManager(plugin);
        return instance;
    }

    public static class PriceState {
        public double price;
        public int quotaLeft;
        public long nextTs;
    }

    private final Plugin plugin;
    private final Random random = new Random();
    private final Map<Material, Double> minPrice = new HashMap<>();
    private final Map<Material, Double> maxPrice = new HashMap<>();
    private final Map<Material, Integer> quota = new HashMap<>();
    private final Map<Material, PriceState> live = new HashMap<>();

    private int intervalSeconds = 1800;
    private File dataFile;
    private File shopsFile;
    private FileConfiguration shopsCfg;
    private YamlConfiguration data;
    private BukkitTask task;

    private DynamicPricingManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "dynamic_market.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        this.shopsFile = new File(plugin.getDataFolder(), "shops.yml");
        this.shopsCfg = YamlConfiguration.loadConfiguration(shopsFile);
        loadConfig();
        tick(false);
        schedule();
    }

    public void reload() {
        loadConfig();
        tick(true);
    }

    public int getIntervalSeconds() { return intervalSeconds; }

    private void loadConfig() {
        try {
            this.shopsCfg = YamlConfiguration.loadConfiguration(this.shopsFile);
            ConfigurationSection m = shopsCfg.getConfigurationSection("market");
            minPrice.clear(); maxPrice.clear(); quota.clear();
            if (m != null) {
                intervalSeconds = m.getInt("interval-seconds", 1800);
                ConfigurationSection items = m.getConfigurationSection("items");
                if (items != null) {
                    for (String key : items.getKeys(false)) {
                        try {
                            Material mat = Material.matchMaterial(key);
                            if (mat == null) continue;
                            double min = items.getDouble(key + ".min");
                            double max = items.getDouble(key + ".max");
                            int q = items.getInt(key + ".quota", 64);
                            if (min <= 0 || max <= 0 || max < min) continue;
                            minPrice.put(mat, min);
                            maxPrice.put(mat, max);
                            quota.put(mat, q);
                        } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Dynamic market config load failed: " + t.getMessage());
        }
    }

    private void schedule() {
        if (task != null) task.cancel();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 20L, 20L);
    }

    private void onTick() {
        tick(false);
    }

    private void tick(boolean force) {
        long now = System.currentTimeMillis();
        for (Material m : minPrice.keySet()) {
            PriceState st = live.computeIfAbsent(m, k -> new PriceState());
            if (st.nextTs <= now || force || st.price <= 0) {
                // roll new price/quota
                double min = minPrice.get(m);
                double max = maxPrice.get(m);
                double price = min + (max - min) * random.nextDouble();
                price = Math.round(price * 100.0) / 100.0; // 2 decimals
                st.price = price;
                st.quotaLeft = quota.getOrDefault(m, 64);
                st.nextTs = now + intervalSeconds * 1000L;
            }
        }
        save();
    }

    private void save() {
        try {
            for (Map.Entry<Material, PriceState> e : live.entrySet()) {
                String key = e.getKey().name();
                PriceState st = e.getValue();
                data.set("state."+key+".price", st.price);
                data.set("state."+key+".quotaLeft", st.quotaLeft);
                data.set("state."+key+".nextTs", st.nextTs);
            }
            data.set("intervalSeconds", intervalSeconds);
            data.save(dataFile);
        } catch (IOException ignored) {}
    }

    public PriceState get(Material m) {
        return live.get(m);
    }

    public long getMillisUntilNext(Material m) {
        PriceState st = live.get(m);
        if (st == null) return 0L;
        long d = st.nextTs - System.currentTimeMillis();
        return Math.max(0L, d);
    }

    /** Try sell {qty} items of material m; returns money to pay, or -1 if blocked (sold out). */

    public java.util.Set<Material> getMarketItems(){
        return new java.util.HashSet<>(minPrice.keySet());
    }

    public ItemConfig getItemConfig(Material m){
        if (!minPrice.containsKey(m)) return null;
        ItemConfig ic = new ItemConfig();
        ic.min = minPrice.get(m);
        ic.max = maxPrice.get(m);
        ic.quota = quota.getOrDefault(m, 64);
        return ic;
    }

    public void setIntervalSecondsAndSave(int seconds){
        this.intervalSeconds = Math.max(10, seconds);
        if (shopsCfg == null) shopsCfg = YamlConfiguration.loadConfiguration(shopsFile);
        ConfigurationSection market = shopsCfg.getConfigurationSection("market");
        if (market == null) market = shopsCfg.createSection("market");
        market.set("interval-seconds", this.intervalSeconds);
        try { shopsCfg.save(shopsFile); } catch (Exception ignored) {}
    }

    public void setItemConfigAndSave(Material m, double min, double max, int q){
        if (shopsCfg == null) shopsCfg = YamlConfiguration.loadConfiguration(shopsFile);
        ConfigurationSection market = shopsCfg.getConfigurationSection("market");
        if (market == null) market = shopsCfg.createSection("market");
        ConfigurationSection items = market.getConfigurationSection("items");
        if (items == null) items = market.createSection("items");
        ConfigurationSection sec = items.getConfigurationSection(m.name());
        if (sec == null) sec = items.createSection(m.name());
        sec.set("min", min);
        sec.set("max", max);
        sec.set("quota", q);
        try { shopsCfg.save(shopsFile); } catch (Exception ignored) {}
        // also update live maps
        minPrice.put(m, min);
        maxPrice.put(m, max);
        quota.put(m, q);
    }

    public void reroll(Material m){
        if (!minPrice.containsKey(m)) return;
        double min = minPrice.get(m);
        double max = maxPrice.get(m);
        double price = min + (max - min) * random.nextDouble();
        price = Math.round(price * 100.0) / 100.0;
        PriceState st = live.computeIfAbsent(m, k -> new PriceState());
        st.price = price;
        st.quotaLeft = quota.getOrDefault(m, 64);
        st.nextTs = System.currentTimeMillis() + intervalSeconds * 1000L;
        save();
    }


    public double trySell(Material m, int qty) {
        PriceState st = live.get(m);
        if (st == null) return -1;
        if (st.quotaLeft <= 0) return -1;
        int sell = Math.min(qty, st.quotaLeft);
        st.quotaLeft -= sell;
        save();
        return sell * st.price;
    }
}