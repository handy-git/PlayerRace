package com.handy.playerrace.util;

import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * 变量扩展
 *
 * @author handy
 */
public class PlaceholderUtil extends PlaceholderExpansion {
    private final PlayerRace plugin;

    public PlaceholderUtil(PlayerRace plugin) {
        this.plugin = plugin;
    }

    /**
     * 变量前缀
     *
     * @return 结果
     */
    @Override
    public String getIdentifier() {
        return "PlayerRace";
    }

    /**
     * 注册变量
     *
     * @param player     玩家
     * @param identifier 变量
     * @return 结果
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        int maxFatigue = ConfigUtil.config.getInt("maxFatigue");
        String race = RaceTypeEnum.MANKIND.getTypeName();

        RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
        if (racePlayer != null) {
            race = RaceTypeEnum.getEnum(racePlayer.getRaceType()).getTypeName();
            if (racePlayer.getMaxAmount() != null && racePlayer.getMaxAmount() != 0) {
                maxFatigue = racePlayer.getMaxAmount();
            }
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
        // %PlayerRace_maxfatigue%
        if ("maxfatigue".equals(identifier)) {
            return plugin.getConfig().getString("maxfatigue", maxFatigue + "");
        }
        // %PlayerRace_fatigue%
        if ("fatigue".equals(identifier)) {
            return plugin.getConfig().getString("fatigue", racePlayer != null ? racePlayer.getAmount().toString() : "0");
        }
        // 种族数量变量
        if ("mankindnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.MANKIND.getType());
            return plugin.getConfig().getString("mankindnum", count + "");
        }
        if ("werWolfnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.WER_WOLF.getType());
            return plugin.getConfig().getString("werWolfnum", count + "");
        }
        if ("vampirenum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.VAMPIRE.getType());
            return plugin.getConfig().getString("vampirenum", count + "");
        }
        if ("ghoulnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.GHOUL.getType());
            return plugin.getConfig().getString("ghoulnum", count + "");
        }
        if ("demonnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.DEMON.getType());
            return plugin.getConfig().getString("demonnum", count + "");
        }
        if ("angelnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.ANGEL.getType());
            return plugin.getConfig().getString("angelnum", count + "");
        }
        if ("demonhunternum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.DEMON_HUNTER.getType());
            return plugin.getConfig().getString("demonhunternum", count + "");
        }
        return null;
    }

    /**
     * 因为这是一个内部类，
     * 你必须重写这个方法，让PlaceholderAPI知道不要注销你的扩展类
     *
     * @return 结果
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * 因为这是一个内部类，所以不需要进行这种检查
     * 我们可以简单地返回{@code true}
     *
     * @return 结果
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * 作者
     *
     * @return 结果
     */
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * 版本
     *
     * @return 结果
     */
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
