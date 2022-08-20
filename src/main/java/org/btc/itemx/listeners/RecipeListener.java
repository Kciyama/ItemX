package org.btc.itemx.listeners;

import org.btc.itemx.ItemX;
import org.btc.itemx.item.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class RecipeListener implements Listener {
    @EventHandler
    public void CraftPrepareEvent(PrepareItemCraftEvent e) {
        Recipe recipe = e.getRecipe();
        if (recipe != null) {
            CraftingInventory inventory = e.getInventory();
            ItemStack[] matrix = inventory.getMatrix();
            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                List<ItemStack> originIngredients = shapelessRecipe.getIngredientList();
                List<ItemStack> tempGivenIngredients = Arrays.stream(matrix).toList();
                List<ItemStack> givenIngredients = new ArrayList<>();
                for (ItemStack itemStack : tempGivenIngredients) {
                    if (itemStack != null) {
                        givenIngredients.add(itemStack);
                    }
                }
                Bukkit.broadcastMessage(givenIngredients.toString());
                if (!givenIngredients.equals(originIngredients)) {
                    Bukkit.broadcastMessage("Not Equal, Event Cancelled!");
                    inventory.setResult(new ItemStack(Material.AIR));
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void CraftEvent(CraftItemEvent e) {
        if (e.getRecipe().getResult().getType().isAir()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent e) {
        if (e.getInventory() instanceof CraftingInventory) {
        }
    }
}
