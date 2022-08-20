package org.btc.itemx.mechanics;

import org.btc.itemx.ItemX;
import org.btc.itemx.events.CropGrowEvent;
import org.btc.itemx.events.CropHarvestEvent;
import org.btc.itemx.events.CropPlantEvent;
import org.btc.itemx.item.ItemManager;
import org.btc.server.utils.EntityUtil;
import org.btc.server.utils.NumberUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Crop implements Listener {
    private static HashMap<String, HashMap> cropMap = new HashMap<>();
    private static HashMap<String, HashMap> seedMap = new HashMap<>();
    private static List<Player> plantedList = new ArrayList<>();

    public static void startCropListener() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(ItemX.getInstance(), () -> {
            List<World> worldList = Bukkit.getWorlds();
            for (World world : worldList) {
                Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                    List<ItemFrame> itemFrames = (List<ItemFrame>) world.getEntitiesByClass(ItemFrame.class);
                    Bukkit.getScheduler().runTaskAsynchronously(ItemX.getInstance(), () -> {
                        for (ItemFrame itemFrame : itemFrames) {
                            if (!itemFrame.isVisible()) {
                                check(itemFrame);
                            }
                        }
                    });
                });
            }
        }, 0, 1);
    }

    public static void registerCropMechanic(String id, List<String> dropTable, Integer matureTicks, String nextStage, Sound growSound, Sound harvestSound) {
        HashMap tempMap = new HashMap<>();
        tempMap.put("DropTable", dropTable);
        tempMap.put("MatureTicks", matureTicks);
        tempMap.put("NextStage", nextStage);
        tempMap.put("GrowSound", growSound);
        tempMap.put("HarvestSound", harvestSound);
        tempMap.put("Mature", false);
        cropMap.put(id, tempMap);
    }

    public static void registerCropMechanic(String id, List<String> dropTable, Sound harvestSound) {
        HashMap tempMap = new HashMap<>();
        tempMap.put("DropTable", dropTable);
        tempMap.put("HarvestSound", harvestSound);
        tempMap.put("Mature", true);
        cropMap.put(id, tempMap);
    }

    public static void registerSeedMechanic(String id, List<String> probableCrops, List<Object> canPlantOn, Sound plantSound) {
        HashMap tempMap = new HashMap<>();
        tempMap.put("ProbableCrops", probableCrops);
        tempMap.put("CanPlantOn", canPlantOn);
        tempMap.put("PlantSound", plantSound);
        seedMap.put(id, tempMap);
    }

    private static void check(ItemFrame itemFrame) {
        String cropId = ItemManager.getIdOrNull(itemFrame.getItem());
        if (!isMature(cropId) && NumberUtil.chanceOf(1 / Double.parseDouble(getMatureTicks(cropId).toString()))) {
            ItemStack nextStage = ItemManager.getItem(getNextStage(cropId));
            CropGrowEvent cropGrowEvent = new CropGrowEvent(itemFrame, itemFrame.getItem(), nextStage);
            Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                Bukkit.getPluginManager().callEvent(cropGrowEvent);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void CropInteractEvent(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (entity instanceof ItemFrame itemFrame && !itemFrame.isVisible()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void CropPlantEvent(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.useItemInHand().equals(Event.Result.ALLOW)) {
            Collection<Entity> entities = EntityUtil.getEntitiesNearBlock(e.getClickedBlock(), 0.01);
            for (Entity entity : entities) {
                if (entity instanceof ItemFrame itemFrame && !itemFrame.isVisible() && itemFrame.getFacing().equals(BlockFace.UP)) {
                    e.setCancelled(true);
                    return;
                }
            }
            Player player = e.getPlayer();
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            String mainHandId = ItemManager.getIdOrNull(mainHand);
            String offHandId = ItemManager.getIdOrNull(offHand);
            Block plantOn = e.getClickedBlock();
            if (mainHandId != null && seedMap.containsKey(mainHandId) && !plantedList.contains(player)) {
                CropPlantEvent cropPlantEvent = new CropPlantEvent(player, mainHand, mainHandId, plantOn);
                Bukkit.getPluginManager().callEvent(cropPlantEvent);
                player.swingMainHand();
                plantedList.add(player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(ItemX.getInstance(), () -> {
                    plantedList.remove(player);
                }, 4);
            } else if (offHandId != null && seedMap.containsKey(offHandId) && !plantedList.contains(player)) {
                CropPlantEvent cropPlantEvent = new CropPlantEvent(player, offHand, offHandId, plantOn);
                Bukkit.getPluginManager().callEvent(cropPlantEvent);
                player.swingOffHand();
                plantedList.add(player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(ItemX.getInstance(), () -> {
                    plantedList.remove(player);
                }, 4);
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void CropHarvestEvent(EntityDamageByEntityEvent e) {
        Entity victim = e.getEntity();
        if (victim instanceof ItemFrame itemFrame && !itemFrame.isVisible() && itemFrame.getFacing().equals(BlockFace.UP)) {
            CropHarvestEvent cropHarvestEvent = new CropHarvestEvent(e.getDamager(), itemFrame);
            Bukkit.getPluginManager().callEvent(cropHarvestEvent);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void CropDamageEvent(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.PHYSICAL) && e.getClickedBlock().getType().equals(Material.FARMLAND)) {
            if (e.useInteractedBlock() != Event.Result.DENY) {
                Collection<Entity> entities = EntityUtil.getEntitiesNearBlock(e.getClickedBlock(), 0.01);
                for (Entity entity : entities) {
                    if (entity instanceof ItemFrame itemFrame && !itemFrame.isVisible() && itemFrame.getFacing().equals(BlockFace.UP)) {
                        CropHarvestEvent cropHarvestEvent = new CropHarvestEvent(e.getPlayer(), itemFrame);
                        Bukkit.getPluginManager().callEvent(cropHarvestEvent);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void CropDamageEvent(EntityInteractEvent e) {
        if (!(e.getEntity() instanceof Player) && e.getBlock().getType().equals(Material.FARMLAND)) {
            Collection<Entity> entities = EntityUtil.getEntitiesNearBlock(e.getBlock(), 0.01);
            for (Entity entity : entities) {
                if (entity instanceof ItemFrame itemFrame && !itemFrame.isVisible() && itemFrame.getFacing().equals(BlockFace.UP)) {
                    CropHarvestEvent cropHarvestEvent = new CropHarvestEvent(e.getEntity(), itemFrame);
                    Bukkit.getPluginManager().callEvent(cropHarvestEvent);
                }
            }
        }
    }


    public static List<String> getDropTable(String id) {
        return (List<String>) cropMap.get(id).get("DropTable");
    }

    public static Boolean isMature(String id) {
        return (Boolean) (cropMap.get(id).get("Mature"));
    }

    public static Integer getMatureTicks(String id) {
        return Integer.valueOf(cropMap.get(id).get("MatureTicks").toString());
    }

    public static String getNextStage(String id) {
        return cropMap.get(id).get("NextStage").toString();
    }

    public static Sound getGrowSound(String id) {
        return (Sound) cropMap.get(id).get("GrowSound");
    }

    public static Sound getHarvestSound(String id) {
        return (Sound) cropMap.get(id).get("HarvestSound");
    }

    public static List<String> getProbableCrops(String id) {
        return (List<String>) seedMap.get(id).get("ProbableCrops");
    }

    public static Sound getPlantSound(String id) {
        return (Sound) seedMap.get(id).get("PlantSound");
    }

    public static Boolean canPlantOn(String id, Block block) {
        for (Object identifier : (List<Object>) seedMap.get(id).get("CanPlantOn")) {
            if (identifier instanceof BlockData) {
                if (block.getBlockData().equals(identifier)) {
                    return true;
                }
            } else if (identifier instanceof Material) {
                if (block.getType().equals(identifier)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void clearAllMechanics() {
        cropMap = new HashMap<>();
        seedMap = new HashMap<>();
    }
}
