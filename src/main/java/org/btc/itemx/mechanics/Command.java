package org.btc.itemx.mechanics;

import org.btc.itemx.item.ItemManager;
import org.btc.server.utils.CompoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Command implements Listener {
    private static HashMap<String, HashMap> commandMap = new HashMap<>();

    @EventHandler
    public void PlayerClickEvent(PlayerInteractEvent e) {
        Action action = e.getAction();
        Player player = e.getPlayer();
        if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
            String mainHandId = ItemManager.getIdOrNull(player.getInventory().getItemInMainHand());
            String offHandId = ItemManager.getIdOrNull(player.getInventory().getItemInOffHand());
            if (offHandId != null) {
                if (commandMap.containsKey(offHandId)) {
                    e.setCancelled(true);
                    if (player.isSneaking()) {
                        List<String> commands = getOffHandCrouchRightClick(offHandId);
                        executeCommand(player, commands);
                    } else {
                        List<String> commands = getOffHandRightClick(offHandId);
                        executeCommand(player, commands);
                    }
                }
            } else if (mainHandId != null) {
                if (commandMap.containsKey(mainHandId)) {
                    e.setCancelled(true);
                    if (player.isSneaking()) {
                        List<String> commands = getMainHandCrouchRightClick(mainHandId);
                        executeCommand(player, commands);
                    } else {
                        List<String> commands = getMainHandRightClick(mainHandId);
                        executeCommand(player, commands);
                    }
                }
            }
        } else if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
            String mainHandId = ItemManager.getIdOrNull(player.getInventory().getItemInMainHand());
            String offHandId = ItemManager.getIdOrNull(player.getInventory().getItemInOffHand());
            if (offHandId != null) {
                if (commandMap.containsKey(offHandId)) {
                    if (player.isSneaking()) {
                        List<String> commands = getOffHandCrouchLeftClick(offHandId);
                        executeCommand(player, commands);
                    } else {
                        List<String> commands = getOffHandLeftClick(offHandId);
                        executeCommand(player, commands);
                    }
                }
            } else if (mainHandId != null) {
                if (commandMap.containsKey(mainHandId)) {
                    if (player.isSneaking()) {
                        List<String> commands = getMainHandCrouchLeftClick(mainHandId);
                        executeCommand(player, commands);
                    } else {
                        List<String> commands = getMainHandLeftClick(mainHandId);
                        executeCommand(player, commands);
                    }
                }
            }

        }

    }

    public List<String> getMainHandRightClick(String id) {
        return (List<String>) commandMap.get(id).get("MainHandRightClick");
    }

    public List<String> getOffHandRightClick(String id) {
        return (List<String>) commandMap.get(id).get("OffHandRightClick");
    }

    public List<String> getMainHandCrouchRightClick(String id) {
        return (List<String>) commandMap.get(id).get("MainHandCrouchRightClick");
    }

    public List<String> getOffHandCrouchRightClick(String id) {
        return (List<String>) commandMap.get(id).get("OffHandCrouchRightClick");
    }

    public List<String> getMainHandLeftClick(String id) {
        return (List<String>) commandMap.get(id).get("MainHandLeftClick");
    }

    public List<String> getOffHandLeftClick(String id) {
        return (List<String>) commandMap.get(id).get("OffHandLeftClick");
    }

    public List<String> getMainHandCrouchLeftClick(String id) {
        return (List<String>) commandMap.get(id).get("MainHandCrouchLeftClick");
    }

    public List<String> getOffHandCrouchLeftClick(String id) {
        return (List<String>) commandMap.get(id).get("OffHandCrouchLeftClick");
    }

    public static void registerCommandMechanic(String id, List<Map> maps) {
        commandMap.put(id, new HashMap<>());
        for (Map map : maps) {
            if (map.containsKey("MainHand")) {
                List<Map> secondMaps = (List<Map>) map.get("MainHand");
                for (Map secondMap : secondMaps) {
                    if (secondMap.containsKey("RightClick")) {
                        List<String> rightClickCommands = (List<String>) secondMap.get("RightClick");
                        commandMap.get(id).put("MainHandRightClick", rightClickCommands);
                    } else if (secondMap.containsKey("LeftClick")) {
                        List<String> leftClickCommands = (List<String>) secondMap.get("LeftClick");
                        commandMap.get(id).put("MainHandLeftClick", leftClickCommands);
                    } else if (secondMap.containsKey("CrouchRightClick")) {
                        List<String> crouchRightClickCommands = (List<String>) secondMap.get("CrouchRightClick");
                        commandMap.get(id).put("MainHandCrouchRightClick", crouchRightClickCommands);
                    } else if (secondMap.containsKey("CrouchLeftClick")) {
                        List<String> crouchLeftClickCommands = (List<String>) secondMap.get("CrouchLeftClick");
                        commandMap.get(id).put("MainHandCrouchLeftClick", crouchLeftClickCommands);
                    }
                }
            } else if (map.containsKey("OffHand")) {
                List<Map> secondMaps = (List<Map>) map.get("OffHand");
                for (Map secondMap : secondMaps) {
                    if (secondMap.containsKey("RightClick")) {
                        List<String> rightClickCommands = (List<String>) secondMap.get("RightClick");
                        commandMap.get(id).put("OffHandRightClick", rightClickCommands);
                    } else if (secondMap.containsKey("LeftClick")) {
                        List<String> leftClickCommands = (List<String>) secondMap.get("LeftClick");
                        commandMap.get(id).put("OffHandLeftClick", leftClickCommands);
                    } else if (secondMap.containsKey("CrouchRightClick")) {
                        List<String> crouchRightClickCommands = (List<String>) secondMap.get("CrouchRightClick");
                        commandMap.get(id).put("OffHandCrouchRightClick", crouchRightClickCommands);
                    } else if (secondMap.containsKey("CrouchLeftClick")) {
                        List<String> crouchLeftClickCommands = (List<String>) secondMap.get("CrouchLeftClick");
                        commandMap.get(id).put("OffHandCrouchLeftClick", crouchLeftClickCommands);
                    }
                }
            }
        }

    }

    public static void clearAllMechanics() {
        commandMap = new HashMap<>();
    }

    public static void executeCommand(Player player, List<String> commandCompounds) {
        if (commandCompounds != null) {
            for (String commandCompound : commandCompounds) {
                String[] temp = CompoundUtil.get(commandCompound);
                if (temp.length == 1) {
                    Bukkit.dispatchCommand(player, temp[0]);
                } else if (temp.length == 2)
                    switch (temp[0].toLowerCase()) {
                        case "server":
                        case "console":
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp[1].replaceAll("\\{Player}", player.getName()));
                            break;
                        case "op":
                        case "admin":
                            if (!player.isOp()) {
                                player.setOp(true);
                                Bukkit.dispatchCommand(player, temp[1].replaceAll("\\{Player}", player.getName()));
                                player.setOp(false);
                            } else {
                                Bukkit.dispatchCommand(player, temp[1].replaceAll("\\{Player}", player.getName()));
                            }
                            break;
                        case "player":
                            Bukkit.dispatchCommand(player, temp[1].replaceAll("\\{Player}", player.getName()));
                            break;
                        default:
                            break;
                    }
            }
        }
    }
}

