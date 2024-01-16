package cn.handyplus.race.task;

import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.ItemStackUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.util.CacheUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author handy
 */
public class VampireCombustTask {

    /**
     * 异步对吸血鬼进行燃烧
     */
    public static void setVampireCombustTask() {
        HandySchedulerUtil.runTaskTimerAsynchronously(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getFireTicks() > 0) {
                    continue;
                }
                // 判断是否为吸血鬼
                if (!CacheUtil.isRaceType(RaceTypeEnum.VAMPIRE, player)) {
                    continue;
                }

                // 判断是否为普通世界白天并且为晴天
                if (!World.Environment.NORMAL.equals(player.getWorld().getEnvironment()) || BaseUtil.worldTimeIsNight(player) || BaseUtil.worldIsStorm(player)) {
                    continue;
                }
                // 判断玩家是不是在水中
                Material material = player.getLocation().getBlock().getType();
                if (Material.WATER.equals(material)) {
                    continue;
                }
                // 判断带没有金头盔
                ItemStack helmet = player.getInventory().getHelmet();
                if (helmet != null && ItemStackUtil.getMaterial("GOLDEN_HELMET").equals(helmet.getType())) {
                    continue;
                }
                // 判断头顶是否有方块
                if (BaseUtil.isUnderRoof(player)) {
                    continue;
                }
                player.setFireTicks(20 * 60);
                MessageUtil.sendActionbar(player, BaseUtil.getLangMsg("vampire.vampireHateSunlight"));
            }
        }, 0, 20);
    }

}