package cn.handyplus.race.util;

import cn.handyplus.race.PlayerRace;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.service.RacePlayerService;
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
    public String onRequest(OfflinePlayer player, String identifier) {
        int maxFatigue = ConfigUtil.CONFIG.getInt("maxFatigue");

        RacePlayer racePlayer = CacheUtil.getRacePlayer(player.getUniqueId());
        String raceDesc = RaceTypeEnum.getDesc(racePlayer.getRaceType());

        if (racePlayer.getMaxAmount() != null && racePlayer.getMaxAmount() != 0) {
            maxFatigue = racePlayer.getMaxAmount();
        }
        // 吸血鬼计算最大值
        if (RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
            double energyDiscount = ConfigUtil.RACE_CONFIG.getDouble("vampire.energyDiscount" + racePlayer.getRaceLevel());
            if (energyDiscount > 0) {
                maxFatigue = (int) Math.ceil(maxFatigue * energyDiscount);
            }
        }

        // %PlayerRace_race%
        if ("race".equals(identifier)) {
            return raceDesc;
        }
        // %PlayerRace_maxfatigue%
        if ("maxfatigue".equals(identifier)) {
            return maxFatigue + "";
        }
        // %PlayerRace_fatigue%
        if ("fatigue".equals(identifier)) {
            return racePlayer.getAmount().toString();
        }
        // 种族数量变量
        if ("mankindnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.MANKIND.getType());
            return count + "";
        }
        if ("werWolfnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.WER_WOLF.getType());
            return count + "";
        }
        if ("vampirenum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.VAMPIRE.getType());
            return count + "";
        }
        if ("ghoulnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.GHOUL.getType());
            return count + "";
        }
        if ("demonnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.DEMON.getType());
            return count + "";
        }
        if ("angelnum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.ANGEL.getType());
            return count + "";
        }
        if ("demonhunternum".equals(identifier)) {
            Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.DEMON_HUNTER.getType());
            return count + "";
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
