package org.btc.itemx.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;


public class ItemRecipe {
    public static void registerShapedRecipe(String id, Integer amount, RecipeChoice.ExactChoice[] choices) {
        ItemStack result = ItemManager.getItem(id);
        result.setAmount(amount);
        ShapedRecipe recipe;
        String nameSpace = id.toLowerCase() + "-";
        if (choices.length == 3) {
            for (Integer i = 0; i < 3; i++) {
                nameSpace = nameSpace + "i";
                if (nameSpace != null) {
                    recipe = new ShapedRecipe(NamespacedKey.fromString(nameSpace), result);
                    recipe.shape("012", "345", "678");
                    for (Integer ii = 0; ii < 3; ii++) {
                        recipe.setIngredient((char) ('0' + (3 * i + ii)), choices[ii]);
                    }
                    Bukkit.addRecipe(recipe);
                }
            }
        } else if (choices.length == 6) {
            for (Integer i = 0; i < 2; i++) {
                nameSpace = nameSpace + "i";
                recipe = new ShapedRecipe(NamespacedKey.fromString(nameSpace), result);
                recipe.shape("012", "345", "678");
                for (Integer ii = 0; ii < 6; ii++) {
                    recipe.setIngredient((char) ('0' + (3 * i + ii)), choices[ii]);
                }
                Bukkit.addRecipe(recipe);
            }
        } else if (choices.length == 9) {
            RecipeChoice.ExactChoice air = new RecipeChoice.ExactChoice(new ItemStack(Material.AIR));
            if (choices[2].equals(air) && choices[5].equals(air) && choices[8].equals(air)) {
                if (choices[1].equals(air) && choices[4].equals(air) && choices[7].equals(air)) {
                    for (Integer i = 0; i < 3; i++) {
                        nameSpace = nameSpace + "i";
                        recipe = new ShapedRecipe(NamespacedKey.fromString(nameSpace), result);
                        recipe.shape("012", "345", "678");
                        for (Integer ii = 0; ii < 3; ii++) {
                            recipe.setIngredient((char) ('0' + (3 * ii + i)), choices[3 * ii + i]);
                        }
                        Bukkit.addRecipe(recipe);
                    }
                } else {
                    for (Integer i = 0; i < 2; i++) {
                        nameSpace = nameSpace + "i";
                        recipe = new ShapedRecipe(NamespacedKey.fromString(nameSpace), result);
                        recipe.shape("012", "345", "678");
                        for (Integer ii = 0; ii < 3; ii++) {
                            recipe.setIngredient((char) ('0' + (3 * ii + i)), choices[3 * ii + i]);
                            recipe.setIngredient((char) ('0' + (3 * ii + i + 1)), choices[3 * ii + i + 1]);
                        }
                        Bukkit.addRecipe(recipe);
                    }
                }
            } else {
                nameSpace = nameSpace + "i";
                recipe = new ShapedRecipe((NamespacedKey.fromString(nameSpace)), result);
                recipe.shape("012", "345", "678");
                for (Integer i = 0; i < 9; i++) {
                    recipe.setIngredient((char) ('0' + i), choices[i]);
                }
                Bukkit.addRecipe(recipe);
            }
        }
    }


    public static void registerShapelessRecipe(String id, Integer amount, RecipeChoice.ExactChoice[] choices) {
        NamespacedKey nameSpace = NamespacedKey.fromString(id.toLowerCase() + "-shapeless");
        ItemStack result = ItemManager.getItem(id);
        result.setAmount(amount);
        ShapelessRecipe recipe = new ShapelessRecipe(nameSpace, result);
        for (RecipeChoice.ExactChoice choice : choices) {
            recipe.addIngredient(choice);
        }
        Bukkit.addRecipe(recipe);
    }

    public static void registerFurnaceRecipe(String id, Integer amount, RecipeChoice.ExactChoice choice, Float exp, Integer cookingTime) {
        NamespacedKey nameSpace = NamespacedKey.fromString(id.toLowerCase() + "-furnace");
        ItemStack result = ItemManager.getItem(id);
        result.setAmount(amount);
        FurnaceRecipe recipe = new FurnaceRecipe(nameSpace, result, choice, exp, cookingTime);
        Bukkit.addRecipe(recipe);
    }

    public static void registerBlastFurnaceRecipe(String id, Integer amount, RecipeChoice.ExactChoice choice, Float exp, Integer cookingTime) {
        NamespacedKey nameSpace = NamespacedKey.fromString(id.toLowerCase() + "-blast_furnace");
        ItemStack result = ItemManager.getItem(id);
        result.setAmount(amount);
        BlastingRecipe recipe = new BlastingRecipe(nameSpace, result, choice, exp, cookingTime);
        Bukkit.addRecipe(recipe);
    }

    public static void registerCampfireRecipe(String id, Integer amount, RecipeChoice.ExactChoice choice, Float exp, Integer cookingTime) {
        NamespacedKey nameSpace = NamespacedKey.fromString(id.toLowerCase() + "-campfire");
        ItemStack result = ItemManager.getItem(id);
        result.setAmount(amount);
        CampfireRecipe recipe = new CampfireRecipe(nameSpace, result, choice, exp, cookingTime);
        Bukkit.addRecipe(recipe);
    }

