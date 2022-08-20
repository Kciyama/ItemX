package org.btc.itemx.command;

import org.btc.itemx.ItemX;
import org.btc.itemx.item.ItemManager;
import org.btc.server.utils.ServerUtil;
import org.btc.server.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MainCommand implements CommandExecutor, TabCompleter {

    public static void registerCommand(String... aliases) {
        for (String alias : aliases) {
            ItemX.getInstance().getCommand(alias).setExecutor(new MainCommand());
            ItemX.getInstance().getCommand(alias).setTabCompleter(new MainCommand());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        command.setPermission("itemx.admin");
        command.setPermissionMessage(ItemX.prefix + StringUtil.toFormatted("&4&l你没有权限!"));
        switch (args.length) {
            case 1:
                switch (args[0]) {
                    case "reload":
                        ItemX.getInstance().reloadPlugin();
                        break;
                    case "get":
                        sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c请输入物品名!"));
                        break;
                    case "give":
                        sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c请输入要给予的玩家名!"));
                        break;
                    case "list":
                        sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&a当前物品库共有下列物品:"));
                        for (String id : ItemManager.getItemList()) {
                            sender.sendMessage(id);
                        }
                        break;
                    default:
                        sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c嘛玩意儿?"));
                        break;
                }
                break;
            case 2:
                switch (args[0]) {
                    case "get":
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            try {
                                ItemStack item = ItemManager.getItem(args[1]);
                                player.getInventory().addItem(item);
                                player.sendMessage(ItemX.prefix + StringUtil.toFormatted("&a已将物品&b " + args[1] + "&a 放入你的背包"));
                            } catch (IllegalArgumentException e) {
                                player.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c不存在该物品!"));
                            }
                        } else {
                            sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c只有玩家能输入该指令"));
                        }
                        break;
                    case "give":
                        sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c请输入要给予的物品!"));
                        break;
                    default:
                        sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c嘛玩意儿?"));
                        break;
                }
                break;
            case 3:
                switch (args[0]) {
                    case "get":
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            try {
                                ItemStack item = ItemManager.getItem(args[1]);
                                Integer amount = Integer.valueOf(args[2]);
                                item.setAmount(amount);
                                player.getInventory().addItem(item);
                                player.sendMessage(ItemX.prefix + StringUtil.toFormatted("&a已将物品&b " + args[1] + "&a 放入你的背包"));
                            } catch (NumberFormatException e) {
                                player.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c物品数量不正确!"));
                            } catch (IllegalArgumentException e) {
                                player.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c不存在该物品!"));
                            }
                        } else {
                            sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c只有玩家能输入该指令"));
                        }
                        break;
                    case "give":
                        Player player = Bukkit.getServer().getPlayer(args[1]);
                        try {
                            ItemStack item = ItemManager.getItem(args[2]);
                            player.getInventory().addItem(item);
                            player.sendMessage(StringUtil.toFormatted("&a管理员给了你一个物品, 打开背包看看吧!"));
                            sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&a已将物品&b " + args[2] + "&a 放入&e " + args[1] + "&a 的背包"));
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c不存在该物品!"));
                        }
                        break;
                    default:
                        sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c嘛玩意儿?"));
                        break;
                }
                break;
            case 4:
                switch (args[0]) {
                    case "give":
                        Player player = Bukkit.getServer().getPlayer(args[1]);
                        try {
                            ItemStack item = ItemManager.getItem(args[2]);
                            Integer amount = Integer.valueOf(args[3]);
                            item.setAmount(amount);
                            player.getInventory().addItem(item);
                            player.sendMessage(StringUtil.toFormatted("&a管理员给了你一个物品, 打开背包看看吧!"));
                            sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&a已将物品&b " + args[2] + "&a 放入&e " + args[1] + "&a 的背包"));
                        } catch (NumberFormatException e) {
                            player.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c物品数量不正确!"));
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c不存在该物品!"));
                        }
                        break;
                    default:
                        sender.sendMessage(ItemX.prefix + StringUtil.toFormatted("&c嘛玩意儿?"));
                        break;
                }
                break;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> tabCompletion = new ArrayList<>();
        switch (args.length) {
            case 1:
                tabCompletion.add("get");
                tabCompletion.add("give");
                tabCompletion.add("list");
                tabCompletion.add("reload");
                break;
            case 2:
                if (args[0].equals("get")) {
                    tabCompletion = ItemManager.getItemList();
                } else if (args[0].equals("give")) {
                    tabCompletion = ServerUtil.getAllPlayerNames();
                }

                break;
            case 3:
                if (args[0].equals("give")) {
                    List<String> playerNameList = ServerUtil.getAllPlayerNames();
                    for (String playerName : playerNameList) {
                        if (args[1].equals(playerName)) {
                            tabCompletion = ItemManager.getItemList();
                        }
                    }
                }
        }
        return tabCompletion;
    }
}
