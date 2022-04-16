package com.handy.playerrace.util;

import com.handy.lib.api.LangMsgApi;
import com.handy.lib.util.HandyConfigUtil;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 配置
 *
 * @author handy
 */
public class ConfigUtil {
    public static FileConfiguration CONFIG, LANG_CONFIG, RACE_CONFIG;

    /**
     * 初始化加载文件
     */
    public static void init() {
        // 加载config
        CONFIG = HandyConfigUtil.loadConfig();
        // 加载语言文件
        LANG_CONFIG = HandyConfigUtil.load("languages/" + CONFIG.getString("language") + ".yml");
        // 加载语言到jar
        LangMsgApi.initLangMsg(LANG_CONFIG);
        // 加载种族配置
        RACE_CONFIG = HandyConfigUtil.load("race.yml");
    }

}