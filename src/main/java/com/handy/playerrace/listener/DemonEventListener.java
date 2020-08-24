package com.handy.playerrace.listener;

import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.service.RacePlayerService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author hs
 * @Description: {恶魔事件}
 * @date 2020/8/22 17:11
 */
public class DemonEventListener implements Listener {

    /**
     * 当任何一个实体死亡时触发本事件
     * 玩家转换恶魔事件
     *
     * @param event 事件
     */
    @EventHandler
    public void playerToDemon(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player) livingEntity;

        // 获取伤害事件
        EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
        // 判断是方块伤害
        if (!(entityDamageEvent instanceof EntityDamageByBlockEvent)) {
            return;
        }
        // 判断方块是否为岩浆
        EntityDamageByBlockEvent entityDamageByBlockEvent = (EntityDamageByBlockEvent) entityDamageEvent;
        Block block = entityDamageByBlockEvent.getDamager();
        if (block == null) {
            return;
        }
        if (VersionCheckEnum.getEnum().getVersionId() < VersionCheckEnum.V_1_13.getVersionId()) {
            if (!Material.LAVA.equals(block.getType()) && !Material.valueOf("STATIONARY_LAVA").equals(block.getType())) {
                return;
            }
        } else {
            if (!Material.LAVA.equals(block.getType())) {
                return;
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                // 判断玩家是否有种族
                String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
                if (!RaceTypeEnum.MANKIND.getType().equals(raceType)) {
                    return;
                }
                // 设置玩家种族为恶魔
                Boolean rst = RacePlayerService.getInstance().updateRaceType(player.getName(), RaceTypeEnum.DEMON.getType(), 0);
                if (rst) {
                    player.sendMessage(BaseUtil.getLangMsg("demon.succeedMsg"));
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

}
