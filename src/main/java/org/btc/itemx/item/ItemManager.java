package org.btc.itemx.item;

import de.tr7zw.nbtapi.NBTItem;
import org.btc.itemx.ItemX;
import org.btc.itemx.enums.ToolType;
import org.btc.itemx.mechanics.*;
import org.btc.server.utils.CompoundUtil;
import org.btc.server.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemManager {

    private static HashMap<String, ItemStack> itemMap = new HashMap<>();
    private static HashMap<String, YamlConfiguration> dynamicItemMap = new HashMap<>();

    public static List<String> getItemList() {
        List<String> tempList = itemMap.keySet().stream().toList();
        return tempList;
    }

    private static List<String> getDynamicItemList() {
        return dynamicItemMap.keySet().stream().toList();
    }

    public static ItemStack getItem(String id) {
        if (itemMap.containsKey(id)) {
            return itemMap.get(id);
        } else if (dynamicItemMap.containsKey(id)) {
            return getDynamicItem(id);
        } else {
            return new ItemStack(Material.valueOf(id.toUpperCase()));
        }
    }

    public static String getIdOrNull(ItemStack item) {
        try {
            String id = new NBTItem(item).getString("ItemID");
            if (id.equals("")) {
                return null;
            } else {
                return id;
            }
        } catch (NullPointerException ignored) {
            return null;
        }
    }

    public static Boolean isMadeOf(ItemStack item, String materialType) {
        String id = getIdOrNull(item);
        if (ItemMaterial.getAllRegisteredMaterials().contains(materialType)) {
            List<String> itemIds = ItemMaterial.getItemIdByMaterial(materialType);
            if (id != null) {
                if (itemIds.contains(id)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                for (String itemId : itemIds) {
                    ItemStack compare = ItemManager.getItem(itemId);
                    Material compareType = compare.getType();
                    if (compare.equals(new ItemStack(compareType))) {
                        if (item.getType().equals(compareType)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } else {
            return false;
        }
    }

    public static Boolean isTool(ItemStack item, ToolType type) {
        Material material = item.getType();
        switch (type) {
            case AXE:
                return material.equals(Material.WOODEN_AXE) ||
                        material.equals(Material.STONE_AXE) ||
                        material.equals(Material.IRON_AXE) ||
                        material.equals(Material.GOLDEN_AXE) ||
                        material.equals(Material.DIAMOND_AXE) ||
                        material.equals(Material.NETHERITE_AXE);
            case PICKAXE:
                return material.equals(Material.WOODEN_PICKAXE) ||
                        material.equals(Material.STONE_PICKAXE) ||
                        material.equals(Material.IRON_PICKAXE) ||
                        material.equals(Material.GOLDEN_PICKAXE) ||
                        material.equals(Material.DIAMOND_PICKAXE) ||
                        material.equals(Material.NETHERITE_PICKAXE);
            case SHOVEL:
                return material.equals(Material.WOODEN_SHOVEL) ||
                        material.equals(Material.STONE_SHOVEL) ||
                        material.equals(Material.IRON_SHOVEL) ||
                        material.equals(Material.GOLDEN_SHOVEL) ||
                        material.equals(Material.DIAMOND_SHOVEL) ||
                        material.equals(Material.NETHERITE_SHOVEL);
            case HOE:
                return material.equals(Material.WOODEN_HOE) ||
                        material.equals(Material.STONE_HOE) ||
                        material.equals(Material.IRON_HOE) ||
                        material.equals(Material.GOLDEN_HOE) ||
                        material.equals(Material.DIAMOND_HOE) ||
                        material.equals(Material.NETHERITE_HOE);
            case SHEARS:
                return material.equals(Material.SHEARS);
            default:
                return false;
        }
    }

    public static void registerItem(YamlConfiguration config, String id) {
        try {
            HashMap tempMap = new HashMap();
            tempMap.put("Material", config.getString(id + ".Material").toUpperCase());
            tempMap.put("Model", config.getInt(id + ".Model"));
            tempMap.put("Name", StringUtil.toFormatted(config.getString(id + ".Name")));
            List<String> tempLore = config.getStringList(id + ".Lore");
            List<String> lore = new ArrayList<>();
            for (String loreLine : tempLore) {
                if (StringUtil.containsRandomInt(loreLine)) {
                    registerDynamicItem(id, config);
                    return;
                } else {
                    lore.add(StringUtil.toFormatted(loreLine));
                }
            }
            tempMap.put("Lore", lore);
            tempMap.put("Unbreakable", config.getBoolean(id + ".Unbreakable"));
            tempMap.put("Enchantments", config.getStringList(id + ".Enchantments"));
            tempMap.put("ItemFlags", config.getStringList(id + ".ItemFlags"));
            tempMap.put("PotionColor", config.getString(id + ".PotionColor"));
            tempMap.put("NBT", config.getStringList(id + ".NBT"));
            tempMap.put("ID", id);
            ItemStack item = ItemFactory.buildItem(tempMap);
            itemMap.put(id, item);
            ItemX.info(StringUtil.toFormatted("&b已注册物品:&a " + id));
            if (config.contains(id + ".Mechanics.Edible")) {
                Integer foodLevel = config.getInt(id + ".Mechanics.Edible.FoodLevel", 0);
                Integer saturation = config.getInt(id + ".Mechanics.Edible.Saturation", 0);
                Double heal = config.getDouble(id + ".Mechanics.Edible.Heal", 0);
                Double healPercent = config.getDouble(id + ".Mechanics.Edible.HealPercent", 0);
                List<String> potionEffectCompounds = config.getStringList(id + ".Mechanics.Edible.PotionEffects");
                Boolean removeBottle = config.getBoolean(id + ".Mechanics.Edible.RemoveBottle");
                String leftover = config.getString(id + ".Mechanics.Edible.Leftover");
                if (potionEffectCompounds.size() > 0) {
                    Edible.registerEdibleMechanic(id, foodLevel, saturation, heal, healPercent, potionEffectCompounds, removeBottle, leftover);
                } else {
                    Edible.registerEdibleMechanic(id, foodLevel, saturation, heal, healPercent, null, removeBottle, leftover);
                }
                ItemX.info(StringUtil.toFormatted("&b已注册可食用机制: &a" + id));
            }
            if (config.contains(id + ".Mechanics.Skills")) {
                if (ItemX.getInstance().getMythicMobsHook().isEnabled()) {
                    List<String> skillMechanics = config.getStringList(id + ".Mechanics.Skills");
                    MythicSkill.registerItemSkillMechanic(id, skillMechanics);
                    ItemX.info(StringUtil.toFormatted("&b已注册MM技能: &a" + id));
                }
            }
            if (config.contains(id + ".Mechanics.Commands")) {
                try {
                    List<Map> maps = (List<Map>) config.get(id + ".Mechanics.Commands");
                    Command.registerCommandMechanic(id, maps);
                } catch (NullPointerException ignored) {
                }
            }
            if (config.contains(id + ".Mechanics.NoteBlock")) {
                try {
                    BlockData blockData = Bukkit.createBlockData(Material.NOTE_BLOCK, config.getString(id + ".Mechanics.NoteBlock.BlockData"));
                    NoteBlock.registerNoteBlockData(id, blockData);
                    Sound breakSound = Sound.valueOf(config.getString(id + ".Mechanics.NoteBlock.BreakSound"));
                    Sound placeSound = Sound.valueOf(config.getString(id + ".Mechanics.NoteBlock.PlaceSound"));
                    Sound diggingSound = Sound.valueOf(config.getString(id + ".Mechanics.NoteBlock.DiggingSound"));
                    Float hardness = Float.valueOf(config.get(id + ".Mechanics.NoteBlock.Hardness").toString());
                    ToolType bestTool = ToolType.valueOf(config.getString(id + ".Mechanics.NoteBlock.BestTool").toUpperCase());
                    List<String> breakableBy = config.getStringList(id + ".Mechanics.NoteBlock.BreakableBy");
                    NoteBlock.registerNoteBlockMechanic(id, hardness, breakSound, placeSound, diggingSound, bestTool, breakableBy);
                    MemorySection dropMap = (MemorySection) config.get(id + ".Mechanics.NoteBlock.Drops");
                    NoteBlock.registerNoteBlockDrops(id, dropMap);
                    ItemX.info(StringUtil.toFormatted("&b已注册音符盒机制: &a" + id));
                } catch (NullPointerException ignored) {
                }
            }
            if (config.contains(id + ".Mechanics.DiggingSpeed")) {
                Float diggingSpeed = Float.valueOf(config.getString(id + ".Mechanics.DiggingSpeed"));
                BreakSpeed.registerBreakSpeed(id, diggingSpeed);
            }
            if (config.contains(id + ".Mechanics.Crop")) {
                List<String> dropTable = config.getStringList(id + ".Mechanics.Crop.DropTable");
                Sound harvestSound = Sound.valueOf(config.getString(id + ".Mechanics.Crop.HarvestSound"));
                if (config.contains(id + ".Mechanics.Crop.MatureTicks") && config.contains(id + ".Mechanics.Crop.NextStage") && config.contains(id + ".Mechanics.Crop.GrowSound")) {
                    Integer matureTicks = config.getInt(id + ".Mechanics.Crop.MatureTicks");
                    String nextStage = config.getString(id + ".Mechanics.Crop.NextStage");
                    Sound growSound = Sound.valueOf(config.getString(id + ".Mechanics.Crop.GrowSound"));
                    Crop.registerCropMechanic(id, dropTable, matureTicks, nextStage, growSound, harvestSound);
                } else {
                    Crop.registerCropMechanic(id, dropTable, harvestSound);
                }
            }
            if (config.contains(id + ".Mechanics.Seed")) {
                List<String> probableCrops = config.getStringList(id + ".Mechanics.Seed.ProbableCrops");
                List<String> tempCanPlantOn = config.getStringList(id + ".Mechanics.Seed.CanPlantOn");
                List<Object> canPlantOn = new ArrayList<>();
                for (String identifier : tempCanPlantOn) {
                    if (identifier.contains("[") && identifier.endsWith("]")) {
                        BlockData blockData = Bukkit.createBlockData(identifier);
                        canPlantOn.add(blockData);
                    }
                    if (NoteBlock.containsId(identifier)) {
                        BlockData blockData = NoteBlock.getBlockDataById(id);
                        canPlantOn.add(blockData);
                    } else {
                        Material material = Material.getMaterial(identifier.toUpperCase());
                        canPlantOn.add(material);
                    }
                }
                Sound plantSound = Sound.valueOf(config.getString(id + ".Mechanics.Seed.PlantSound"));
                Crop.registerSeedMechanic(id, probableCrops, canPlantOn, plantSound);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void registerItemRecipe(YamlConfiguration config, String id) {
        List<Map> recipes = (List<Map>) config.getList(id + ".Recipes");
        if (recipes != null) {
            for (Map map : recipes) {
                String recipeType = (String) map.get("Type");
                Integer amount = (Integer) map.getOrDefault("Amonut", 1);
                if (recipeType.equals("Shaped")) {
                    List<String> ingredientIds = (List<String>) map.get("Recipe");
                    List<RecipeChoice.ExactChoice> tempIngredients = new ArrayList<>();
                    for (String choiceId : ingredientIds) {
                        if (choiceId != null) {
                            RecipeChoice.ExactChoice choice = ItemRecipe.getChoice(choiceId);
                            tempIngredients.add(choice);
                        }
                    }
                    RecipeChoice.ExactChoice[] ingredients = tempIngredients.toArray(new RecipeChoice.ExactChoice[0]);
                    ItemRecipe.registerShapedRecipe(id, amount, ingredients);
                    ItemX.info(ChatColor.AQUA + "已注册 " + ChatColor.GREEN + id + ChatColor.AQUA + " 的工作台有序配方");
                } else if (recipeType.equals("Shapeless")) {
                    List<String> ingredientIds = (List<String>) map.get("Recipe");
                    List<RecipeChoice.ExactChoice> tempIngredients = new ArrayList<>();
                    for (String choiceId : ingredientIds) {
                        if (choiceId != null) {
                            RecipeChoice.ExactChoice choice = ItemRecipe.getChoice(choiceId);
                            tempIngredients.add(choice);
                        }
                    }
                    RecipeChoice.ExactChoice[] ingredients = tempIngredients.toArray(new RecipeChoice.ExactChoice[0]);
                    ItemRecipe.registerShapelessRecipe(id, amount, ingredients);
                    ItemX.info(ChatColor.AQUA + "已注册 " + ChatColor.GREEN + id + ChatColor.AQUA + " 的工作台无序配方");
                } else if (recipeType.equals("Furnace")) {
                    String ingredientId = (String) map.get("Ingredient");
                    if (ingredientId != null) {
                        RecipeChoice.ExactChoice ingredient = ItemRecipe.getChoice(ingredientId);
                        Integer cookingTime = Integer.valueOf(map.get("CookingTime").toString());
                        Float exp = Float.valueOf(map.get("Exp").toString());
                        ItemRecipe.registerFurnaceRecipe(id, amount, ingredient, exp, cookingTime);
                        ItemX.info(ChatColor.AQUA + "已注册 " + ChatColor.GREEN + id + ChatColor.AQUA + " 的熔炉配方");
                    }
                } else if (recipeType.equals("BlastFurnace")) {
                    String ingredientId = (String) map.get("Ingredient");
                    if (ingredientId != null) {
                        RecipeChoice.ExactChoice ingredient = ItemRecipe.getChoice(ingredientId);
                        Integer cookingTime = Integer.valueOf(map.get("CookingTime").toString());
                        Float exp = Float.valueOf(map.get("Exp").toString());
                        ItemRecipe.registerBlastFurnaceRecipe(id, amount, ingredient, exp, cookingTime);
                        ItemX.info(ChatColor.AQUA + "已注册 " + ChatColor.GREEN + id + ChatColor.AQUA + " 的高炉配方");
                    }
                } else if (recipeType.equals("Campfire")) {
                    String ingredientId = (String) map.get("Ingredient");
                    if (ingredientId != null) {
                        RecipeChoice.ExactChoice ingredient = ItemRecipe.getChoice(ingredientId);
                        Integer cookingTime = Integer.valueOf(map.get("CookingTime").toString());
                        ItemRecipe.registerCampfireRecipe(id, amount, ingredient, Float.valueOf(0), cookingTime);
                        ItemX.info(ChatColor.AQUA + "已注册 " + ChatColor.GREEN + id + ChatColor.AQUA + " 的营火配方");
                    }
                } else if (recipeType.equals("Smoker")) {
                    String ingredientId = (String) map.get("Ingredient");
                    if (ingredientId != null) {
                        RecipeChoice.ExactChoice ingredient = ItemRecipe.getChoice(ingredientId);
                        Integer cookingTime = Integer.valueOf(map.get("CookingTime").toString());
                        Float exp = Float.valueOf(map.get("Exp").toString());
                        ItemRecipe.registerSmokerRecipe(id, amount, ingredient, exp, cookingTime);
                        ItemX.info(ChatColor.AQUA + "已注册 " + ChatColor.GREEN + id + ChatColor.AQUA + " 的烟熏炉配方");
                    }
                } else if (recipeType.equals("SmithingTable")) {
                    String baseId = (String) map.get("Base");
                    String additionId = (String) map.get("Addition");
                    if (baseId != null && additionId != null) {
                        RecipeChoice.ExactChoice base = ItemRecipe.getChoice(baseId);
                        RecipeChoice.ExactChoice addition = ItemRecipe.getChoice(additionId);
                        ItemRecipe.registerSmithingTableRecipe(id, amount, base, addition);
                        ItemX.info(ChatColor.AQUA + "已注册 " + ChatColor.GREEN + id + ChatColor.AQUA + " 的锻造台配方");
                        ItemX.info(ChatColor.RED + "慎用锻造台配方, NBT保留会有些小问题");
                    }
                } else if (recipeType.equals("StoneCutter")) {
                    String ingredientId = (String) map.get("Ingredient");
                    if (ingredientId != null) {
                        RecipeChoice.ExactChoice ingredient = ItemRecipe.getChoice(ingredientId);
                        ItemRecipe.registerStonecutterRecipe(id, amount, ingredient);
                        ItemX.info(ChatColor.AQUA + "已注册 " + ChatColor.GREEN + id + ChatColor.AQUA + " 的切石机配方");
                    }
                }
            }
        }
    }

    public static void clearCache() {
        itemMap = new HashMap<>();
        dynamicItemMap = new HashMap<>();
    }

    private static void registerDynamicItem(String id, YamlConfiguration config) {
        dynamicItemMap.put(id, config);
    }

    private static ItemStack getDynamicItem(String id) {
        YamlConfiguration config = dynamicItemMap.get(id);
        HashMap tempMap = new HashMap();
        tempMap.put("Material", config.getString(id + ".Material").toUpperCase());
        tempMap.put("Model", config.getInt(id + ".Model"));
        tempMap.put("Name", StringUtil.toFormatted(config.getString(id + ".Name")));
        List<String> tempLore = config.getStringList(id + ".Lore");
        List<String> lore = new ArrayList<>();
        for (String loreLine : tempLore) {
            lore.add(StringUtil.toFormatted(StringUtil.replaceRandomInt(loreLine)));
        }
        tempMap.put("Lore", lore);
        tempMap.put("Unbreakable", config.getBoolean(id + ".Unbreakable"));
        tempMap.put("Enchantments", config.getStringList(id + ".Enchantments"));
        tempMap.put("ItemFlags", config.getStringList(id + ".ItemFlags"));
        tempMap.put("PotionColor", config.getString(id + ".PotionColor"));
        tempMap.put("NBT", config.getStringList(id + ".NBT"));
        tempMap.put("ID", id);
        return ItemFactory.buildItem(tempMap, true);
    }
}

