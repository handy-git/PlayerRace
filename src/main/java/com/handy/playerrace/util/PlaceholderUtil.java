package com.handy.playerrace.util;

import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * @author hs
 * @Description: {变量扩展}
 * @date 2020/3/26 18:38
 */
public class PlaceholderUtil extends PlaceholderExpansion {
    private PlayerRace plugin;

    public PlaceholderUtil(PlayerRace plugin) {
        this.plugin = plugin;
    }

    /**
     * 变量前缀
     *
     * @return
     */
    @Override
    public String getIdentifier() {
        return "PlayerRace";
    }

    /**
     * 注册变量
     *
     * @param player
     * @param identifier
     * @return
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null || !player.isOnline()) {
            return "";
        }
        int maxFatigue = ConfigUtil.config.getInt("maxFatigue");
        String race = RaceTypeEnum.MANKIND.getTypeName();

        RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
        if (racePlayer != null && racePlayer.getMaxAmount() != null && racePlayer.getMaxAmount() != 0) {
            maxFatigue = racePlayer.getMaxAmount();
            race = RaceTypeEnum.getEnum(racePlayer.getRaceType()).getTypeName();
        }

        // 吸血鬼计算最大值
        if (racePlayer != null && RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
            double energyDiscount = ConfigUtil.raceConfig.getDouble("vampire.energyDiscount" + racePlayer.getRaceLevel());
            if (energyDiscount > 0) {
                maxFatigue = (int) Math.ceil(maxFatigue * energyDiscount);
            }
        }

        // %PlayerRace_race%
        if ("race".equals(identifier)) {
            return plugin.getConfig().getString("race", race);
        }
        // %PlayerRace_maxFatigue%
        if ("maxFatigue".equals(identifier)) {
            return plugin.getConfig().getString("maxFatigue", maxFatigue + "");
        }
        // %PlayerRace_fatigue%
        if ("fatigue".equals(identifier)) {
            return plugin.getConfig().getString("fatigue", racePlayer != null ? racePlayer.getAmount().toString() : "0");
        }
        return null;
    }

    /**
     * 因为这是一个内部类，
     * 你必须重写这个方法，让PlaceholderAPI知道不要注销你的扩展类
     *
     * @return
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * 因为这是一个内部类，所以不需要进行这种检查
     * 我们可以简单地返回{@code true}
     *
     * @return
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * 作者
     *
     * @return
     */
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * 版本
     *
     * @return
     */
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
