package cn.handyplus.race.task;

import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 异步恢复能量值
 *
 * @author handy
 */
public class RecoveryEnergyTask {

    public static void start() {
        HandySchedulerUtil.runTaskTimerAsynchronously(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                RacePlayer racePlayer = CacheUtil.getRacePlayer(player.getUniqueId());
                RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnum(racePlayer.getRaceType());
                if (raceTypeEnum == null) {
                    continue;
                }
                if (racePlayer.getAmount() >= racePlayer.getMaxAmount()) {
                    continue;
                }
                int restoreNumber = 0;
                switch (raceTypeEnum) {
                    case MANKIND:
                        restoreNumber = ConfigUtil.RACE_CONFIG.getInt("mankind.restoreNumber");
                        break;
                    case WER_WOLF:
                        restoreNumber = ConfigUtil.RACE_CONFIG.getInt("werwolf.restoreNumber");
                        break;
                    case VAMPIRE:
                        restoreNumber = ConfigUtil.RACE_CONFIG.getInt("vampire.restoreNumber");
                        break;
                    case DEMON:
                        restoreNumber = ConfigUtil.RACE_CONFIG.getInt("demon.restoreNumber");
                        break;
                    case ANGEL:
                        restoreNumber = ConfigUtil.RACE_CONFIG.getInt("angel.restoreNumber");
                        break;
                    case GHOUL:
                        restoreNumber = ConfigUtil.RACE_CONFIG.getInt("ghoul.restoreNumber");
                        break;
                    case DEMON_HUNTER:
                        restoreNumber = ConfigUtil.RACE_CONFIG.getInt("demonHunter.restoreNumber");
                        break;
                    default:
                        break;
                }
                if (restoreNumber == 0) {
                    continue;
                }
                if (racePlayer.getAmount() + restoreNumber > racePlayer.getMaxAmount()) {
                    restoreNumber = racePlayer.getMaxAmount() - racePlayer.getAmount();
                }
                CacheUtil.add(player, restoreNumber);
            }
        }, 20 * 60, 20 * 60);
    }

}
