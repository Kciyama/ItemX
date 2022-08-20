package org.btc.itemx.handlers;

import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTEntity;
import org.btc.itemx.ItemX;
import org.btc.itemx.events.CropGrowEvent;
import org.btc.itemx.events.CropHarvestEvent;
import org.btc.itemx.events.CropPlantEvent;
import org.btc.itemx.item.ItemManager;
import org.btc.itemx.mechanics.Crop;
import org.btc.server.utils.CompoundUtil;
import org.btc.server.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class CropHandler implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void CropPlantEvent(CropPlantEvent e) {
        if (!e.isCancelled()) {
            Player player = e.getPlayer();
            ItemStack seed = e.getSeed();
            String seedId = ItemManager.getIdOrNull(seed);
            Block plantOn = e.getPlantOn();
            Block up = new Location(player.getWorld(), plantOn.getX(), plantOn.getY() + 1, plantOn.getZ()).getBlock();
            if (ItemX.getInstance().getResidenceHook().isEnabled()) {
                if (!ResidenceBlockListener.canPlaceBlock(player, up, true)) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (Crop.canPlantOn(seedId, plantOn) && up.getType().isAir()) {
                spawnCrop(player.getWorld(), up.getLocation(), Crop.getProbableCrops(seedId));
                seed.setAmount(seed.getAmount() - 1);
                player.getWorld().playSound(up.getLocation(), Crop.getPlantSound(seedId), 1f, 0.8f);
            } else {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void CropGrowEvent(CropGrowEvent e) {
        if (!e.isCancelled()) {
            ItemStack oldStage = e.getOldStage();
            ItemStack newStage = e.getNewStage();
            ItemFrame holder = e.getHolder();
            holder.setItem(newStage, false);
            try {
                holder.getWorld().playSound(holder.getLocation(), Crop.getGrowSound(ItemManager.getIdOrNull(oldStage)), 1f, 0.8f);
            } catch (NullPointerException ignored) {
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void CropHarvestEvent(CropHarvestEvent e) {
        if (!e.isCancelled()) {
            Entity entity = e.getEntity();
            String cropId = ItemManager.getIdOrNull(e.getHolder().getItem());
            ItemFrame holder = e.getHolder();
            List<String> dropTable = Crop.getDropTable(cropId);
            if (dropTable == null) {
                entity.sendMessage("NullPointer");
            }
            for (String compound : dropTable) {
                String[] temp = CompoundUtil.get(compound, ":");
                ItemStack drop = ItemManager.getItem(temp[0]);
                drop.setAmount(Integer.valueOf(StringUtil.replaceRandomInt(temp[1])));
                entity.getWorld().dropItemNaturally(holder.getLocation(), drop);
            }
            holder.getWorld().playSound(holder.getLocation(), Crop.getHarvestSound(cropId), 1.0f, 0.8f);
            holder.remove();
        }
    }

    private static void spawnCrop(World world, Location location, List<String> probableCrops) {
        world.spawn(location, ItemFrame.class, itemFrame -> {
            itemFrame.setVisible(false);
            NBTEntity entity = new NBTEntity(itemFrame);
            entity.setInteger("Facing", 1);
            itemFrame.setItem(ItemManager.getItem(probableCrops.get(new Random().nextInt(probableCrops.size()))), false);

        });
    }
}
