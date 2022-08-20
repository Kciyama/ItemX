package org.btc.itemx.mechanics;

import org.btc.itemx.ItemX;
import org.btc.itemx.item.ItemManager;
import org.btc.server.utils.CompoundUtil;
import org.btc.server.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;

public class Edible implements Listener {
    @EventHandler
    public void PlayerConsumeEvent(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Boolean itemIsMainHand = false;
        ItemStack offHand = player.getInventory().getItemInOffHand();
        Boolean itemIsOffhand = false;
        if (item.equals(mainHand)) {
            itemIsMainHand = true;
        } else if (item.equals(offHand)) {
            itemIsOffhand = true;
        }
        String id = ItemManager.getIdOrNull(item);
        if (id != null) {
            Double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            Integer currentFoodLevel = player.getFoodLevel();
            Float currentSaturation = player.getSaturation();
            Double currentHealth = player.getHealth();
            if (edibleMap.get(id) != null) {
                Boolean finalItemIsMainHand = itemIsMainHand;
                Boolean finalItemIsOffhand = itemIsOffhand;
                Bukkit.getScheduler().runTaskAsynchronously(ItemX.getInstance(), () -> {
                    Integer foodLevel = getFoodLevel(id);
                    Float saturation = getSaturation(id);
                    Float heal = getHeal(id);
                    Float healPercent = getHealPercent(id);
                    Boolean removeBottle = getRemoveBottle(id);

                    Double afterHeal = currentHealth + healPercent * maxHealth + heal;
                    if (afterHeal > maxHealth) {
                        player.setHealth(maxHealth);
                    } else {
                        player.setHealth(afterHeal);
                    }
                    player.setFoodLevel(currentFoodLevel + foodLevel);
                    player.setSaturation(currentSaturation + saturation);
                    if (item.getType().equals(Material.POTION)) {
                        if (removeBottle != null && removeBottle) {
                            if (finalItemIsMainHand) {
                                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            } else if (finalItemIsOffhand) {
                                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                            }
                        }
                    }
                });
                List<String> potionEffectCompounds = getPotionEffects(id);
                if (potionEffectCompounds != null) {
                    for (String compound : potionEffectCompounds) {
                        String[] temp = CompoundUtil.get(compound, ":");
                        PotionEffectType type = PotionEffectType.getByName(temp[0].toUpperCase());
                        Integer duration = Integer.valueOf(StringUtil.replaceRandomInt(temp[1]));
                        Integer amplifier = Integer.valueOf(StringUtil.replaceRandomInt(temp[2]));
                        if (temp.length == 3) {
                            PotionEffect potionEffect = new PotionEffect(type, duration, amplifier);
                            Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                                player.addPotionEffect(potionEffect);
                            });
                        } else if (temp.length == 4) {
                            Boolean ambient = Boolean.getBoolean(temp[3]);
                            PotionEffect potionEffect = new PotionEffect(type, duration, amplifier, ambient);
                            Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                                player.addPotionEffect(potionEffect);
                            });
                        } else if (temp.length == 5) {
                            Boolean ambient = Boolean.getBoolean(temp[3]);
                            Boolean particles = Boolean.getBoolean(temp[4]);
                            PotionEffect potionEffect = new PotionEffect(type, duration, amplifier, ambient, particles);
                            Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                                player.addPotionEffect(potionEffect);
                            });
                        } else if (temp.length == 6) {
                            Boolean ambient = Boolean.getBoolean(temp[3]);
                            Boolean particles = Boolean.getBoolean(temp[4]);
                            Boolean icon = Boolean.getBoolean(temp[5]);
                            PotionEffect potionEffect = new PotionEffect(type, duration, amplifier, ambient, particles, icon);
                            Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                                player.addPotionEffect(potionEffect);
                            });
                        }
                    }
                }
                ItemStack leftover = ItemManager.getItem(getLeftover(id));
                player.getInventory().addItem(leftover);
            }
        }
    }

    private static HashMap<String, HashMap> edibleMap = new HashMap<>();

    public static Integer getFoodLevel(String id) {
        return (Integer) edibleMap.get(id).get("FoodLevel");
    }

    public static Float getSaturation(String id) {
        return Float.valueOf(edibleMap.get(id).get("Saturation").toString());
    }

    public static Float getHeal(String id) {
        return Float.valueOf(edibleMap.get(id).get("Heal").toString());
    }

    public static Float getHealPercent(String id) {
        return Float.valueOf(edibleMap.get(id).get("HealPercent").toString());
    }

    public static List<String> getPotionEffects(String id) {
        return (List<String>) edibleMap.get(id).get("PotionEffects");
    }

    public static Boolean getRemoveBottle(String id) {
        return (Boolean) edibleMap.get(id).get("RemoveBottle");
    }

    public static String getLeftover(String id) {
        return edibleMap.get(id).get("Leftover").toString();
    }

    public static void registerEdibleMechanic(String id, Integer foodLevel, Integer saturation, Double heal, Double healPercent, List<String> potionEffects, Boolean removeBottle, String leftover) {
        Material itemType = ItemManager.getItem(id).getType();
        if (itemType.isEdible() || itemType.equals(Material.POTION)) {
            HashMap map = new HashMap<>();
            map.put("FoodLevel", foodLevel);
            map.put("Saturation", saturation);
            map.put("Heal", heal);
            map.put("HealPercent", healPercent);
            map.put("PotionEffects", potionEffects);
            if (itemType.equals(Material.POTION)) {
                map.put("RemoveBottle", removeBottle);
            }
            map.put("Leftover", leftover);
            edibleMap.put(id, map);
        }
    }

    public static void clearAllMechanics() {
        edibleMap = new HashMap<>();
    }

}
