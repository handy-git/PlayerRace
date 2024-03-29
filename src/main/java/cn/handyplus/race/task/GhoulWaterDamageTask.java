package cn.handyplus.race.task;

import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * 食尸鬼在水中受伤
 *
 * @author handy
 */
public class GhoulWaterDamageTask {

    public static void start() {
        HandySchedulerUtil.runTaskTimerAsynchronously(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // 判断是否为食尸鬼
                if (!CacheUtil.isRaceType(RaceTypeEnum.GHOUL, player)) {
                    return;
                }

                // 判断是不是在水中
                Material material = player.getLocation().getBlock().getType();
                if (!Material.WATER.equals(material)) {
                    continue;
                }
                int waterDamage = ConfigUtil.RACE_CONFIG.getInt("ghoul.waterDamage");

                double health = player.getHealth() - waterDamage;
                if (health < 0) {
                    HandySchedulerUtil.runTask(() -> player.setHealth(0));
                } else {
                    player.setHealth(health);
                    MessageUtil.sendActionbar(player, BaseUtil.getLangMsg("ghoul.waterDamageMsg"));
                }
            }
        }, 0, 20);
    }

}
