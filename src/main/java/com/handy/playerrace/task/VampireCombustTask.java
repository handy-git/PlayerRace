package com.handy.playerrace.task;

import cn.handyplus.lib.api.MessageApi;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.ItemStackUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author handy
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
                    if (RaceUtil.isRaceTypeAndGetRace(RaceTypeEnum.VAMPIRE, player.getName()) == null) {
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

                    Integer versionId = VersionCheckEnum.getEnum().getVersionId();
                    String goldenHelmetStr = "GOLDEN_HELMET";
                    if (versionId < VersionCheckEnum.V_1_13.getVersionId()) {
                        goldenHelmetStr = "GOLD_HELMET";
                    }
                    if (helmet != null && ItemStackUtil.getMaterial(goldenHelmetStr).equals(helmet.getType())) {
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
