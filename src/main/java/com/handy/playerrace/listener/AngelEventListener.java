package com.handy.playerrace.listener;

import com.handy.lib.api.MessageApi;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * @author hs
 * @Description: {天使相关事件}
 * @date 2020/8/24 17:23
 */
public class AngelEventListener implements Listener {

    /**
     * 当任何一个实体死亡时触发本事件
     * 玩家转换天使事件
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

        // 判断伤害来源是否掉落
        if (!EntityDamageEvent.DamageCause.FALL.equals(entityDamageEvent.getCause())) {
            return;
        }
        // 判断手上是否羽毛
        if (!Material.FEATHER.equals(player.getInventory().getItemInHand().getType())) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                // 判断玩家是否有种族
                String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
                if (!RaceTypeEnum.MANKIND.getType().equals(raceType)) {
                    return;
                }
                // 设置玩家种族为天使
                Boolean rst = RacePlayerService.getInstance().updateRaceType(player.getName(), RaceTypeEnum.ANGEL.getType(), 0);
                if (rst) {
                    player.sendMessage(BaseUtil.getLangMsg("demon.succeedMsg"));
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 天使主动技能: 上天
     *
     * @param event 事件
     */
    @EventHandler
    public void diaup(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        // 判断是否近战
        if (!(damager instanceof Player)) {
            return;
        }

        Player player = (Player) damager;

        // 判断是否拿的羽毛
        ItemStack item = player.getItemInHand();
        if (!Material.FEATHER.equals(item.getType())) {
            return;
        }

        // 判断是否为天使
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.ANGEL.getType().equals(raceType)) {
            return;
        }

        int amount = ConfigUtil.raceConfig.getInt("angel.diaup");
        Boolean rst = RacePlayerService.getInstance().updateSubtract(player.getName(), amount);
        if (!rst) {
            MessageApi.sendActionbar(player, RaceUtil.getEnergyShortageMsg(amount, amount));
            return;
        }

        // 删除物品
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }
        event.setCancelled(true);
        // 判断是否是玩家
        if (entity instanceof Player) {
            Player entityPlayer = (Player) entity;
            entityPlayer.setVelocity(player.getLocation().getDirection().multiply(7));
            entityPlayer.setVelocity(new Vector(entityPlayer.getVelocity().getX(), 10.0D, entityPlayer.getVelocity().getZ()));
            entityPlayer.playSound(entityPlayer.getLocation(), Sound.ENTITY_SLIME_SQUISH, 100.0F, 10.0F);
            player.playSound(entityPlayer.getLocation(), Sound.ENTITY_SLIME_SQUISH, 100.0F, 10.0F);
            entityPlayer.setFallDistance(-100.0F);
            // 发送提醒
            String diaupMsg = BaseUtil.getLangMsg("angel.diaupMsg");
            diaupMsg = diaupMsg.replaceAll("\\$\\{".concat("player").concat("\\}"), player.getName());
            MessageApi.sendActionbar(entityPlayer, BaseUtil.replaceChatColor(diaupMsg));
        } else {
            entity.setVelocity(player.getLocation().getDirection().multiply(7));
            entity.setVelocity(new Vector(entity.getVelocity().getX(), 3.1D, entity.getVelocity().getZ()));
            player.playSound(entity.getLocation(), Sound.ENTITY_SLIME_SQUISH, 100.0F, 10.0F);
        }
    }

}
