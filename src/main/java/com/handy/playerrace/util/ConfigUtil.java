package com.handy.playerrace.util;

import com.handy.lib.api.LangMsgApi;
import com.handy.playerrace.PlayerRace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * @author hs
 * @Description: {}
 * @date 2020/3/23 16:30
 */
public class ConfigUtil {
    public static FileConfiguration config, langConfig, raceConfig;

    /**
     * 初始化加载文件
     */
    public static void enableConfig() {
        // 1:没有文件夹就创建
        if (!PlayerRace.getInstance().getDataFolder().exists()) {
            PlayerRace.getInstance().getDataFolder().mkdir();
        }
        // 2:查询config没有就读取jar中的
        File configFile = new File(PlayerRace.getInstance().getDataFolder(), "config.yml");
        if (!(configFile.exists())) {
            PlayerRace.getInstance().saveDefaultConfig();
        }

        // 加载config
        lordConfig();
        // 加载语言文件
        lordLangConfig();
        // 加载种族配置
        lordRaceConfig();
    }

    /**
     * 加载config
     */
    public static void lordConfig() {
        // 读取信息
        PlayerRace.getInstance().reloadConfig();
        // 加载config
        config = PlayerRace.getInstance().getConfig();
    }

    /**
     * 加载lang文件
     */
    public static void lordLangConfig() {
        File langFile = new File(PlayerRace.getInstance().getDataFolder(), "languages/" + config.getString("language") + ".yml");
        if (!(langFile.exists())) {
            PlayerRace.getInstance().saveResource("languages/" + config.getString("language") + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        LangMsgApi.initLangMsg(langConfig);
    }

    /**
     * 加载race文件
     */
    public static void lordRaceConfig() {
        File raceFile = new File(PlayerRace.getInstance().getDataFolder(), "race.yml");
        if (!(raceFile.exists())) {
            PlayerRace.getInstance().saveResource("race.yml", false);
        }
        raceConfig = YamlConfiguration.loadConfiguration(raceFile);
    }

}
