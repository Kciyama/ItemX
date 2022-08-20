package org.btc.itemx.item;

import org.btc.itemx.ItemX;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemBans implements Listener {

    private static final File ITEM_BANS = new File(ItemX.getInstance().getDataFolder() + "\\ItemBans.yml");

    @EventHandler
    public void PlayerRightClickEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();
        ItemStack mainHand = player.getInventory().getItemInMainHand().clone();
        mainHand.setAmount(1);
        ItemStack offHand = player.getInventory().getItemInOffHand().clone();
        offHand.setAmount(1);
        if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
            try {
                String id = ItemManager.getIdOrNull(mainHand);
                if (id == null) {
                    for (String itemId : banList) {
                        if (mainHand.equals(ItemManager.getItem(itemId))) {
                            e.setCancelled(true);
                        }
                    }
                } else {
                    for (String itemId : banList) {
                        if (id.equals(itemId)) {
                            e.setCancelled(true);
                        }
                    }
                }
            } catch (NullPointerException ignored) {
            }
        }
    }

    private static List<String> banList = new ArrayList<>();

    public static void init() {
        if (!ITEM_BANS.exists()) {
            try {
                ITEM_BANS.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void read() {

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(ITEM_BANS);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        List<String> itemBanList = config.getStringList("BanList");
        for (String id : itemBanList) {
            banList.add(id);
        }
    }
}
