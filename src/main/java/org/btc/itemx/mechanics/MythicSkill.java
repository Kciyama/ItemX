package org.btc.itemx.mechanics;

import org.btc.itemx.ItemX;
import org.btc.itemx.enums.SlotType;
import org.btc.itemx.hooks.MythicMobsHook;
import org.btc.server.utils.CompoundUtil;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;

public class MythicSkill {

    public static void registerItemSkillMechanic(String id, List<String> skillMechanics) {
        for (String skillMechanic : skillMechanics) {
            //格式: 技能名-触发方式-触发几率-[可选:触发槽位]-[可选:技能威力]-[可选:消耗法力]-[可选:触发间隔(只适用于Timer触发器)]
            String[] temp = CompoundUtil.get(skillMechanic);
            if (temp.length > 2) {
                String skillName = temp[0];
                String skillTrigger = temp[1].toUpperCase();
                Float skillChance;
                SlotType skillSlot;
                Float skillPower;
                Float skillMana;
                //Parse skill data and put it in the map
                try {
                    skillChance = Float.valueOf(temp[2]);
                } catch (NumberFormatException e) {
                    ItemX.info("不正确的浮点数: 技能几率, 默认使用1 (" + id + ")");
                    skillChance = 1f;
                }
                if (temp.length < 4) {
                    ItemX.info("未找到触发槽位, 默认使用MAINHAND (" + id + ")");
                    skillSlot = SlotType.MAINHAND;
                } else {
                    try {
                        skillSlot = SlotType.valueOf(temp[3]);
                    } catch (IllegalArgumentException e) {
                        ItemX.info("不正确的触发槽位, 默认使用MAINHAND (" + id + ")");
                        skillSlot = SlotType.MAINHAND;
                    }
                }
                if (temp.length < 5) {
                    ItemX.info("未找到技能威力, 默认使用1 (" + id + ")");
                    skillPower = 1f;
                } else {
                    try {
                        skillPower = Float.valueOf(temp[4]);
                    } catch (NumberFormatException e) {
                        ItemX.info("不正确的浮点数: 技能威力, 默认使用1 (" + id + ")");
                        skillPower = 1f;
                    }
                }
                if (temp.length < 6) {
                    ItemX.info("未找到技能消耗魔法, 默认使用0 (" + id + ")");
                    skillMana = 0f;
                } else {
                    try {
                        skillMana = Float.valueOf(temp[5]);
                    } catch (NumberFormatException e) {
                        ItemX.info("不正确的浮点数: 技能消耗魔法, 默认使用0 (" + id + ")");
                        skillMana = 0f;
                    }
                }

                HashMap map = new HashMap<>();
                map.put("SkillName", skillName);
                map.put("SkillChance", skillChance);
                map.put("SkillPower", skillPower);
                map.put("SkillMana", skillMana);
                if (temp[1].toUpperCase().equals("TIMER")) {
                    if (temp.length < 6) {
                        Integer period = 20;
                        map.put("SkillPeriod", period);
                    } else {
                        Integer period = Integer.valueOf(temp[5]);
                        map.put("SkillPeriod", period);
                    }

                }
                String slotIdCompound = skillSlot + "-" + id;

                switch (skillTrigger) {
                    case "ATTACK":
                        MythicMobsHook.attack.put(slotIdCompound, map);
                        break;
                    case "DAMAGE":
                        MythicMobsHook.damage.put(slotIdCompound, map);
                        break;
                    case "JOIN":
                        MythicMobsHook.join.put(slotIdCompound, map);
                        break;
                    case "DEATH":
                        MythicMobsHook.death.put(slotIdCompound, map);
                        break;
                    case "QUIT":
                        MythicMobsHook.quit.put(slotIdCompound, map);
                    case "KILL":
                        MythicMobsHook.kill.put(slotIdCompound, map);
                        break;
                    case "TIMER":
                        MythicMobsHook.timer.put(slotIdCompound, map);
                        break;
                    case "RESPAWN":
                        MythicMobsHook.respawn.put(slotIdCompound, map);
                        break;
                    case "EQUIP":
                        MythicMobsHook.equip.put(slotIdCompound, map);
                        break;
                    case "UNEQUIP":
                        MythicMobsHook.unEquip.put(slotIdCompound, map);
                        break;
                    case "CROUCH":
                        MythicMobsHook.crouch.put(slotIdCompound, map);
                        break;
                    case "UNCROUCH":
                        MythicMobsHook.unCrouch.put(slotIdCompound, map);
                        break;
                    case "BOWSHOOT":
                        MythicMobsHook.bowShoot.put(slotIdCompound, map);
                        break;
                    case "BOWHIT":
                        MythicMobsHook.bowHit.put(slotIdCompound, map);
                        break;
                    case "TRIDENTTHROW":
                        MythicMobsHook.tridentThrow.put(slotIdCompound, map);
                        break;
                    case "TRIDENTHIT":
                        MythicMobsHook.tridentHit.put(slotIdCompound, map);
                        break;
                    case "FISH":
                        MythicMobsHook.fish.put(slotIdCompound, map);
                        break;
                    case "FISHBITE":
                        MythicMobsHook.fishBite.put(slotIdCompound, map);
                        break;
                    case "FISHCATCH":
                        MythicMobsHook.fishCatch.put(slotIdCompound, map);
                        break;
                    case "FISHGRAB":
                        MythicMobsHook.fishGrab.put(slotIdCompound, map);
                        break;
                    case "FISHGROUND":
                        MythicMobsHook.fishGround.put(slotIdCompound, map);
                        break;
                    case "FISHREEL":
                        MythicMobsHook.fishReel.put(slotIdCompound, map);
                        break;
                    case "FISHFAIL":
                        MythicMobsHook.fishFail.put(slotIdCompound, map);
                        break;
                    //比较特别的触发器, 没有槽位
                    case "DROP":
                        MythicMobsHook.drop.put(id, map);
                        break;
                    default:
                        MythicMobsHook.use.put(slotIdCompound, map);
                        break;
                }
            }
        }
    }
}
