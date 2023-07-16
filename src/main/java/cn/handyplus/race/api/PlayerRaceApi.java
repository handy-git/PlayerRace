package cn.handyplus.race.api;

import cn.handyplus.race.PlayerRace;
import cn.handyplus.race.constants.RaceConstants;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.service.RacePlayerService;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 种族api
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
        RaceConstants.PLAYER_RACE.remove(player.getName());
    }

    /**
     * 重设玩家种族缓存
     *
     * @param player 玩家
     * @since 1.2.4
     */
    public void resetPlayerRace(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String playerName = player.getName();
                RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(playerName);
                if (racePlayer != null) {
                    RaceConstants.PLAYER_RACE.put(playerName, racePlayer);
                    return;
                }
                racePlayer = new RacePlayer();
                racePlayer.setPlayerName(playerName);
                racePlayer.setPlayerUuid(player.getUniqueId().toString());
                racePlayer.setRaceType(RaceTypeEnum.MANKIND.getType());
                racePlayer.setAmount(0);
                racePlayer.setMaxAmount(0);
                racePlayer.setTransferTime(0L);
                RacePlayerService.getInstance().add(racePlayer);
                RaceConstants.PLAYER_RACE.put(playerName, racePlayer);
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 查询种族类型
     *
     * @param playerName 玩家名
     * @return 种族类型
     * @since 1.3.0
     */
    public String findRaceType(String playerName) {
        return RacePlayerService.getInstance().findRaceType(playerName);
    }

    /**
     * 查询种族名称
     *
     * @param playerName 玩家名
     * @return 种族名称
     * @since 1.3.0
     */
    public String findRaceTypeName(String playerName) {
        return RaceTypeEnum.getTypeName(this.findRaceType(playerName));
    }

}