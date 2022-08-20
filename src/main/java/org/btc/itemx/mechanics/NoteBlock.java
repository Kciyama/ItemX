package org.btc.itemx.mechanics;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.btc.itemx.ItemX;
import org.btc.itemx.enums.ToolType;
import org.btc.itemx.events.CustomBlockPlaceEvent;
import org.btc.itemx.item.ItemManager;
import org.btc.server.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoteBlock implements Listener {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    public static HashMap<String, BlockData> blockDataMap = new HashMap<>();
    public static HashMap<BlockData, String> blockMap = new HashMap<>();
    public static HashMap<String, HashMap> dropsMap = new HashMap<>();
    public static HashMap<String, HashMap> mechanicMap = new HashMap<>();
    private static HashMap<Player, Float> diggingMap = new HashMap<>();
    private static HashMap<Player, BlockData> diggingBlockDataMap = new HashMap<>();
    private static List<Player> vanillaDiggingList = new ArrayList<>();
    private static List<Player> vanillaDiggingSoundList = new ArrayList<>();
    //数据结构: HashMap<玩家, 剩余时间:层数:是否信标:是否有粒子:是否有图标>
    private static HashMap<Player, String> cachedMiningFatigueEffects = new HashMap<>();
    private static List<Player> diggingSoundList = new ArrayList<>();
    private static List<Player> placedList = new ArrayList<>();

    private static final PacketAdapter digListener = new PacketAdapter(ItemX.getInstance(), ListenerPriority.LOWEST, PacketType.Play.Client.BLOCK_DIG) {
        @Override
        public void onPacketReceiving(PacketEvent e) {
            Player player = e.getPlayer();
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                PacketContainer packet = e.getPacket();
                EnumWrappers.PlayerDigType type = packet.getPlayerDigTypes().read(0);
                BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
                Block block = new Location(player.getWorld(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ()).getBlock();
                BlockData blockData = block.getBlockData().clone();
                if (type.equals(EnumWrappers.PlayerDigType.START_DESTROY_BLOCK)) {
                    if (block.getType().isSolid()) {
                        if (blockDataMap.containsValue(blockData)) {
                            diggingBlockDataMap.put(player, blockData);
                            if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                                PotionEffect potionEffect = player.getPotionEffect(PotionEffectType.SLOW_DIGGING);
                                String compound = "";
                                Integer potionEffectEndTime = potionEffect.getDuration() + ItemX.getRunningTicks();
                                compound += potionEffectEndTime + ":";
                                compound += potionEffect.getAmplifier() + ":";
                                compound += potionEffect.isAmbient() + ":";
                                compound += potionEffect.hasParticles() + ":";
                                compound += potionEffect.hasIcon();
                                cachedMiningFatigueEffects.put(player, compound);
                            }
                            diggingMap.put(player, 0f);
                        } else {
                            vanillaDiggingList.add(player);
                            diggingBlockDataMap.remove(player);
                        }
                    }
                } else {
                    if (diggingBlockDataMap.containsKey(player) && cachedMiningFatigueEffects.containsKey(player)) {
                        String[] temp = CompoundUtil.get(cachedMiningFatigueEffects.get(player), ":");
                        if (temp.length == 5) {
                            if (Integer.valueOf(temp[1]) < 256) {
                                PotionEffect potionEffect = new PotionEffect(PotionEffectType.SLOW_DIGGING, (Integer.valueOf(temp[0]) - ItemX.getRunningTicks()), Integer.valueOf(temp[1]), Boolean.getBoolean(temp[2]), Boolean.getBoolean(temp[3]), Boolean.getBoolean(temp[4]));
                                Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                                    player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                                    player.addPotionEffect(potionEffect);
                                });
                            }
                        }
                    } else {
                        Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                            player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                        });
                    }
                    cachedMiningFatigueEffects.remove(player);
                    diggingBlockDataMap.remove(player);
                    diggingMap.remove(player);
                    vanillaDiggingList.remove(player);
                    try {
                        sendFakeBlockBreakState(player, block, 0L);
                    } catch (InvocationTargetException ignored) {
                    }
                }

            }
        }
    };


    public static void startPacketListener() {
        protocolManager.addPacketListener(digListener);
        Bukkit.getScheduler().runTaskTimer(ItemX.getInstance(), () -> {
            for (Player player : diggingMap.keySet()) {
                if (!diggingSoundList.contains(player)) {
                    triggerDigging(player, player.getTargetBlockExact(16), true);
                    diggingSoundList.add(player);
                    Bukkit.getScheduler().runTaskLater(ItemX.getInstance(), () -> {
                        diggingSoundList.remove(player);
                    }, 4);
                } else {
                    triggerDigging(player, player.getTargetBlockExact(16), false);
                }
            }
            for (Player player : vanillaDiggingList) {
                Block block = player.getTargetBlockExact(32);
                if (!vanillaDiggingSoundList.contains(player)) {
                    vanillaDiggingSoundList.add(player);
                    SoundGroup soundGroup = block.getBlockData().getSoundGroup();
                    Bukkit.getScheduler().runTaskLater(ItemX.getInstance(), () -> {
                        vanillaDiggingSoundList.remove(player);
                    }, 4);
                }
            }
        }, 0, 1);
    }

    public static void startBlockChecker() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(ItemX.getInstance(), () -> {
            Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                File savesFile = new File(ItemX.getInstance().getDataFolder() + "\\Saves.yml");
                YamlConfiguration saves = YamlConfiguration.loadConfiguration(savesFile);
                List<String> blockList = saves.getStringList("BlockList");
                for (String compound : blockList) {
                    String[] temp = CompoundUtil.get(compound);
                    if (temp.length == 5) {
                        World world = Bukkit.getWorld(temp[0]);
                        Integer x = Integer.valueOf(temp[1]);
                        Integer y = Integer.valueOf(temp[2]);
                        Integer z = Integer.valueOf(temp[3]);
                        String id = temp[4];
                        Block block = new Location(world, x, y, z).getBlock();
                        if (block.getType().equals(Material.NOTE_BLOCK)) {
                            BlockData standard = blockDataMap.get(id);
                            if (block.getBlockData() != standard) {
                                try {
                                    block.setBlockData(standard);
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                        }
                    }
                }
            });
        }, 0, 2);
    }

    private static void triggerDigging(Player player, Block block, Boolean withDiggingSound) {
        BlockData blockData = block.getBlockData();
        if (blockDataMap.containsValue(blockData)) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            Float diggingSpeed = BreakSpeed.getBreakSpeedFromTool(tool);
            String blockId = getIdByBlockData(blockData);
            ToolType bestTool = getBestTool(blockId);
            if (!ItemManager.isTool(tool, bestTool)) {
                diggingSpeed = diggingSpeed * 0.3f;
            } else if (tool.containsEnchantment(Enchantment.DIG_SPEED)) {
                Integer efficiencyLevel = tool.getEnchantmentLevel(Enchantment.DIG_SPEED);
                diggingSpeed = diggingSpeed * (1 + efficiencyLevel * efficiencyLevel);
            }
            if (!((Entity) player).isOnGround()) {
                diggingSpeed = diggingSpeed * 0.2f;
            }
            if (player.getEyeLocation().getBlock().getType().equals(Material.WATER)) {
                if (!player.getInventory().getHelmet().containsEnchantment(Enchantment.WATER_WORKER)) {
                    diggingSpeed = diggingSpeed * 0.2f;
                }
            }
            if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
                Integer hasteLevel = player.getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier() + 1;
                diggingSpeed = diggingSpeed * (1 + hasteLevel * 0.2f);
            }
            if (cachedMiningFatigueEffects.containsKey(player)) {
                String[] temp = CompoundUtil.get(cachedMiningFatigueEffects.get(player), ":");
                Integer miningFatigueLevel = Integer.valueOf(temp[1]) + 1;
                for (Integer i = 0; i < miningFatigueLevel; i++) {
                    diggingSpeed = diggingSpeed * 0.3f;
                }
            }
            if (cachedMiningFatigueEffects.containsKey(player)) {
                String[] temp = CompoundUtil.get(cachedMiningFatigueEffects.get(player), ":");
                if (temp.length == 5) {
                    Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, Integer.MAX_VALUE, Boolean.getBoolean(temp[2]), Boolean.getBoolean(temp[3]), Boolean.getBoolean(temp[4])));
                    });
                }
            } else {
                Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false, false));
                });
            }
            final Float finalDiggingSpeed = diggingSpeed;
            final Float hardness = getHardness(blockId);
            Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                try {
                    Float diggingProgress = diggingMap.get(player);
                    diggingProgress = diggingProgress + finalDiggingSpeed;
                    if (diggingProgress > hardness * 30) {
                        String toolId = ItemManager.getIdOrNull(tool);
                        Boolean breakable = isBreakable(block, tool);
                        //发送动画
                        final BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
                        Bukkit.getPluginManager().callEvent(blockBreakEvent);
                        //若已成功挖掘
                        if (!blockBreakEvent.isCancelled()) {
                            if (breakable) {
                                removeFromData(block.getWorld(), block.getX(), block.getY(), block.getZ(), blockMap.get(block.getBlockData()));
                                for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 32, 32, 32)) {
                                    if (entity instanceof Player p) {
                                        sendFakeBlockBreakState(p, block, 0L);
                                    }
                                }
                                block.getWorld().playSound(block.getLocation(), getBreakSound(blockId), 0.8f, 0.8f);
                                block.getWorld().spawn(BlockUtil.getBlockLocation(block), ExperienceOrb.class, experienceOrb -> {
                                    experienceOrb.setExperience(getExp(blockId));
                                });
                                diggingMap.put(player, 0f);
                                if (!tool.getItemMeta().isUnbreakable()) {
                                    Damageable damageable = (Damageable) tool.getItemMeta();
                                    damageable.setDamage(damageable.getDamage() + 1);
                                    tool.setItemMeta(damageable);
                                    if (getSilkTouch(blockId) && tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
                                        block.getWorld().dropItemNaturally(block.getLocation(), ItemManager.getItem(blockId));
                                    } else {
                                        List<ItemStack> drops = new ArrayList<>();
                                        List<String> dropTable = getDropTable(blockId);
                                        for (String compound : dropTable) {
                                            compound = StringUtil.replaceRandomInt(compound);
                                            String[] temp = CompoundUtil.get(compound);
                                            ItemStack drop = ItemManager.getItem(temp[0]);
                                            Integer amount = Integer.valueOf(temp[1]);
                                            drop.setAmount(amount);
                                            if (temp.length == 2 || (temp.length > 2 && NumberUtil.chanceOf(Double.valueOf(temp[2])))) {
                                                drops.add(drop);
                                            }
                                        }
                                        if (getFortune(blockId)) {
                                            if (NumberUtil.chanceOf(2d / (tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 2d))) {
                                                for (ItemStack drop : drops) {
                                                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                                                }
                                            } else {
                                                Integer lootBonusTime = NumberUtil.randomInt(2, tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
                                                for (Integer i = 0; i < lootBonusTime; i++) {
                                                    for (ItemStack drop : drops) {
                                                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                block.setType(Material.AIR);
                            }
                        }
                    } else {
                        diggingMap.put(player, diggingProgress);
                        //发送动画
                        for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 32, 32, 32)) {
                            if (entity instanceof Player p) {
                                sendFakeBlockBreakState(p, block, Math.round(Math.floor(diggingProgress / (hardness * 3))));
                            }
                        }
                        if (withDiggingSound) {
                            playBreakingSound(player, block, getDiggingSound(blockId));
                        }
                    }
                } catch (NullPointerException ignored) {
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        } else {

        }

    }

    public static void registerNoteBlockData(String id, BlockData data) {
        blockDataMap.put(id, data);
        blockMap.put(data, id);
    }

    public static void registerNoteBlockMechanic(String id, Float hardness, Sound breakSound, Sound placeSound, Sound diggingSound, ToolType bestTool, List<String> breakableBy) {
        HashMap tempMap = new HashMap<>();
        tempMap.put("Hardness", hardness);
        tempMap.put("BreakSound", breakSound);
        tempMap.put("PlaceSound", placeSound);
        tempMap.put("DiggingSound", diggingSound);
        tempMap.put("BestTool", bestTool);
        tempMap.put("BreakableBy", breakableBy);
        mechanicMap.put(id, tempMap);
    }

    public static void registerNoteBlockDrops(String id, MemorySection dropMap) {
        Boolean silkTouch = (Boolean) dropMap.get("SilkTouch");
        Boolean fortune = (Boolean) dropMap.get("Fortune");
        List<String> dropTable = (List<String>) dropMap.get("DropTable");
        String exp = dropMap.getString("Exp");
        HashMap tempMap = new HashMap<>();
        tempMap.put("SilkTouch", silkTouch);
        tempMap.put("Fortune", fortune);
        tempMap.put("DropTable", dropTable);
        tempMap.put("Exp", exp);
        dropsMap.put(id, tempMap);
    }

    public static String getIdByBlockData(BlockData data) {
        return blockMap.get(data);
    }

    public static BlockData getBlockDataById(String id) {
        return blockDataMap.get(id);
    }

    public static Float getHardness(String id) {
        return Float.valueOf(mechanicMap.get(id).get("Hardness").toString());
    }

    public static Sound getBreakSound(String id) {
        return (Sound) (mechanicMap.get(id).get("BreakSound"));
    }

    public static Sound getPlaceSound(String id) {
        return (Sound) (mechanicMap.get(id).get("PlaceSound"));
    }

    public static Sound getDiggingSound(String id) {
        return (Sound) (mechanicMap.get(id).get("DiggingSound"));
    }

    public static ToolType getBestTool(String id) {
        return (ToolType) (mechanicMap.get(id).get("BestTool"));
    }

    public static List<String> getBreakableBy(Block block) {
        String id = getIdByBlockData(block.getBlockData());
        return id != null ? (List<String>) (mechanicMap.get(id).get("BreakableBy")) : null;
    }

    public static Boolean getSilkTouch(String id) {
        return (Boolean) dropsMap.get(id).get("SilkTouch");
    }

    public static Boolean getFortune(String id) {
        return (Boolean) dropsMap.get(id).get("Fortune");
    }

    public static List<String> getDropTable(String id) {
        return (List<String>) dropsMap.get(id).get("DropTable");
    }

    public static Integer getExp(String id) {
        String tempExp = dropsMap.get(id).get("Exp").toString();
        return Integer.valueOf(StringUtil.replaceRandomInt(tempExp));
    }

    public static Boolean isBreakable(Block block, ItemStack item) {
        String itemId = ItemManager.getIdOrNull(item);
        for (String id : getBreakableBy(block)) {
            ItemStack compare = ItemManager.getItem(id);
            Material compareType = compare.getType();
            if (compare.equals(new ItemStack(compareType))) {
                if (itemId == null && item.getType().equals(compareType)) {
                    return true;
                }
            } else {
                if (itemId != null && itemId.equals(ItemManager.getIdOrNull(compare))) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void CustomBlockPlaceEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        String mainHandId = ItemManager.getIdOrNull(mainHand);
        String offHandId = ItemManager.getIdOrNull(offHand);
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            BlockFace face = e.getBlockFace();
            Block block = e.getClickedBlock();
            if (!block.getType().equals(Material.NOTE_BLOCK) && block.getType().isInteractable()) {
                return;
            }
            Double x = block.getLocation().getX() + 0.5;
            Double y = block.getLocation().getY() + 0.5;
            Double z = block.getLocation().getZ() + 0.5;
            CustomBlockPlaceEvent customBlockPlaceEvent = null;
            if (mainHandId != null && blockDataMap.containsKey(mainHandId) && !placedList.contains(player)) {
                EquipmentSlot equipmentSlot = EquipmentSlot.HAND;
                if (block.getType().isSolid()) {
                    switch (face) {
                        case UP:
                            Location upLocation = new Location(block.getWorld(), x, y + 1, z);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, upLocation, blockDataMap.get(mainHandId));
                            break;
                        case DOWN:
                            Location downLocation = new Location(block.getWorld(), x, y - 1, z);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, downLocation, blockDataMap.get(mainHandId));
                            break;
                        case EAST:
                            Location eastLocation = new Location(block.getWorld(), x + 1, y, z);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, eastLocation, blockDataMap.get(mainHandId));
                            break;
                        case WEST:
                            Location westLocation = new Location(block.getWorld(), x - 1, y, z);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, westLocation, blockDataMap.get(mainHandId));
                            break;
                        case SOUTH:
                            Location southLocation = new Location(block.getWorld(), x, y, z + 1);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, southLocation, blockDataMap.get(mainHandId));
                            break;
                        case NORTH:
                            Location northLocation = new Location(block.getWorld(), x, y, z - 1);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, northLocation, blockDataMap.get(mainHandId));
                            break;
                    }
                } else {
                    customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, block.getLocation(), blockDataMap.get(mainHandId));
                }
                if (customBlockPlaceEvent != null) {
                    Bukkit.getPluginManager().callEvent(customBlockPlaceEvent);
                }
                placedList.add(player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(ItemX.getInstance(), () -> {
                    placedList.remove(player);
                }, 4);
            } else if (offHandId != null && blockDataMap.containsKey(offHandId) && !placedList.contains(player)) {
                EquipmentSlot equipmentSlot = EquipmentSlot.OFF_HAND;
                if (block.getType().isSolid()) {
                    switch (face) {
                        case UP:
                            Location upLocation = new Location(block.getWorld(), x, y + 1, z);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, upLocation, blockDataMap.get(offHandId));
                            break;
                        case DOWN:
                            Location downLocation = new Location(block.getWorld(), x, y - 1, z);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, downLocation, blockDataMap.get(offHandId));
                            break;
                        case EAST:
                            Location eastLocation = new Location(block.getWorld(), x + 1, y, z);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, eastLocation, blockDataMap.get(offHandId));
                            break;
                        case WEST:
                            Location westLocation = new Location(block.getWorld(), x - 1, y, z);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, westLocation, blockDataMap.get(offHandId));
                            break;
                        case SOUTH:
                            Location southLocation = new Location(block.getWorld(), x, y, z + 1);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, southLocation, blockDataMap.get(offHandId));
                            break;
                        case NORTH:
                            Location northLocation = new Location(block.getWorld(), x, y, z - 1);
                            customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, northLocation, blockDataMap.get(offHandId));
                            break;
                    }
                } else {
                    customBlockPlaceEvent = new CustomBlockPlaceEvent(player, equipmentSlot, block, block.getLocation(), blockDataMap.get(offHandId));
                }
                if (customBlockPlaceEvent != null) {
                    Bukkit.getPluginManager().callEvent(customBlockPlaceEvent);
                }
                placedList.add(player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(ItemX.getInstance(), () -> {
                    placedList.remove(player);
                }, 4);
            } else {
                Block newBlock = null;
                BlockPlaceEvent blockPlaceEvent = null;
                if (blockMap.containsKey(block.getBlockData())) {
                    if (!mainHand.getType().isAir() && mainHand.getType().isBlock()) {
                        if (!player.isSneaking()) {
                            player.setSneaking(true);
                            Bukkit.getScheduler().runTaskAsynchronously(ItemX.getInstance(), () -> {
                                player.setSneaking(false);
                            });
                        }
                    } else if (!offHand.getType().equals(Material.AIR) && offHand.getType().isBlock()) {
                        if (!player.isSneaking()) {
                            player.setSneaking(true);
                            Bukkit.getScheduler().runTaskLater(ItemX.getInstance(), () -> {
                                player.setSneaking(false);
                            }, 1);
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(ItemX.getInstance(), () -> {
            Block block = e.getBlock();
            if (!e.isCancelled()) {
                if (!block.getType().equals(Material.NOTE_BLOCK)) {
                    SoundGroup soundGroup = e.getBlock().getBlockData().getSoundGroup();
                } else {
                    ItemStack item = e.getItemInHand();
                    item.setAmount(item.getAmount() - 1);
                    e.getBlock().getWorld().playSound(e.getBlock().getLocation(), getPlaceSound(getIdByBlockData(block.getBlockData())), 1f, 0.8f);
                }
            } else {
                e.getPlayer().sendMessage("Cancelled");
            }
        });
    }

    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (!e.isCancelled() && !block.getType().equals(Material.NOTE_BLOCK)) {
            SoundGroup soundGroup = e.getBlock().getBlockData().getSoundGroup();
        }
    }

    @EventHandler
    public void NoteBlockHitEvent(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (e.getClickedBlock().getType().equals(Material.NOTE_BLOCK)) {
                if (!e.getPlayer().isSneaking()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent e) {
        diggingMap.remove(e.getPlayer());
    }

    private static void sendFakeBlockBreakState(Player player, Block block, Long state) throws
            InvocationTargetException {
        if (block.getType().equals(Material.NOTE_BLOCK)) {
            Location loc = block.getLocation();
            PacketContainer fakeBlockBreakAnimation = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
            fakeBlockBreakAnimation.getIntegers().write(0, loc.hashCode()).write(1, state.intValue());
            fakeBlockBreakAnimation.getBlockPositionModifier().write(0, new BlockPosition(loc.toVector()));
            protocolManager.sendServerPacket(player, fakeBlockBreakAnimation);
        }
    }

    private static void playBreakingSound(Player player, Block block, Sound replacingSound) throws
            InvocationTargetException {
        if (block.getType().equals(Material.NOTE_BLOCK)) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.STOP_SOUND);
            packet.getSoundCategories().write(0, EnumWrappers.SoundCategory.MASTER);
            protocolManager.sendServerPacket(player, packet);
            block.getWorld().playSound(block.getLocation(), replacingSound, 0.8f, 0.75f);
        }
    }


    private static void removeFromData(World world, Integer x, Integer y, Integer z, String id) {
        File savesFile = new File(ItemX.getInstance().getDataFolder() + "\\Saves.yml");
        YamlConfiguration saves = YamlConfiguration.loadConfiguration(savesFile);
        List<String> blockList = saves.getStringList("BlockList");
        blockList.remove(world.getName() + "-" + x + "-" + y + "-" + z + "-" + id);
        saves.set("BlockList", blockList);
        try {
            saves.save(savesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Boolean containsId(String id) {
        return blockDataMap.containsKey(id);
    }

    public static void clearAllMechanics() {
        blockDataMap = new HashMap<>();
        blockMap = new HashMap<>();
        dropsMap = new HashMap<>();
        mechanicMap = new HashMap<>();
        diggingMap = new HashMap<>();
        cachedMiningFatigueEffects = new HashMap<>();
    }
}
