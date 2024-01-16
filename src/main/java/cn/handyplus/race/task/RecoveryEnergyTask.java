package cn.handyplus.race.task;

import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * 异步恢复能量值
 *
 * @author handy
 */
public class RecoveryEnergyTask {

    /**
     * 异步恢复能量值
     */
    public static void setRecoveryFatigueTask() {
        HandySchedulerUtil.runTaskTimerAsynchronously(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Optional<RacePlayer> racePlayerOptional = RacePlayerService.getInstance().findByPlayerName(player.getName());
                if (!racePlayerOptional.isPresent()) {
                    continue;
                }
                RacePlayer racePlayer = racePlayerOptional.get();
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
                RacePlayerService.getInstance().updateAdd(player.getName(), restoreNumber);
            }
        }, 20 * 60, 20 * 60);
    }

}
