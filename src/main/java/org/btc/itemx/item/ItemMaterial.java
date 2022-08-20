package org.btc.itemx.item;

import org.btc.itemx.ItemX;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ItemMaterial {

    private static final File ITEM_MATERIALS = new File(ItemX.getInstance().getDataFolder() + "\\Materials.yml");

    private static HashMap<String, List<String>> materialMap = new HashMap<>();

    public static List<String> getAllRegisteredMaterials() {
        return materialMap.keySet().stream().toList();
    }

    public static List<String> getItemIdByMaterial(String materialType) {
        return materialMap.get(materialType);
    }

    public static void init() {
        if (!ITEM_MATERIALS.exists()) {
            try {
                ITEM_MATERIALS.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void read() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(ITEM_MATERIALS);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            List<String> itemIds = config.getStringList(key);
            materialMap.put(key, itemIds);
        }
    }
}
