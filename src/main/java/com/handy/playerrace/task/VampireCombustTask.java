package com.handy.playerrace.task;

import com.handy.lib.api.MessageApi;
import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/21 10:51
 */
public class VampireCombustTask {

    /**
     * 异步对吸血鬼进行燃烧
     */
    public static void setVampireCombustTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getFireTicks() > 0) {
                        return;
                    }
                    // 判断是否为吸血鬼
                    String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
                    if (!RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
                        continue;
                    }

                    RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
                    if (racePlayer == null || !RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
                        continue;
                    }

                    // 判断是否为白天并且为晴天
                    if (BaseUtil.playerTimeIsNether(player) || BaseUtil.worldTimeIsNight(player) || BaseUtil.worldIsStorm(player)) {
                        continue;
                    }
                    // 判断玩家是不是在水中
                    Material material = player.getLocation().getBlock().getType();
                    if (Material.WATER.equals(material)) {
                        continue;
                    }
                    // 判断带没有金头盔
                    ItemStack helmet = player.getInventory().getHelmet();

                    Integer versionId = VersionCheckEnum.getEnum().getVersionId();
                    Material goldenHelmet = BaseUtil.getMaterial("GOLDEN_HELMET");
                    if (versionId < VersionCheckEnum.V_1_13.getVersionId()) {
                        goldenHelmet = BaseUtil.getMaterial("GOLD_HELMET");
                    }
                    if (helmet != null && goldenHelmet.equals(helmet.getType())) {
                        continue;
                    }
                    // 判断头顶是否有方块
                    if (BaseUtil.isUnderRoof(player)) {
                        continue;
                    }
                    player.setFireTicks(20 * 60);
                    MessageApi.sendActionbar(player, BaseUtil.getLangMsg("vampire.vampireHateSunlight"));
                }
            }
        }.runTaskTimerAsynchronously(PlayerRace.getInstance(), 0, 20);
    }
}
