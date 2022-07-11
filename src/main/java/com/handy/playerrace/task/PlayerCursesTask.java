package com.handy.playerrace.task;

import cn.handyplus.lib.api.MessageApi;
import cn.handyplus.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.param.PlayerCursesParam;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

/**
 * @author handy
 */
public class PlayerCursesTask {

    /**
     * 异步对被诅咒玩家进行转换
     */
    public static void setPlayerCursesTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<PlayerCursesParam> iterator = RaceConstants.PLAYER_CURSES.iterator();
                while (iterator.hasNext()) {
                    PlayerCursesParam playerCursesParam = iterator.next();
                    // 判断是否到时间
                    int anInt = ConfigUtil.RACE_CONFIG.getInt("ghoul.curseSecond");
                    long second = System.currentTimeMillis() - playerCursesParam.getAddTime() / 1000;
                    if (second >= anInt) {
                        RacePlayerService.getInstance().updateRaceType(playerCursesParam.getPlayerName(), playerCursesParam.getRaceTypeEnum().getType(), 0);
                        iterator.remove();
                        if (playerCursesParam.getPlayer().isOnline()) {
                            playerCursesParam.getPlayer().getInventory().addItem(RaceUtil.getRaceHelpBook(RaceTypeEnum.GHOUL));
                            MessageApi.sendActionbar(playerCursesParam.getPlayer(), BaseUtil.getLangMsg("ghoul.cursesucceedMsg"));
                            RaceUtil.refreshCache(playerCursesParam.getPlayer());
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(PlayerRace.getInstance(), 0, 20 * 60);
    }

}
