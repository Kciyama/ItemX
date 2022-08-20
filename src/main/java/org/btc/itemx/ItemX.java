package org.btc.itemx;

import org.btc.itemx.command.MainCommand;
import org.btc.itemx.handlers.CropHandler;
import org.btc.itemx.handlers.CustomBlockPlaceHandler;
import org.btc.itemx.hooks.MythicMobsHook;
import org.btc.itemx.hooks.ResidenceHook;
import org.btc.itemx.item.ItemBans;
import org.btc.itemx.item.ItemManager;
import org.btc.itemx.item.ItemMaterial;
import org.btc.itemx.listeners.RecipeListener;
import org.btc.itemx.mechanics.Command;
import org.btc.itemx.mechanics.Crop;
import org.btc.itemx.mechanics.Edible;
import org.btc.itemx.mechanics.NoteBlock;
import org.btc.server.utils.FileUtil;
import org.btc.server.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.bukkit.Bukkit.getPluginManager;

public final class ItemX extends JavaPlugin {
    private static ItemX instance;
    private static Long startTime;
    private MythicMobsHook mythicMobsHook = null;
    private ResidenceHook residenceHook = null;
    public ItemXConfig config;
    public static String prefix;

    @Override
    public void onEnable() {
        instance = this;
        startTime = System.currentTimeMillis();
        info(StringUtil.toFormatted("&b插件已启用"));
        info(StringUtil.toFormatted("&b作者: Kciyamat"));
        info(StringUtil.toFormatted("&bQQ: 1446394515"));
        getPluginManager().registerEvents(new ItemBans(), this);
        getPluginManager().registerEvents(new Command(), this);
        getPluginManager().registerEvents(new Edible(), this);
        getPluginManager().registerEvents(new NoteBlock(), this);
        getPluginManager().registerEvents(new CustomBlockPlaceHandler(), this);
        getPluginManager().registerEvents(new Crop(), this);
        getPluginManager().registerEvents(new CropHandler(), this);
        getPluginManager().registerEvents(new RecipeListener(), this);
        config = new ItemXConfig();
        prefix = config.getPrefix();
        MainCommand.registerCommand("itemx", "itx");
        initConfigDir();
        if (getPluginManager().getPlugin("MythicMobs") != null) {
            info(StringUtil.toFormatted("&b发现插件: &dMythicMobs&b, 正在创建钩子..."));
            mythicMobsHook = new MythicMobsHook();
            mythicMobsHook.setEnabled(true);
            getPluginManager().registerEvents(mythicMobsHook, this);
            mythicMobsHook.startTimer();
            info("MythicMobs 钩子创建完成");
        }
        if (getPluginManager().getPlugin("Residence") != null) {
            info(StringUtil.toFormatted("&b发现插件: &dResidence&b, 正在创建钩子..."));
            residenceHook = new ResidenceHook();
            residenceHook.setEnabled(true);
            info("Residence 钩子创建完成");
        }
        handleItems();
        NoteBlock.startPacketListener();
        NoteBlock.startBlockChecker();
        Crop.startCropListener();
    }

    @Override
    public void onDisable() {
        info("插件已卸载");
        info("感谢使用");
    }

    public void reloadPlugin() {
        MythicMobsHook.init();
        initConfigDir();
        handleItems();
    }

    public static ItemX getInstance() {
        return instance;
    }

    public static void info(String msg) {
        instance.getLogger().info(msg);
    }

    public static void warn(String msg) {
        instance.getLogger().warning(msg);
    }

    public MythicMobsHook getMythicMobsHook() {
        return mythicMobsHook;
    }

    public ResidenceHook getResidenceHook() {
        return residenceHook;
    }

    public static Integer getRunningTicks() {
        Long runningMillis = System.currentTimeMillis() - startTime;
        Long runningTicksL = Math.round(Math.floor(runningMillis / 50L));
        Integer runningTicks = runningTicksL.intValue();
        return runningTicks;
    }

    private void initConfigDir() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File itemDir = new File(getDataFolder() + "\\Items");
        if (!itemDir.exists()) {
            itemDir.mkdir();
            info(StringUtil.toFormatted("&c物品文件夹&4 \"Items\" &c未找到, 正在创建"));
        } else {
            info(StringUtil.toFormatted("&b物品文件夹&a \"Items\" &b已找到"));
        }
        File savesDir = new File(getDataFolder() + "\\Saves.yml");
        if (!savesDir.exists()) {
            try {
                info(StringUtil.toFormatted("&c存储文件&4 Saves.yml &c未找到, 正在创建"));
                savesDir.createNewFile();
            } catch (IOException e) {
                info(StringUtil.toFormatted("&c存储文件&4 Saves.yml &c创建失败"));
            }
        }

    }

    private void handleItems() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            ItemManager.clearCache();
            Edible.clearAllMechanics();
            Command.clearAllMechanics();
            NoteBlock.clearAllMechanics();
            Crop.clearAllMechanics();
            File[] itemFiles = FileUtil.getFilesFromDir(this, "\\Items");
            for (File itemFile : itemFiles) {
                YamlConfiguration config = new YamlConfiguration();
                try {
                    config.load(itemFile);
                } catch (IOException | InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                Set<String> keys = config.getKeys(false);
                for (String key : keys) {
                    if (!key.contains(" ")) {
                        ItemManager.registerItem(config, key);
                    }
                }
            }
            Bukkit.getScheduler().runTask(this, () -> {
                Bukkit.resetRecipes();
                for (File itemFile : itemFiles) {
                    YamlConfiguration config = new YamlConfiguration();
                    try {
                        config.load(itemFile);
                    } catch (IOException | InvalidConfigurationException e) {
                        e.printStackTrace();
                    }
                    Set<String> keys = config.getKeys(false);
                    for (String key : keys) {
                        if (!key.contains(" ")) {
                            ItemManager.registerItemRecipe(config, key);
                        }
                    }
                }
            });
            ItemBans.init();
            ItemBans.read();
            ItemMaterial.init();
            ItemMaterial.read();
        });
    }

}
