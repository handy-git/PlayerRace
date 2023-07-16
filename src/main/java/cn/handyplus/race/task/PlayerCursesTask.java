package cn.handyplus.race.task;

import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.PlayerRace;
import cn.handyplus.race.constants.RaceConstants;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.param.PlayerCursesParam;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.ConfigUtil;
import cn.handyplus.race.util.RaceUtil;
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
                        RacePlayerService.getInstance().updateRaceType(playerCursesParam.getPlayerName(), playerCursesParam.getRaceTypeEnum().getType());
                        iterator.remove();
                        if (playerCursesParam.getPlayer().isOnline()) {
                            playerCursesParam.getPlayer().getInventory().addItem(RaceUtil.getRaceHelpBook(RaceTypeEnum.GHOUL));
                            MessageUtil.sendActionbar(playerCursesParam.getPlayer(), BaseUtil.getLangMsg("ghoul.cursesucceedMsg"));
                            RaceUtil.refreshCache(playerCursesParam.getPlayer());
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(PlayerRace.getInstance(), 0, 20 * 60);
    }

}
