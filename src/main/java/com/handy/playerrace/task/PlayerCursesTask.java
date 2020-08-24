package com.handy.playerrace.task;

import com.handy.lib.api.MessageApi;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.param.PlayerCursesParam;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/22 15:58
 */
public class PlayerCursesTask {

    /**
     * 异步对被诅咒玩家进行转换
     */
    public static void setPlayerCursesTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 下架的配件直接删除
                Iterator<PlayerCursesParam> iterator = RaceConstants.PLAYER_CURSES.iterator();
                while (iterator.hasNext()) {
                    PlayerCursesParam playerCursesParam = iterator.next();
                    // 判断是否到时间
                    int anInt = ConfigUtil.raceConfig.getInt("ghoul.curseSecond");
                    long second = System.currentTimeMillis() - playerCursesParam.getAddTime() / 1000;
                    if (second >= anInt) {
                        RacePlayerService.getInstance().updateRaceType(playerCursesParam.getPlayerName(), playerCursesParam.getRaceTypeEnum().getType(), 0);
                        iterator.remove();
                        if (playerCursesParam.getPlayer().isOnline()) {
                            MessageApi.sendActionbar(playerCursesParam.getPlayer(), BaseUtil.getLangMsg("ghoul.cursesucceedMsg"));
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(PlayerRace.getInstance(), 0, 20 * 60);
    }

}
