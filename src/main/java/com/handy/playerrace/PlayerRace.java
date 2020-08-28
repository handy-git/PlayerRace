package com.handy.playerrace;

import com.handy.lib.api.CheckVersionApi;
import com.handy.lib.api.MessageApi;
import com.handy.lib.api.StorageApi;
import com.handy.lib.bstats.bukkit.Metrics;
import com.handy.lib.util.BaseUtil;
import com.handy.lib.util.SqlManagerUtil;
import com.handy.playerrace.command.PlayerRaceCommand;
import com.handy.playerrace.listener.ListenerManage;
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
 * @author hs
 * @Description: {}
 * @date 2020/3/23 13:52
 */
public final class PlayerRace extends JavaPlugin {
    private static PlayerRace instance;

    @Override
    public void onEnable() {
        instance = this;
        // 加载配置文件
        ConfigUtil.enableConfig();
        // 加载数据库
        StorageApi.enableSql(this);
        RacePlayerService.getInstance().create();
        // 加载命令
        this.getCommand("PlayerRace").setExecutor(new PlayerRaceCommand());
        this.getCommand("PlayerRace").setTabCompleter(new PlayerRaceCommand());
        // 加载监听器
        ListenerManage.enableListener(this);

        // 加载PlaceholderApi
        if (!lorePlaceholder()) {
            getLogger().info(BaseUtil.getLangMsg("placeholderAPIFailureMsg"));
        } else {
            getLogger().info(BaseUtil.getLangMsg("placeholderAPISucceedMsg"));
        }

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
        getLogger().info("§aauthor:handy QQ群:1064982471");

        // 注册消息
        MessageApi.initActionBar();

        // 注册合成表
        RaceUtil.registerCompound();

        //注册定时事件
        TaskManage.enableTask();

        // 进行插件使用数据统计
        new Metrics(this, 8605);

        // 版本更新检查
        if (ConfigUtil.config.getBoolean("isCheckUpdate")) {
            CheckVersionApi.checkVersion(this, null, "https://api.github.com/repos/handy-git/PlayerRaceVersion/releases/latest");
        }
        // 进行验签
        // HandyHttpUtil.verifySign(this, "PlayerRace", "1296322710519025664", ConfigUtil.config.getString("sign"));
    }

    @Override
    public void onDisable() {
        // 关闭数据源
        SqlManagerUtil.getInstance().close();
        getLogger().info("§a已成功卸载！");
        getLogger().info("§aauthor:handy QQ群:1064982471");
    }

    public static PlayerRace getInstance() {
        return instance;
    }


    /**
     * 加载Placeholder
     *
     * @return 是否加载
     */
    public boolean lorePlaceholder() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderUtil(this).register();
            return true;
        }
        return false;
    }

}
