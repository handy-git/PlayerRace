package cn.handyplus.race.task;

import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.AbstractRaceConstants;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.param.PlayerCursesParam;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import cn.handyplus.race.util.RaceUtil;

import java.util.Iterator;

/**
 * 异步对被诅咒玩家进行转换
 *
 * @author handy
 */
public class PlayerCursesTask {

    public static void start() {
        HandySchedulerUtil.runTaskTimerAsynchronously(() -> {
            Iterator<PlayerCursesParam> iterator = AbstractRaceConstants.PLAYER_CURSES.iterator();
            while (iterator.hasNext()) {
                PlayerCursesParam playerCursesParam = iterator.next();
                if (!CacheUtil.isRaceType(RaceTypeEnum.MANKIND, playerCursesParam.getPlayer())) {
                    iterator.remove();
                    continue;
                }
                // 判断是否到时间
                int anInt = ConfigUtil.RACE_CONFIG.getInt("ghoul.curseSecond");
                long second = System.currentTimeMillis() - playerCursesParam.getAddTime() / 1000;
                if (second >= anInt) {
                    iterator.remove();
                    CacheUtil.updateRaceType(playerCursesParam.getPlayer().getUniqueId(), playerCursesParam.getRaceTypeEnum());
                    if (playerCursesParam.getPlayer().isOnline()) {
                        playerCursesParam.getPlayer().getInventory().addItem(RaceUtil.getRaceHelpBook(RaceTypeEnum.GHOUL));
                        MessageUtil.sendActionbar(playerCursesParam.getPlayer(), BaseUtil.getLangMsg("ghoul.cursesucceedMsg"));
                    }
                }
            }
        }, 0, 20 * 60);
    }

}
