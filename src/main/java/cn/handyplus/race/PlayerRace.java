package cn.handyplus.race;

import cn.handyplus.lib.InitApi;
import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.AbstractRaceConstants;
import cn.handyplus.race.hook.PlaceholderUtil;
import cn.handyplus.race.task.TaskManage;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import cn.handyplus.race.util.RaceUtil;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 主类
 *
 * @author handy
 */
public final class PlayerRace extends JavaPlugin {
    public static PlayerRace INSTANCE;
    public static ResidenceApi RES_API;

    @Override
    public void onEnable() {
        INSTANCE = this;
        InitApi initApi = InitApi.getInstance(this);
        // 加载配置文件
        ConfigUtil.init();

        // 加载PlaceholderApi
        this.loadPlaceholder();
        // 加载领地
        this.loadResidence();

        // 注册合成表
        RaceUtil.registerCompound();

        // 初始化
        initApi.checkVersion(ConfigUtil.CONFIG.getBoolean(BaseConstants.IS_CHECK_UPDATE), AbstractRaceConstants.CHECK_VERSION_URL)
                .enableSql("cn.handyplus.race.entity")
                .initCommand("cn.handyplus.race.command")
                .initListener("cn.handyplus.race.listener")
                .addMetrics(8605);

        // 注册定时事件
        TaskManage.start();

        MessageUtil.sendConsoleMessage(ChatColor.GREEN + "已成功载入服务器！");
        MessageUtil.sendConsoleMessage(ChatColor.GREEN + "Author:handy WIKI: https://ricedoc.handyplus.cn/wiki/PlayerRace/README/");
    }

    @Override
    public void onDisable() {
        // 数据保存
        CacheUtil.cache2Db();
        InitApi.disable();
    }

    /**
     * 加载Placeholder
     */
    public void loadPlaceholder() {
        if (Bukkit.getPluginManager().getPlugin(BaseConstants.PLACEHOLDER_API) != null) {
            new PlaceholderUtil(this).register();
            MessageUtil.sendConsoleMessage(BaseUtil.getLangMsg("placeholderAPISucceedMsg"));
            return;
        }
        MessageUtil.sendConsoleMessage(BaseUtil.getLangMsg("placeholderAPIFailureMsg"));
    }

    /**
     * 加载Residence
     */
    public void loadResidence() {
        if (Bukkit.getPluginManager().isPluginEnabled("Residence")) {
            if (Residence.getInstance() == null) {
                MessageUtil.sendConsoleMessage(BaseUtil.getLangMsg("ResidenceFailureMsg"));
                return;
            }
            RES_API = Residence.getInstance().getAPI();
            MessageUtil.sendConsoleMessage(BaseUtil.getLangMsg("ResidenceSucceedMsg"));
            return;
        }
        MessageUtil.sendConsoleMessage(BaseUtil.getLangMsg("ResidenceFailureMsg"));
    }

}