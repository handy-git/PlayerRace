package com.handy.playerrace.listener;

import com.handy.lib.api.MessageApi;
import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import com.handy.playerrace.util.RaceUtil;
import com.handy.playerrace.util.RecoveryEnergyUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author hs
 * @Description: {狼人相关事件}
 * @date 2020/8/19 19:10
 */
public class WerWolfEventListener implements Listener {

    /**
     * 当任何一个实体死亡时触发本事件
     * 玩家转换狼人事件
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

        // 判断是在夜晚
        if (RaceUtil.worldTimeIsNight(player)) {
            return;
        }

        // 获取伤害事件
        EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
        // 判断是实体伤害
        if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
            return;
        }
        // 判断伤害者是否为狼
        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
        Entity entity = entityDamageByEntityEvent.getDamager();
        if (!(entity instanceof Wolf)) {
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
                // 设置玩家种族为狼人
                Boolean rst = RacePlayerService.getInstance().updateRaceType(player.getName(), RaceTypeEnum.WER_WOLF.getType(), 0);
                if (rst) {
                    player.sendMessage(BaseUtil.getLangMsg("werwolf.succeedMsg"));
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 当任何一个实体死亡时触发本事件
     * 狼人击杀玩家或者怪物恢复能量
     *
     * @param event 事件
     */
    @EventHandler
    public void killRestoreEnergy(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        // 获取伤害事件
        EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
        // 判断是实体伤害
        if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
            return;
        }
        // 判断伤害者是否为玩家
        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
        Entity entity = entityDamageByEntityEvent.getDamager();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        // 判断是在夜晚
        if (RaceUtil.worldTimeIsNight(player)) {
            return;
        }

