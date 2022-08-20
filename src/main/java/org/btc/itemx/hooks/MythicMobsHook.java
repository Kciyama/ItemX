package org.btc.itemx.hooks;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.collect.Lists;
import io.lumine.mythic.api.mobs.GenericCaster;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillMetadataImpl;
import io.lumine.mythic.core.skills.SkillTriggers;
import org.btc.itemx.ItemX;
import org.btc.itemx.enums.SlotType;
import org.btc.itemx.item.ItemManager;
import org.btc.server.utils.ServerUtil;
import org.btc.server.utils.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.List;

public class MythicMobsHook implements Listener {
    /*技能Map数据结构:
    SkillName: 技能名
    SkillChance: 技能触发几率
    SkillMana: 技能消耗的魔法
    SkillPower: 技能威力
    */
    private Boolean enabled;
    public static HashMap<String, HashMap> timer;
    public static HashMap<String, HashMap> use;
    public static HashMap<String, HashMap> attack;
    public static HashMap<String, HashMap> damage;
    public static HashMap<String, HashMap> join;
    public static HashMap<String, HashMap> death;
    public static HashMap<String, HashMap> quit;
    public static HashMap<String, HashMap> kill;
    public static HashMap<String, HashMap> respawn;
    public static HashMap<String, HashMap> equip;
    public static HashMap<String, HashMap> unEquip;
    public static HashMap<String, HashMap> crouch;
    public static HashMap<String, HashMap> unCrouch;
    public static HashMap<String, HashMap> bowShoot;
    public static HashMap<String, HashMap> bowHit;
    public static HashMap<String, HashMap> tridentThrow;
    public static HashMap<String, HashMap> tridentHit;
    public static HashMap<String, HashMap> fish;
    public static HashMap<String, HashMap> fishBite;
    public static HashMap<String, HashMap> fishCatch;
    public static HashMap<String, HashMap> fishGrab;
    public static HashMap<String, HashMap> fishGround;
    public static HashMap<String, HashMap> fishReel;
    public static HashMap<String, HashMap> fishFail;
    public static HashMap<String, HashMap> drop;
    //这个是记录器
    //数据格式:  HashMap<玩家, HashMap<"槽位-ID", 记录的时间>>
    public static HashMap<Player, HashMap<String, Integer>> playerTimer;

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void startTimer() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(ItemX.getInstance(), () -> {
            for (Player player : ServerUtil.getAllPlayers()) {
                castAllSlot(player, timer);
            }
        }, 0, 1);
    }

    public void castSkill(Player player, String skillName, Float skillMana, Float skillPower) {
        if (ItemX.getInstance().getMythicMobsHook().isEnabled()) {
            GenericCaster caster = new GenericCaster(BukkitAdapter.adapt(player));
            Skill skill = MythicBukkit.inst().getSkillManager().getSkill(skillName).get();
            SkillMetadataImpl metadata = new SkillMetadataImpl(SkillTriggers.API, caster, BukkitAdapter.adapt(player), BukkitAdapter.adapt(player.getLocation()), Lists.newArrayList(), Lists.newArrayList(), skillPower);
            if (skill.isUsable(metadata)) {
                skill.execute(metadata);
            }
        }
    }

    public void castSkill(Player player, HashMap skillMap) {
        String skillName = skillMap.get("SkillName").toString();
        Float skillChance = Float.valueOf(skillMap.get("SkillChance").toString());
        Float skillMana = Float.valueOf(skillMap.get("SkillMana").toString());
        Float skillPower = Float.valueOf(skillMap.get("SkillPower").toString());
        if (NumberUtil.chanceOf(Double.valueOf(skillChance.toString()))) {
            castSkill(player, skillName, skillMana, skillPower);
        }
    }

    public MythicMobsHook() {
        init();
    }

    public static void init() {
        //数据结构: HashMap<"槽位-ID", 技能Map>
        use = new HashMap<String, HashMap>();
        attack = new HashMap<String, HashMap>();
        damage = new HashMap<String, HashMap>();
        join = new HashMap<String, HashMap>();
        death = new HashMap<String, HashMap>();
        quit = new HashMap<String, HashMap>();
        kill = new HashMap<String, HashMap>();
        timer = new HashMap<String, HashMap>();
        respawn = new HashMap<String, HashMap>();
        equip = new HashMap<String, HashMap>();
        unEquip = new HashMap<String, HashMap>();
        crouch = new HashMap<String, HashMap>();
        unCrouch = new HashMap<String, HashMap>();
        bowShoot = new HashMap<String, HashMap>();
        bowHit = new HashMap<String, HashMap>();
        tridentThrow = new HashMap<String, HashMap>();
        tridentHit = new HashMap<String, HashMap>();
        fish = new HashMap<String, HashMap>();
        fishBite = new HashMap<String, HashMap>();
        fishCatch = new HashMap<String, HashMap>();
        fishGrab = new HashMap<String, HashMap>();
        fishGround = new HashMap<String, HashMap>();
        fishReel = new HashMap<String, HashMap>();
        fishFail = new HashMap<String, HashMap>();
        //较为特殊, 数据结构: HashMap<ID, 技能Map>
        drop = new HashMap<String, HashMap>();
        playerTimer = new HashMap<Player, HashMap<String, Integer>>();
        for (Player player : ServerUtil.getAllPlayers()) {
            playerTimer.put(player, new HashMap<>());
        }
    }

    public void castSlot(Player player, HashMap trigger, SlotType type, String id) {
        String marker = type + "-" + id;
        if (trigger.containsKey(marker)) {
            HashMap map = (HashMap) trigger.get(marker);
            if (NumberUtil.chanceOf(Double.valueOf(map.get("SkillChance").toString()))) {
                String skillName = (String) map.get("SkillName");
                Float mana = Float.valueOf(map.get("SkillMana").toString());
                Float power = Float.valueOf(map.get("SkillPower").toString());
                castSkill(player, skillName, mana, power);
            }
        }
    }

    public Boolean checkSlot(HashMap trigger, SlotType type, String id) {
        String marker = type + "-" + id;
        if (trigger.containsKey(marker)) {
            return true;
        } else {
            return false;
        }
    }

    public HashMap getSkillMap(HashMap trigger, SlotType type, String id) {
        return (HashMap) trigger.get(type + "-" + id);
    }

    public void castAllSlot(Player player, HashMap trigger) {
        Bukkit.getScheduler().runTaskAsynchronously(ItemX.getInstance(), () -> {
            String[] temp = getAllSlotIds(player);
            if (temp.length < 7) {
                try {
                    temp[0] = "MAINHAND-" + temp[0];
                    temp[1] = "OFFHAND-" + temp[1];
                    temp[2] = "HELMET-" + temp[2];
                    temp[3] = "CHESTPLATE-" + temp[3];
                    temp[4] = "LEGGINGS-" + temp[4];
                    temp[5] = "BOOTS-" + temp[5];
                    if (trigger.equals(timer)) {
                        for (Integer i = 0; i < 6; i++) {
                            if (trigger.containsKey(temp[i])) {
                                if (playerTimer.get(player).containsKey(temp[i])) {
                                    playerTimer.get(player).put(temp[i], (playerTimer.get(player).get(temp[i])) + 1);
                                } else {
                                    playerTimer.get(player).put(temp[i], 1);
                                }
                                final Integer time = playerTimer.get(player).get(temp[i]);
                                Integer period = (Integer) timer.get(temp[i]).get("SkillPeriod");
                                Integer finalI = i;
                                Bukkit.getScheduler().runTask(ItemX.getInstance(), () -> {
                                    if (time > period) {
                                        Integer savedTime = time - period;
                                        playerTimer.get(player).put(temp[finalI], savedTime);
                                        Bukkit.getScheduler().runTaskAsynchronously(ItemX.getInstance(), () -> {
                                            HashMap skillMap = timer.get(temp[finalI]);
                                            castSkill(player, skillMap);
                                        });
                                    }
                                });
                            }
                        }
                    } else {
                        for (Integer i = 0; i < 6; i++) {
                            if (trigger.containsKey(temp[i])) {
                                HashMap map = (HashMap) trigger.get(temp[i]);
                                if (NumberUtil.chanceOf(Double.valueOf(map.get("SkillChance").toString()))) {
                                    String skillName = (String) map.get("SkillName");
                                    Float mana = Float.valueOf(map.get("SkillMana").toString());
                                    Float power = Float.valueOf(map.get("SkillPower").toString());
                                    castSkill(player, skillName, mana, power);
                                    if (!trigger.equals(bowHit) && !trigger.equals(bowShoot) && !trigger.equals(tridentHit) && !trigger.equals(tridentThrow)) {
                                        if (temp[i].contains("MAINHAND")) {
                                            player.swingMainHand();
                                        } else if (temp[i].contains("OFFHAND")) {
                                            player.swingOffHand();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
        });
    }

    @EventHandler
    public void PlayerClickEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();
        if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
            castAllSlot(player, use);
        } else if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
            castAllSlot(player, attack);
        }

    }

    @EventHandler
    public void PlayerDamageEvent(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player player) {
            castAllSlot(player, damage);
        }
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        playerTimer.put(player, new HashMap<>());
        castAllSlot(player, join);
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent e) {
        Player player = e.getEntity();
        playerTimer.remove(player);
        castAllSlot(player, death);
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        playerTimer.remove(player);
        castAllSlot(player, quit);
    }

    @EventHandler
    public void PlayerKillEvent(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            if (e.getEntity().isDead()) {
                castAllSlot(player, kill);
            }
        }
    }

    @EventHandler
    public void PlayerRespawnEvent(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        castAllSlot(player, respawn);
    }

    //需要Paper
    @EventHandler
    public void PlayerEquipEvent(PlayerArmorChangeEvent e) {
        Player player = e.getPlayer();
        ItemStack oldItem = e.getOldItem();
        ItemStack newItem = e.getNewItem();
        switch (e.getSlotType()) {
            case HEAD:
                if (oldItem.equals(new ItemStack(Material.AIR))) {
                    castSlot(player, equip, SlotType.HELMET, ItemManager.getIdOrNull(newItem));
                } else {
                    castSlot(player, unEquip, SlotType.HELMET, ItemManager.getIdOrNull(oldItem));
                }
                break;
            case CHEST:
                if (oldItem.equals(new ItemStack(Material.AIR))) {
                    castSlot(player, equip, SlotType.CHESTPLATE, ItemManager.getIdOrNull(newItem));
                } else {
                    castSlot(player, unEquip, SlotType.CHESTPLATE, ItemManager.getIdOrNull(oldItem));
                }
                break;
            case LEGS:
                if (oldItem.equals(new ItemStack(Material.AIR))) {
                    castSlot(player, equip, SlotType.LEGGINGS, ItemManager.getIdOrNull(newItem));
                } else {
                    castSlot(player, unEquip, SlotType.LEGGINGS, ItemManager.getIdOrNull(oldItem));
                }
                break;
            case FEET:
                if (oldItem.equals(new ItemStack(Material.AIR))) {
                    castSlot(player, equip, SlotType.BOOTS, ItemManager.getIdOrNull(newItem));
                } else {
                    castSlot(player, unEquip, SlotType.BOOTS, ItemManager.getIdOrNull(oldItem));
                }
                break;
        }
    }

    @EventHandler
    public void PlayerToggleSneakEvent(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (e.isSneaking()) {
            castAllSlot(player, crouch);
        } else {
            castAllSlot(player, unCrouch);
        }
    }

    @EventHandler
    public void PlayerShootEvent(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player player) {
            Entity projectile = e.getProjectile();
            if (projectile instanceof Arrow) {
                castAllSlot(player, bowShoot);
                String mainHandId = ItemManager.getIdOrNull(player.getInventory().getItemInMainHand());
                String offHandId = ItemManager.getIdOrNull(player.getInventory().getItemInOffHand());
                String helmetId = ItemManager.getIdOrNull(player.getInventory().getHelmet());
                String chestplateId = ItemManager.getIdOrNull(player.getInventory().getChestplate());
                String leggingsId = ItemManager.getIdOrNull(player.getInventory().getLeggings());
                String bootsId = ItemManager.getIdOrNull(player.getInventory().getBoots());
                FixedMetadataValue playerMetaData = new FixedMetadataValue(ItemX.getInstance(), player);
                projectile.setMetadata("Shooter", playerMetaData);
                if (mainHandId != null && checkSlot(bowHit, SlotType.MAINHAND, mainHandId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.MAINHAND, mainHandId);
                } else if (offHandId != null && checkSlot(bowHit, SlotType.OFFHAND, offHandId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.OFFHAND, offHandId);
                } else if (helmetId != null && checkSlot(bowHit, SlotType.HELMET, helmetId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.HELMET, helmetId);
                } else if (chestplateId != null && checkSlot(bowHit, SlotType.CHESTPLATE, chestplateId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.CHESTPLATE, chestplateId);
                } else if (leggingsId != null && checkSlot(bowHit, SlotType.LEGGINGS, leggingsId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.LEGGINGS, leggingsId);
                } else if (bootsId != null && checkSlot(bowHit, SlotType.BOOTS, bootsId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.BOOTS, bootsId);
                }
            } else if (e.getProjectile() instanceof Trident) {
                castAllSlot(player, tridentThrow);
                String mainHandId = ItemManager.getIdOrNull(player.getInventory().getItemInMainHand());
                String offHandId = ItemManager.getIdOrNull(player.getInventory().getItemInOffHand());
                String helmetId = ItemManager.getIdOrNull(player.getInventory().getHelmet());
                String chestplateId = ItemManager.getIdOrNull(player.getInventory().getChestplate());
                String leggingsId = ItemManager.getIdOrNull(player.getInventory().getLeggings());
                String bootsId = ItemManager.getIdOrNull(player.getInventory().getBoots());
                if (mainHandId != null && checkSlot(tridentHit, SlotType.MAINHAND, mainHandId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.MAINHAND, mainHandId);
                } else if (offHandId != null && checkSlot(tridentHit, SlotType.OFFHAND, offHandId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.OFFHAND, offHandId);
                } else if (helmetId != null && checkSlot(tridentHit, SlotType.HELMET, helmetId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.HELMET, helmetId);
                } else if (chestplateId != null && checkSlot(tridentHit, SlotType.CHESTPLATE, chestplateId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.CHESTPLATE, chestplateId);
                } else if (leggingsId != null && checkSlot(tridentHit, SlotType.LEGGINGS, leggingsId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.LEGGINGS, leggingsId);
                } else if (bootsId != null && checkSlot(tridentHit, SlotType.BOOTS, bootsId)) {
                    addSkillMetadataToProjectile(projectile, SlotType.BOOTS, bootsId);
                }
            }
        }
    }

    @EventHandler
    public void ProjectileHitEvent(ProjectileHitEvent e) {
        Entity projectile = e.getEntity();
        List<MetadataValue> shooterMetadatas = projectile.getMetadata("Shooter");
        Player player = null;
        for (MetadataValue shooterMetadata : shooterMetadatas) {
            if (shooterMetadata.getOwningPlugin().equals(ItemX.getInstance())) {
                player = (Player) shooterMetadata.value();
            }
        }
        if (player != null) {
            List<MetadataValue> skillMetadatas = projectile.getMetadata("SkillList");
            for (MetadataValue skillMetadata : skillMetadatas) {
                if (skillMetadata.getOwningPlugin().equals(ItemX.getInstance())) {
                    List<HashMap> skillMaps = (List<HashMap>) skillMetadata.value();
                    for (HashMap skillMap : skillMaps) {
                        castSkill(player, skillMap);
                    }
                }
            }
        }
    }

    @EventHandler
    public void PlayerFishEvent(PlayerFishEvent e) {
        Player player = e.getPlayer();
        switch (e.getState()) {
            case FISHING:
                castAllSlot(player, fish);
                break;
            case BITE:
                castAllSlot(player, fishBite);
                break;
            case CAUGHT_FISH:
                castAllSlot(player, fishCatch);
                break;
            case CAUGHT_ENTITY:
                castAllSlot(player, fishGrab);
                break;
            case IN_GROUND:
                castAllSlot(player, fishGround);
                break;
            case REEL_IN:
                castAllSlot(player, fishReel);
                break;
            case FAILED_ATTEMPT:
                castAllSlot(player, fishFail);
        }
    }

    private static String[] getAllSlotIds(Player player) {
        String[] temp = new String[6];
        temp[0] = ItemManager.getIdOrNull(player.getInventory().getItemInMainHand());
        temp[1] = ItemManager.getIdOrNull(player.getInventory().getItemInOffHand());
        temp[2] = ItemManager.getIdOrNull(player.getInventory().getHelmet());
        temp[3] = ItemManager.getIdOrNull(player.getInventory().getChestplate());
        temp[4] = ItemManager.getIdOrNull(player.getInventory().getLeggings());
        temp[5] = ItemManager.getIdOrNull(player.getInventory().getBoots());
        return temp;
    }

    private void addSkillMetadataToProjectile(Entity projectile, SlotType type, String id) {
        HashMap skillMap = new HashMap<>();
        if (projectile instanceof Arrow) {
            skillMap = getSkillMap(bowHit, type, id);
        } else if (projectile instanceof Trident) {
            skillMap = getSkillMap(tridentHit, type, id);
        } else {
        }
        FixedMetadataValue skillMetaData = new FixedMetadataValue(ItemX.getInstance(), skillMap);
        projectile.setMetadata("SkillList", skillMetaData);
    }

}