    public static void registerSmokerRecipe(String id, Integer amount, RecipeChoice.ExactChoice choice, Float exp, Integer cookingTime) {
        NamespacedKey nameSpace = NamespacedKey.fromString(id.toLowerCase() + "-smoker");
        ItemStack result = ItemManager.getItem(id);
        result.setAmount(amount);
        SmokingRecipe recipe = new SmokingRecipe(nameSpace, result, choice, exp, cookingTime);
        Bukkit.addRecipe(recipe);
    }

    public static void registerSmithingTableRecipe(String id, Integer amount, RecipeChoice.ExactChoice base, RecipeChoice.ExactChoice addition) {
        NamespacedKey nameSpace = NamespacedKey.fromString(id.toLowerCase() + "-smithing_table");
        ItemStack result = ItemManager.getItem(id);
        result.setAmount(amount);
        SmithingRecipe recipe = new SmithingRecipe(nameSpace, result, base, addition);
        Bukkit.addRecipe(recipe);
    }

    public static void registerStonecutterRecipe(String id, Integer amount, RecipeChoice.ExactChoice input) {
        NamespacedKey nameSpace = NamespacedKey.fromString(id.toLowerCase() + "-stonecutter");
        ItemStack result = ItemManager.getItem(id);
        result.setAmount(amount);
        StonecuttingRecipe recipe = new StonecuttingRecipe(nameSpace, result, input);
    }

    public static RecipeChoice.ExactChoice getChoice(String id) {
        if (id.equals("WOOL")) {
            return new RecipeChoice.ExactChoice(
                    new ItemStack(Material.BLACK_WOOL),
                    new ItemStack(Material.WHITE_WOOL),
                    new ItemStack(Material.GRAY_WOOL),
                    new ItemStack(Material.LIGHT_GRAY_WOOL),
                    new ItemStack(Material.RED_WOOL),
                    new ItemStack(Material.ORANGE_WOOL),
                    new ItemStack(Material.YELLOW_WOOL),
                    new ItemStack(Material.GREEN_WOOL),
                    new ItemStack(Material.BLUE_WOOL),
                    new ItemStack(Material.LIGHT_BLUE_WOOL),
                    new ItemStack(Material.PURPLE_WOOL),
                    new ItemStack(Material.CYAN_WOOL),
                    new ItemStack(Material.BROWN_WOOL),
                    new ItemStack(Material.LIME_WOOL),
                    new ItemStack(Material.MAGENTA_WOOL),
                    new ItemStack(Material.PINK_WOOL)
            );
        } else if (id.equals("LOG")) {
            return new RecipeChoice.ExactChoice(
                    new ItemStack(Material.ACACIA_LOG),
                    new ItemStack(Material.STRIPPED_ACACIA_LOG),
                    new ItemStack(Material.BIRCH_LOG),
                    new ItemStack(Material.STRIPPED_BIRCH_LOG),
                    new ItemStack(Material.DARK_OAK_LOG),
                    new ItemStack(Material.STRIPPED_DARK_OAK_LOG),
                    new ItemStack(Material.JUNGLE_LOG),
                    new ItemStack(Material.STRIPPED_JUNGLE_LOG),
                    new ItemStack(Material.OAK_LOG),
                    new ItemStack(Material.STRIPPED_OAK_LOG),
                    new ItemStack(Material.SPRUCE_LOG),
                    new ItemStack(Material.STRIPPED_SPRUCE_LOG),
                    new ItemStack(Material.CRIMSON_STEM),
                    new ItemStack(Material.STRIPPED_CRIMSON_STEM),
                    new ItemStack(Material.WARPED_STEM),
                    new ItemStack(Material.STRIPPED_WARPED_STEM)
            );
        } else if (id.equals("WOOD")) {
            return new RecipeChoice.ExactChoice(
                    new ItemStack(Material.ACACIA_WOOD),
                    new ItemStack(Material.STRIPPED_ACACIA_WOOD),
                    new ItemStack(Material.BIRCH_WOOD),
                    new ItemStack(Material.STRIPPED_BIRCH_WOOD),
                    new ItemStack(Material.DARK_OAK_WOOD),
                    new ItemStack(Material.STRIPPED_DARK_OAK_WOOD),
                    new ItemStack(Material.JUNGLE_WOOD),
                    new ItemStack(Material.STRIPPED_JUNGLE_WOOD),
                    new ItemStack(Material.OAK_WOOD),
                    new ItemStack(Material.STRIPPED_OAK_WOOD),
                    new ItemStack(Material.SPRUCE_WOOD),
                    new ItemStack(Material.STRIPPED_SPRUCE_WOOD),
                    new ItemStack(Material.CRIMSON_STEM),
                    new ItemStack(Material.STRIPPED_CRIMSON_STEM),
                    new ItemStack(Material.WARPED_STEM),
                    new ItemStack(Material.STRIPPED_WARPED_STEM)
            );
        } else if (id.equals("WOOD_PLANK")) {
            return new RecipeChoice.ExactChoice(
                    new ItemStack(Material.ACACIA_PLANKS),
                    new ItemStack(Material.BIRCH_PLANKS),
                    new ItemStack(Material.DARK_OAK_PLANKS),
                    new ItemStack(Material.JUNGLE_PLANKS),
                    new ItemStack(Material.OAK_PLANKS),
                    new ItemStack(Material.SPRUCE_PLANKS),
                    new ItemStack(Material.CRIMSON_PLANKS),
                    new ItemStack(Material.WARPED_PLANKS)
            );
        } else {
            return new RecipeChoice.ExactChoice(ItemManager.getItem(id));
        }
    }
}
