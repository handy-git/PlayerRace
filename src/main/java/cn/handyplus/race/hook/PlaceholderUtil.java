package cn.handyplus.race.hook;

import cn.handyplus.race.PlayerRace;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

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

    @Override
    public String getIdentifier() {
        return "PlayerRace";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        RacePlayer racePlayer = CacheUtil.getRacePlayer(player.getUniqueId());
        // %PlayerRace_race%
        if ("race".equals(identifier)) {
            return RaceTypeEnum.getDesc(racePlayer.getRaceType());
        }
        // %PlayerRace_level%
        if ("level".equals(identifier)) {
            return ConfigUtil.RACE_CONFIG.getString(racePlayer.getRaceType() + ".levelTitle." + racePlayer.getRaceLevel());
        }
        // %PlayerRace_level_day%
        if ("level_day".equals(identifier)) {
            return CacheUtil.levelUpDay(racePlayer);
        }
        // %PlayerRace_race_level%
        if ("race_level".equals(identifier)) {
            String levelTitle = ConfigUtil.RACE_CONFIG.getString(racePlayer.getRaceType() + ".levelTitle." + racePlayer.getRaceLevel());
            return RaceTypeEnum.getDesc(racePlayer.getRaceType()) + " " + levelTitle;
        }
        // %PlayerRace_maxfatigue%
        if ("maxfatigue".equals(identifier)) {
            return String.valueOf(racePlayer.getMaxAmount());
        }
        // %PlayerRace_fatigue%
        if ("fatigue".equals(identifier)) {
            return String.valueOf(racePlayer.getAmount());
        }
        // %PlayerRace_race_number%
        if ("race_number".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(racePlayer.getRaceType());
            return String.valueOf(count);
        }
        // 种族数量变量
        if ("mankindnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.MANKIND.getType());
            return String.valueOf(count);
        }
        if ("werWolfnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.WER_WOLF.getType());
            return String.valueOf(count);
        }
        if ("vampirenum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.VAMPIRE.getType());
            return String.valueOf(count);
        }
        if ("ghoulnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.GHOUL.getType());
            return String.valueOf(count);
        }
        if ("demonnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.DEMON.getType());
            return String.valueOf(count);
        }
        if ("angelnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.ANGEL.getType());
            return String.valueOf(count);
        }
        if ("demonhunternum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.DEMON_HUNTER.getType());
            return String.valueOf(count);
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
