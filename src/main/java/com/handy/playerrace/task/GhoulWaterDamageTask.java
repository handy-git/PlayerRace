package com.handy.playerrace.task;

import com.handy.lib.api.MessageApi;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author handy
 */
public class GhoulWaterDamageTask {

    /**
     * 食尸鬼在水中受伤
     */
    public static void setWaterDamageTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // 判断是否为食尸鬼
                    String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
                    if (!RaceTypeEnum.GHOUL.getType().equals(raceType)) {
                        continue;
                    }

                    // 判断是不是在水中
                    Material material = player.getLocation().getBlock().getType();
                    if (!Material.WATER.equals(material)) {
                        continue;
                    }
                    int waterDamage = ConfigUtil.raceConfig.getInt("ghoul.waterDamage");

                    double health = player.getHealth() - waterDamage;
                    if (health < 0) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.setHealth(0);
                            }
                        }.runTask(PlayerRace.getInstance());
                    } else {
                        player.setHealth(health);
                        MessageApi.sendActionbar(player, BaseUtil.getLangMsg("ghoul.waterDamageMsg"));
                    }
                }
            }
        }.runTaskTimerAsynchronously(PlayerRace.getInstance(), 0, 20);
    }
}
