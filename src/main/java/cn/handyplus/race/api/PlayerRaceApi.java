package cn.handyplus.race.api;

import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.CacheUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * API
 *
 * @author handy
 */
public class PlayerRaceApi {
    private PlayerRaceApi() {
    }

    private static final PlayerRaceApi INSTANCE = new PlayerRaceApi();

    /**
     * 获取api实例
     *
     * @return PlayerRaceApi
     */
    public static PlayerRaceApi getInstance() {
        return INSTANCE;
    }

    /**
     * 临时取消玩家种族之力
     *
     * @param player 玩家
     * @since 1.2.4
     */
    public void temporaryCancel(Player player) {
        CacheUtil.removeCache(player);
    }

    /**
     * 重设玩家种族缓存
     *
     * @param player 玩家
     * @since 1.2.4
     */
    public void resetPlayerRace(Player player) {
        HandySchedulerUtil.runTaskAsynchronously(() -> {
            CacheUtil.db2Cache(player);
        });
    }

    /**
     * 查询种族类型
     *
     * @param playerUuid 玩家uid
     * @return 种族类型
     * @since 1.3.0
     */
    public String findRaceType(UUID playerUuid) {
        return RacePlayerService.getInstance().findRaceType(playerUuid);
    }

    /**
     * 查询种族名称
     *
     * @param playerName 玩家名
     * @return 种族名称
     * @since 1.3.0
     */
    public String findRaceTypeName(String playerName) {
        OfflinePlayer offlinePlayer = BaseUtil.getOfflinePlayer(playerName);
        return RaceTypeEnum.getTypeName(this.findRaceType(offlinePlayer.getUniqueId()));
    }

}