        int amount = 0;
        if (livingEntity instanceof Player) {
            amount = ConfigUtil.raceConfig.getInt("werwolf.killPlayer");
        }
        if (livingEntity instanceof Monster) {
            amount = ConfigUtil.raceConfig.getInt("werwolf.killMonster");
        }
        if (livingEntity instanceof Animals) {
            amount = ConfigUtil.raceConfig.getInt("werwolf.killAnimals");
        }
        RecoveryEnergyUtil.restoreEnergy(player, RaceTypeEnum.WER_WOLF, amount);
    }

    /**
     * 当玩家消耗完物品时, 此事件将触发 例如:(食物, 药水, 牛奶桶).
     * 狼人吃东西恢复能量
     *
     * @param event 事件
     */
    @EventHandler
    public void consumeRestoreEnergy(PlayerItemConsumeEvent event) {
        // 事件是否被取消
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        Material itemStackType = event.getItem().getType();

        List<String> materials = new ArrayList<>();
        Integer versionId = VersionCheckEnum.getEnum().getVersionId();
        if (VersionCheckEnum.V_1_7.equals(VersionCheckEnum.getEnum())) {
            // 生牛肉,生鸡肉,生猪排
            materials = Arrays.asList("RAW_BEEF", "RAW_CHICKEN", "PORK");
        } else if (versionId > 7 && versionId < 13) {
            // 生牛肉,生鸡肉,生羊肉,生兔肉,生猪排
            materials = Arrays.asList("RAW_BEEF", "RAW_CHICKEN", "MUTTON", "RABBIT", "PORK");
        } else if (versionId > 12) {
            // 生牛肉,生鸡肉,生羊肉,生兔肉,生猪排
            materials = Arrays.asList("BEEF", "CHICKEN", "MUTTON", "RABBIT", "PORKCHOP");
        }
        for (String materialStr : materials) {
            Material material = Material.valueOf(materialStr);
            if (itemStackType.equals(material)) {
                RecoveryEnergyUtil.restoreEnergy(player, RaceTypeEnum.WER_WOLF, ConfigUtil.raceConfig.getInt("werwolf.consume"));
                return;
            }
        }
    }

    /**
     * 存储健康恢复事件的数据
     * 狼人恢复生命双倍
     *
     * @param event 事件
     */
    @EventHandler
    public void regainHealth(EntityRegainHealthEvent event) {
        // 事件是否被取消
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        // 判断是在夜晚
        if (RaceUtil.worldTimeIsNight(player)) {
            return;
        }

        // 判断是否为狼人
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.WER_WOLF.getType().equals(raceType)) {
            return;
        }

        player.sendMessage("修改前:" + event.getAmount());
        event.setAmount(event.getAmount() * ConfigUtil.raceConfig.getInt("werwolf.regainHealth"));
        player.sendMessage("修改后:" + event.getAmount());
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 狼人攻击翻倍
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        // 判断是在夜晚
        if (RaceUtil.worldTimeIsNight(player)) {
            return;
        }

        // 判断是否为狼人
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.WER_WOLF.getType().equals(raceType)) {
            return;
        }

        player.sendMessage("修改前:" + event.getDamage());
        event.setDamage(event.getDamage() * ConfigUtil.raceConfig.getInt("werwolf.damage"));
        player.sendMessage("修改后:" + event.getDamage());
    }

    /**
     * 储存伤害事件的数据
     * 狼人掉落伤害减倍
     *
     * @param event 事件
     */
    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        // 判断伤害来源是否掉落
        if (!EntityDamageEvent.DamageCause.FALL.equals(event.getCause())) {
            return;
        }

        // 判断是否为狼人
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.WER_WOLF.getType().equals(raceType)) {
            return;
        }

        player.sendMessage("修改前:" + event.getDamage());
        event.setDamage(event.getDamage() / ConfigUtil.raceConfig.getInt("werwolf.fall"));
        player.sendMessage("修改后:" + event.getDamage());
    }

    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 狼人主动技能-召唤狼
     *
     * @param event 事件
     */
    @EventHandler
    public void onSummonWolf(PlayerInteractEvent event) {
        // 判断是否为生猪排
        String material = "PORK";
        if (VersionCheckEnum.getEnum().getVersionId() > 12) {
            material = "PORKCHOP";
        }
        ItemStack item = event.getItem();
        if (item == null || !Material.valueOf(material).equals(item.getType())) {
            return;
        }
        Player player = event.getPlayer();

        // 判断是否为狼人
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.WER_WOLF.getType().equals(raceType)) {
            return;
        }

        // 判断是否为狼人
        RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
        if (racePlayer == null || !RaceTypeEnum.WER_WOLF.getType().equals(racePlayer.getRaceType())) {
            return;
        }

        int amount = ConfigUtil.raceConfig.getInt("werwolf.summonWolf");
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

        // 召唤狼
        Location location = player.getLocation();
        Entity entity = location.getWorld().spawnEntity(location, EntityType.WOLF);
        Wolf wolf = (Wolf) entity;
        wolf.setOwner(player);
    }

    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 狼人主动技能-猛冲
     *
     * @param event 事件
     */
    @EventHandler
    public void onSprint(PlayerInteractEvent event) {
        // 判断是否为羽毛
        ItemStack item = event.getItem();
        if (item == null || !Material.FEATHER.equals(item.getType())) {
            return;
        }
        Player player = event.getPlayer();

        // 判断是否为狼人
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.WER_WOLF.getType().equals(raceType)) {
            return;
        }

        // 判断是否为狼人
        RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
        if (racePlayer == null || !RaceTypeEnum.WER_WOLF.getType().equals(racePlayer.getRaceType())) {
            return;
        }
        int amount = ConfigUtil.raceConfig.getInt("werwolf.sprint");
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

        Location location = player.getLocation();
        double yaw = location.getYaw();
        double pitch = location.getPitch();
        double sprintDistance = ConfigUtil.raceConfig.getDouble("werwolf.sprintDistance");
        player.setVelocity(new Vector(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * sprintDistance * sprintDistance,
                -Math.sin(Math.toRadians(pitch)) * sprintDistance * sprintDistance,
                Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * sprintDistance * sprintDistance));
    }

}
