package com.handy.playerrace;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.handy.lib.InitApi;
import com.handy.lib.api.StorageApi;
import com.handy.lib.constants.BaseConstants;
import com.handy.lib.util.BaseUtil;
import com.handy.lib.util.SqlManagerUtil;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.task.TaskManage;
import com.handy.playerrace.util.ConfigUtil;
import com.handy.playerrace.util.PlaceholderUtil;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * 主类
 *
 * @author handy
 */
public final class PlayerRace extends JavaPlugin {
    private static PlayerRace instance;
    private static ResidenceApi resApi;

    @Override
    public void onEnable() {
        instance = this;
        InitApi initApi = InitApi.getInstance(this);
        // 加载配置文件
        ConfigUtil.enableConfig();
        // 加载数据库
        StorageApi.enableSql();
        RacePlayerService.getInstance().create();

        // 加载PlaceholderApi
        this.loadPlaceholder();
        // 加载领地
        this.loadResidence();

        List<String> lordList = Arrays.asList(
                "",
                "§3 ____  _                       ____                ",
                "§3 |  _ \\| | __ _ _   _  ___ _ __|  _ \\ __ _  ___ ___ ",
                "§3 | |_) | |/ _` | | | |/ _ \\ '__| |_) / _` |/ __/ _ \\",
                "§3 |  __/| | (_| | |_| |  __/ |  |  _ < (_| | (_|  __/",
                "§3 |_|   |_|\\__,_|\\__, |\\___|_|  |_| \\_\\__,_|\\___\\___|",
                "§3                |___/                               "
        );
        for (String lord : lordList) {
            getLogger().info(lord);
        }
        getLogger().info("§a已成功载入服务器！");
        getLogger().info("§aAuthor:handy QQ群:1064982471");

        // 注册合成表
        RaceUtil.registerCompound();

        // 初始化
        initApi.checkVersion(ConfigUtil.config.getBoolean(BaseConstants.IS_CHECK_UPDATE), RaceConstants.CHECK_VERSION_URL)
                .initCommand("com.handy.playerrace.command")
                .initSubCommand("com.handy.playerrace.command")
                .initListener("com.handy.playerrace.listener")
                .addMetrics(8605);

        //注册定时事件
        TaskManage.enableTask();
    }

    @Override
    public void onDisable() {
        // 关闭数据源
        SqlManagerUtil.getInstance().close();
        getLogger().info("§a已成功卸载！");
        getLogger().info("§aAuthor:handy QQ群:1064982471");
    }

    public static PlayerRace getInstance() {
        return instance;
    }

    public static ResidenceApi getResidenceApi() {
        return resApi;
    }

    /**
     * 加载Placeholder
     */
    public void loadPlaceholder() {
        if (Bukkit.getPluginManager().getPlugin(BaseConstants.PLACEHOLDER_API) != null) {
            new PlaceholderUtil(this).register();
            getLogger().info(BaseUtil.getLangMsg("placeholderAPISucceedMsg"));
            return;
        }
        getLogger().info(BaseUtil.getLangMsg("placeholderAPIFailureMsg"));
    }

    /**
     * 加载Residence
     */
    public void loadResidence() {
        if (Bukkit.getPluginManager().isPluginEnabled("Residence")) {
            if (Residence.getInstance() == null) {
                getLogger().info(BaseUtil.getLangMsg("ResidenceFailureMsg"));
                return;
            }
            resApi = Residence.getInstance().getAPI();
            getLogger().info(BaseUtil.getLangMsg("ResidenceSucceedMsg"));
            return;
        }
        getLogger().info(BaseUtil.getLangMsg("ResidenceFailureMsg"));
    }

}