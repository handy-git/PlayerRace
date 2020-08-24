package com.handy.playerrace.listener;

import com.handy.lib.api.MessageApi;
import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.param.PlayerCursesParam;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/22 14:14
 */
public class GhoulEventListener implements Listener {

    /**
     * 当任何一个实体死亡时触发本事件
     * 玩家转换食尸鬼事件
     *
     * @param event 事件
     */
    @EventHandler
    public void playerToWolf(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player) livingEntity;

        // 获取伤害事件
        EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
        // 判断是实体伤害
        if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
            return;
        }
        // 判断伤害者是否为猪人
        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
        Entity entity = entityDamageByEntityEvent.getDamager();
        if (!(entity instanceof PigZombie)) {
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
                // 设置玩家种族为食尸鬼
                Boolean rst = RacePlayerService.getInstance().updateRaceType(player.getName(), RaceTypeEnum.GHOUL.getType(), 0);
                if (rst) {
                    player.sendMessage(BaseUtil.getLangMsg("ghoul.succeedMsg"));
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 当生物攻击或解除目标时调用
     * 食尸鬼和僵尸休战
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!EntityTargetEvent.TargetReason.CLOSEST_PLAYER.equals(event.getReason())) {
            return;
        }
        Entity target = event.getTarget();
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) target;

        // 判断是否为食尸鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.GHOUL.getType().equals(raceType)) {
            return;
        }

        if (!EntityType.ZOMBIE.equals(event.getEntity().getType())) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 食尸鬼近战吸血
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        // 判断是否近战
        if (!(damager instanceof Player)) {
            return;
        }
        Player player = (Player) damager;

        if (player.getHealth() == player.getMaxHealth()) {
            return;
        }

        // 判断是否为食尸鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.GHOUL.getType().equals(raceType)) {
            return;
        }

        // 扣除能量回血
        new BukkitRunnable() {
            @Override
            public void run() {
                Double finalDamage = event.getFinalDamage();
                Boolean rst = RacePlayerService.getInstance().updateSubtract(player.getName(), finalDamage.intValue());
                if (!rst) {
                    return;
                }

                double health = player.getHealth() + event.getFinalDamage();

                if (health > player.getMaxHealth()) {
                    health = player.getMaxHealth();
                }

                player.setHealth(health);

                // 如果是玩家还要扣除能量值
                Entity entity = event.getEntity();
                if (!(entity instanceof Player)) {
                    return;
                }
                Player entityPlayer = (Player) entity;
                int amount = ConfigUtil.raceConfig.getInt("ghoul.absorptionValue");
                RacePlayerService.getInstance().updateSubtract(entityPlayer.getName(), amount);

                String absorptionMsg = BaseUtil.getLangMsg("ghoul.absorptionMsg");
                absorptionMsg = absorptionMsg
                        .replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "");
                MessageApi.sendActionbar(entityPlayer, absorptionMsg);
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 食尸鬼主动技能-召唤猪人
     *
     * @param event 事件
     */
    @EventHandler
    public void onSummonPigZombie(PlayerInteractEvent event) {
        // 判断是否左击
        if (!Action.LEFT_CLICK_AIR.equals(event.getAction()) && !Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
            return;
        }

        // 判断是否为金块
        ItemStack item = event.getItem();
        if (item == null || !Material.GOLD_BLOCK.equals(item.getType())) {
            return;
        }
        Player player = event.getPlayer();

        // 判断是否为食尸鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.GHOUL.getType().equals(raceType)) {
            return;
        }

        // 判断是否为食尸鬼
        RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
        if (racePlayer == null || !RaceTypeEnum.GHOUL.getType().equals(racePlayer.getRaceType())) {
            return;
        }

        int amount = ConfigUtil.raceConfig.getInt("ghoul.summonPigZombie");
        Boolean rst = RacePlayerService.getInstance().updateSubtract(player.getName(), amount);
        if (!rst) {
            MessageApi.sendActionbar(player, RaceUtil.getEnergyShortageMsg(amount, racePlayer.getAmount()));
            return;
        }

        // 删除物品
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }

        // 获取猪人
        String entityType = "PIG_ZOMBIE";
        if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_15.getVersionId()) {
            entityType = "ZOMBIFIED_PIGLIN";
        }
        // 召唤猪人
        Location location = player.getLocation();
        location.getWorld().spawnEntity(location, EntityType.valueOf(entityType));
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 食尸鬼主动技能: 邪恶诅咒
     *
     * @param event 事件
     */
    @EventHandler
    public void curse(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        // 判断是否近战
        if (!(damager instanceof Player)) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) damager;
        Player playerEntity = (Player) entity;

        // 判断是否拿的骨头
        ItemStack item = player.getItemInHand();
        if (!Material.BONE.equals(item.getType())) {
            return;
        }

        // 判断是否为食尸鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.GHOUL.getType().equals(raceType)) {
            return;
        }

        int amount = ConfigUtil.raceConfig.getInt("ghoul.curse");
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

        RaceConstants.PLAYER_CURSES.add(new PlayerCursesParam(playerEntity, playerEntity.getName().toLowerCase(), System.currentTimeMillis(), RaceTypeEnum.GHOUL));
        MessageApi.sendActionbar(playerEntity, BaseUtil.getLangMsg("ghoul.curseMsg"));
    }

}
