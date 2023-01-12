package com.handy.playerrace.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.api.MessageApi;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 狼人相关事件
 *
 * @author handy
 */
@HandyListener
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
        if (BaseUtil.worldTimeIsNotNight(player)) {
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
                Boolean rst = RacePlayerService.getInstance().updateRaceType(player.getName(), RaceTypeEnum.WER_WOLF.getType());
                if (rst) {
                    player.getInventory().addItem(RaceUtil.getRaceHelpBook(RaceTypeEnum.WER_WOLF));
                    player.sendMessage(BaseUtil.getLangMsg("werwolf.succeedMsg"));
                    RaceUtil.refreshCache(player);
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
        if (BaseUtil.worldTimeIsNotNight(player)) {
            return;
        }

        int amount = 0;
        if (livingEntity instanceof Player) {
            amount = ConfigUtil.RACE_CONFIG.getInt("werwolf.killPlayer");
        }
        if (livingEntity instanceof Monster) {
            amount = ConfigUtil.RACE_CONFIG.getInt("werwolf.killMonster");
        }
        if (livingEntity instanceof Animals) {
            amount = ConfigUtil.RACE_CONFIG.getInt("werwolf.killAnimals");
        }
        RaceUtil.restoreEnergy(player, RaceTypeEnum.WER_WOLF, amount);
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

        // 判断是在夜晚
        if (BaseUtil.worldTimeIsNotNight(player)) {
            return;
        }

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
                RaceUtil.restoreEnergy(player, RaceTypeEnum.WER_WOLF, ConfigUtil.RACE_CONFIG.getInt("werwolf.consume"));
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
        if (BaseUtil.worldTimeIsNotNight(player)) {
            return;
        }

        // 判断是否为狼人
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.WER_WOLF.getType().equals(raceType)) {
            return;
        }

        event.setAmount(event.getAmount() + ConfigUtil.RACE_CONFIG.getInt("werwolf.regainHealth"));
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 狼人攻击加成
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();

        int damage = 0;
        Player player = null;
        // 判断是否远程
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                player = (Player) shooter;
                damage = ConfigUtil.RACE_CONFIG.getInt("werwolf.projectileDamage");
            }
        }

        // 判断是否近战
        if ((entity instanceof Player)) {
            player = (Player) entity;
            damage = ConfigUtil.RACE_CONFIG.getInt("werwolf.damage");
        }
        if (player == null) {
            return;
        }

        // 判断是在夜晚
        if (BaseUtil.worldTimeIsNotNight(player)) {
            return;
        }

        // 判断是否为狼人
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.WER_WOLF.getType().equals(raceType)) {
            return;
        }

        event.setDamage(event.getDamage() + ConfigUtil.RACE_CONFIG.getInt("werwolf.damage"));
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

        // 判断是在夜晚
        if (BaseUtil.worldTimeIsNotNight(player)) {
            return;
        }

        // 判断是否为狼人
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.WER_WOLF.getType().equals(raceType)) {
            return;
        }

        event.setDamage(event.getDamage() - ConfigUtil.RACE_CONFIG.getInt("werwolf.fall"));
    }

    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 狼人主动技能-召唤狼
     *
     * @param event 事件
     */
    @EventHandler
    public void onSummonWolf(PlayerInteractEvent event) {
        // 判断是否左击
        if (!Action.LEFT_CLICK_AIR.equals(event.getAction()) && !Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
            return;
        }
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

        int amount = ConfigUtil.RACE_CONFIG.getInt("werwolf.summonWolf");
        Boolean rst = RacePlayerService.getInstance().updateSubtract(player.getName(), amount);
        if (!rst) {
            MessageApi.sendActionbar(player, RaceUtil.getEnergyShortageMsg(amount));
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

        String summonWolfMsg = BaseUtil.getLangMsg("werwolf.summonWolfMsg");
        summonWolfMsg = summonWolfMsg.replace("${amount}", amount + "");
        MessageApi.sendActionbar(player, BaseUtil.replaceChatColor(summonWolfMsg));
    }

    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 狼人主动技能-猛冲
     *
     * @param event 事件
     */
    @EventHandler
    public void onSprint(PlayerInteractEvent event) {
        // 判断是否左击
        if (!Action.LEFT_CLICK_AIR.equals(event.getAction()) && !Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
            return;
        }
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

        int amount = ConfigUtil.RACE_CONFIG.getInt("werwolf.sprint");
        Boolean rst = RacePlayerService.getInstance().updateSubtract(player.getName(), amount);
        if (!rst) {
            MessageApi.sendActionbar(player, RaceUtil.getEnergyShortageMsg(amount));
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
        double sprintDistance = ConfigUtil.RACE_CONFIG.getDouble("werwolf.sprintDistance");
        player.setVelocity(new Vector(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * sprintDistance * sprintDistance,
                -Math.sin(Math.toRadians(pitch)) * sprintDistance * sprintDistance,
                Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * sprintDistance * sprintDistance));

        String sprintMsg = BaseUtil.getLangMsg("werwolf.sprintMsg");
        sprintMsg = sprintMsg.replace("${amount}", amount + "");
        MessageApi.sendActionbar(player, BaseUtil.replaceChatColor(sprintMsg));
    }

}
