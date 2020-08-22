package com.handy.playerrace.task;

import com.handy.lib.constants.BaseConstants;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

/**
 * @author hs
 * @Description: {}
 * @date 2020/7/15 13:38
 */
public class RecoveryEnergyTask {

    /**
     * 异步恢复能量值
     */
    public static void setRecoveryFatigueTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ConfigUtil.config.getBoolean("offlineRestore")) {
                    setOfflineRecoveryFatigueTask();
                } else {
                    setOnLineRecoveryFatigueTask();
                }
            }
        }.runTaskTimerAsynchronously(PlayerRace.getInstance(), 20 * 60, 20 * ConfigUtil.config.getInt("restoreSpeed"));
    }


    /**
     * 恢复在线玩家的能量值
     */
    private static void setOnLineRecoveryFatigueTask() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
            if (racePlayer == null) {
                racePlayer = new RacePlayer();
                racePlayer.setPlayerName(player.getName().toLowerCase());
                racePlayer.setPlayerUuid(player.getUniqueId().toString());
                racePlayer.setRaceType(RaceTypeEnum.MANKIND.getType());
                racePlayer.setRaceLevel(0);
                racePlayer.setAmount(ConfigUtil.config.getInt("maxFatigue"));
                RacePlayerService.getInstance().add(racePlayer);
            }
            int maxFatigue = ConfigUtil.config.getInt("maxFatigue");
            if (racePlayer.getMaxAmount() != null && racePlayer.getMaxAmount() != 0) {
                maxFatigue = racePlayer.getMaxAmount();
            }

            // 吸血鬼计算最大值
            if (RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
                double energyDiscount = ConfigUtil.raceConfig.getDouble("vampire.energyDiscount" + racePlayer.getRaceLevel());
                if (energyDiscount > 0) {
                    maxFatigue = (int) Math.ceil(maxFatigue * energyDiscount);
                }
            }

            if (racePlayer.getAmount() >= maxFatigue) {
                continue;
            }
            Integer amount = ConfigUtil.config.getInt("restoreNumber");
            if (player.hasPermission("playerfatigue.vip") && BaseConstants.SIGN) {
                amount = ConfigUtil.config.getInt("vipRestoreNumber");
            }

            if (racePlayer.getAmount() + amount > maxFatigue) {
                amount = maxFatigue - racePlayer.getAmount();
            }
            RacePlayerService.getInstance().updateAdd(player.getName(), amount);
        }
    }

    /**
     * 恢复全部玩家的能量值
     */
    private static void setOfflineRecoveryFatigueTask() {
        int maxFatigue = ConfigUtil.config.getInt("maxFatigue");
        List<RacePlayer> racePlayers = RacePlayerService.getInstance().findAll(maxFatigue);
        if (BaseUtil.colLIsEmpty(racePlayers)) {
            return;
        }
        for (RacePlayer racePlayer : racePlayers) {
            if (racePlayer.getMaxAmount() != null && racePlayer.getMaxAmount() != 0) {
                maxFatigue = racePlayer.getMaxAmount();
            }
            // 吸血鬼计算最大值
            if (RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
                double energyDiscount = ConfigUtil.raceConfig.getDouble("vampire.energyDiscount" + racePlayer.getRaceLevel());
                if (energyDiscount > 0) {
                    maxFatigue = (int) Math.ceil(maxFatigue * energyDiscount);
                }
            }

            if (racePlayer.getAmount() >= maxFatigue) {
                continue;
            }
            Integer amount = ConfigUtil.config.getInt("restoreNumber");

            // 只有在线玩家能获取vip恢复速度
            Player player = Bukkit.getPlayer(UUID.fromString(racePlayer.getPlayerUuid()));
            if (player != null && player.hasPermission("playerrace.vip") && BaseConstants.SIGN) {
                amount = ConfigUtil.config.getInt("vipRestoreNumber");
            }

            if (racePlayer.getAmount() + amount > maxFatigue) {
                amount = maxFatigue - racePlayer.getAmount();
            }
            RacePlayerService.getInstance().updateAdd(racePlayer.getPlayerName(), amount);
        }
    }

}
