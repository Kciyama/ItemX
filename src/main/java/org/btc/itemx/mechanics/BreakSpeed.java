package org.btc.itemx.mechanics;

import org.btc.itemx.item.ItemManager;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BreakSpeed {

    private static HashMap<String, Float> breakSpeedMap = new HashMap<>();

    public static void registerBreakSpeed(String id, Float breakSpeed) {
        breakSpeedMap.put(id, breakSpeed);
    }

    public static Float getBreakSpeedFromTool(ItemStack item) {
        String id = ItemManager.getIdOrNull(item);
        if (id != null) {
            return breakSpeedMap.getOrDefault(id, 1f);
        } else {
            if (ItemManager.isMadeOf(item, "Wood")) {
                return 2f;
            } else if (ItemManager.isMadeOf(item, "Stone")) {
                return 4f;
            } else if (ItemManager.isMadeOf(item, "Iron")) {
                return 6f;
            } else if (ItemManager.isMadeOf(item, "Gold")) {
                return 12f;
            } else if (ItemManager.isMadeOf(item, "Diamond")) {
                return 8f;
            } else if (ItemManager.isMadeOf(item, "Netherite")) {
                return 9f;
            } else {
                return 1f;
            }
        }
    }

}
