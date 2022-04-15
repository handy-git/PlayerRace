package com.handy.playerrace.api;

import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
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

}