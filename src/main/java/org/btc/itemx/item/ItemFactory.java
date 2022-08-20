package org.btc.itemx.item;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.btc.server.utils.CompoundUtil;
import org.btc.server.utils.StringUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemFactory {

    public static ItemStack buildItem(HashMap map) {
        return buildItem(map, false);
    }

    public static ItemStack buildItem(HashMap map, Boolean dynamic) {
        Material material = Material.getMaterial((String) map.getOrDefault("Material", "AIR"));
        Integer modelData = (Integer) map.get("Model");
        String displayName = (String) map.get("Name");
        List<String> lore = (List<String>) map.get("Lore");
        Boolean unbreakable = (Boolean) map.get("Unbreakable");
        List<String> enchantmentCompounds = (List<String>) map.get("Enchantments");
        List<String> itemFlags = (List<String>) map.get("ItemFlags");
        String id = (String) map.get("ID");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(modelData);
        meta.setDisplayName(displayName);
        if (!dynamic) {
            meta.setLore(lore);
        } else {
            List<String> realLore = new ArrayList<>();
            for (String loreLine : lore) {
                realLore.add(StringUtil.replaceRandomInt(loreLine));
            }
            meta.setLore(realLore);
        }
        meta.setUnbreakable(unbreakable);
        for (String enchantmentCompound : enchantmentCompounds) {
            if (CompoundUtil.isCompound(enchantmentCompound)) {
                String[] tempList = CompoundUtil.get(enchantmentCompound);
                if (tempList != null) {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(tempList[0].toLowerCase()));
                    Integer level;
                    Boolean ignoreRestriction;
                    try {
                        level = Integer.valueOf(tempList[1]);
                    } catch (NumberFormatException e) {
                        level = 1;
                    }
                    try{
                        ignoreRestriction = Boolean.getBoolean(tempList[2]);
                    }catch (IndexOutOfBoundsException e){
                        ignoreRestriction = false;
                    }
                    meta.addEnchant(enchantment, level, ignoreRestriction);
                }
            }
        }
        for (String itemFlag : itemFlags) {
            ItemFlag flag = ItemFlag.valueOf(itemFlag);
            meta.addItemFlags(flag);
        }
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        String hexPotionColor = (String) map.get("PotionColor");
        if (material.equals(Material.POTION) || material.equals(Material.SPLASH_POTION) || material.equals(Material.LINGERING_POTION)) {
            if (hexPotionColor.replaceAll("\\d|[abcdef]", "").equals("")) {
                Integer potionColor = Integer.valueOf(hexPotionColor, 16);
                nbtItem.setInteger("CustomPotionColor", potionColor);
            }
        }
        List<String> nbtCompounds = (List<String>) map.get("NBT");
        for (String nbtCompound : nbtCompounds) {
            nbtItem.mergeCompound(new NBTContainer(nbtCompound));
        }
        nbtItem.mergeCompound(new NBTContainer("{ItemID:" + id + "}"));
        return nbtItem.getItem();
    }


}
