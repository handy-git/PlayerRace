package com.handy.playerrace.listener;

import com.handy.lib.util.BaseUtil;
import com.handy.lib.util.HandyHttpUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author hs
 * @Description: {玩家加入游戏的事件.}
 * @date 2020/3/23 14:09
 */
public class PlayerJoinEventListener implements Listener {

    /**
     * 玩家加入初始化种族
     *
     * @param event 进入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                String playerName = BaseUtil.toLowerCase(player.getName());
                RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(playerName);
                if (racePlayer != null) {
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
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * op加入发送更新提醒 进入事件
     *
     * @param event
     */
    @EventHandler
    public void onOpPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigUtil.config.getBoolean("isCheckUpdateToOpMsg")) {
            return;
        }
        HandyHttpUtil.checkVersion(PlayerRace.getInstance(), event.getPlayer(), RaceConstants.CHECK_VERSION_URL);
    }

}
