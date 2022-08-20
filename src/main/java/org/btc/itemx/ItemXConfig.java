package org.btc.itemx;

import org.btc.server.utils.StringUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ItemXConfig {
    private final File configFile = new File(ItemX.getInstance().getDataFolder()+"\\config.yml");
    private YamlConfiguration config;

    public ItemXConfig(){
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getPrefix(){
        return StringUtil.toFormatted(config.getString("Prefix"));
    }

}
