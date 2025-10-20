package com.minkang.usp2.lock;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.minkang.ultimate.Main;

/** Utility methods for lock signs & target blocks. */
public class LockUtils {

    public static final NamespacedKey KEY_TICKET =
            new NamespacedKey(JavaPlugin.getPlugin(Main.class), "lock_ticket");

    /** Containers we protect. */
    public static boolean isLockable(Block b) {
        if (b == null) return false;
        Material m = b.getType();
        switch (m) {
            case CHEST:
            case TRAPPED_CHEST:
            case BARREL:
                return true;
            default:
                return false;
        }
    }

    /** True if the item is our issued lock ticket sign. */
    public static boolean isOurTicket(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer flag = pdc.get(KEY_TICKET, PersistentDataType.INTEGER);
        return flag != null && flag == 1;
    }

    /** Resolve the container block a sign is attached to. */
    public static Block getAttachedBlock(Block signBlock) {
        if (signBlock == null) return null;
        BlockData data = signBlock.getBlockData();
        if (data instanceof WallSign) {
            BlockFace facing = ((WallSign) data).getFacing();
            return signBlock.getRelative(facing.getOppositeFace());
        }
        // Standing sign placed on top of container
        return signBlock.getRelative(BlockFace.DOWN);
    }
}