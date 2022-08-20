package org.btc.itemx.handlers;

import org.btc.itemx.ItemX;
import org.btc.itemx.events.CustomBlockPlaceEvent;
import org.btc.itemx.mechanics.NoteBlock;
import org.btc.server.utils.EntityUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CustomBlockPlaceHandler implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void CustomBlockPlaceEvent(CustomBlockPlaceEvent e) {
        if (!e.isCancelled()) {
            Player player = e.getPlayer();
            EquipmentSlot equipmentSlot = e.getEquipmentSlot();
            Block placedAgainst = e.getPlacedAgainst();
            Location loc = e.getLocation();
            Block b = loc.getBlock();
            BlockData data = e.getBlockData();
            BlockPlaceEvent blockPlaceEvent = null;
            for (Entity entity : EntityUtil.getEntitiesNearBlock(b, 1d)) {
                if (EntityUtil.collidesWith(entity, b)) {
                    return;
                }
            }
            if (equipmentSlot.equals(EquipmentSlot.HAND)) {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                blockPlaceEvent = new BlockPlaceEvent(b, b.getState(), placedAgainst, mainHand, player, true, equipmentSlot);
                player.swingMainHand();
            } else if (equipmentSlot.equals(EquipmentSlot.OFF_HAND)) {
                ItemStack offHand = player.getInventory().getItemInOffHand();
                blockPlaceEvent = new BlockPlaceEvent(b, b.getState(), placedAgainst, offHand, player, true, equipmentSlot);
                player.swingOffHand();
            }
            if (blockPlaceEvent == null) {
                return;
            }
            Bukkit.getPluginManager().callEvent(blockPlaceEvent);
            if (blockPlaceEvent.isCancelled()) {
                return;
            }
            b.setBlockData(data);
            saveToData(b.getWorld(), b.getX(), b.getY(), b.getZ(), NoteBlock.getIdByBlockData(data));
        }
    }


    private static void saveToData(World world, Integer x, Integer y, Integer z, String id) {
        File savesFile = new File(ItemX.getInstance().getDataFolder() + "\\Saves.yml");
        YamlConfiguration saves = YamlConfiguration.loadConfiguration(savesFile);
        List<String> blockList = saves.getStringList("BlockList");
        String compound = world.getName() + "-" + x + "-" + y + "-" + z + "-" + id;
        if (!blockList.contains(compound)) {
            blockList.add(compound);
            saves.set("BlockList", blockList);
            try {
                saves.save(savesFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
