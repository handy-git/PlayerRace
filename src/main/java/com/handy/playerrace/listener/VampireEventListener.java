package com.handy.playerrace.listener;

import com.handy.lib.api.MessageApi;
import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import com.handy.playerrace.util.RaceUtil;
import com.handy.playerrace.util.RecoveryEnergyUtil;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author hs
 * @Description: {吸血鬼相关事件}
 * @date 2020/8/20 17:52
 */
public class VampireEventListener implements Listener {

    /**
     * 当任何一个实体死亡时触发本事件
     * 掉落吸血鬼知识之书
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        // 判断杀手是不是玩家
        if (player == null) {
            return;
        }
        //注意：这里从0算起
        int anInt = ConfigUtil.raceConfig.getInt("vampire.dropRate");
        if (anInt == 0) {
            return;
        }
        int dropRate = new Random().nextInt(anInt);
        if (dropRate == 0) {
            event.getDrops().add(RaceUtil.getKnowledgeBook());
            player.sendMessage(BaseUtil.getLangMsg("vampire.bookSucceedMsg"));
        }
    }

    /**
     * 当玩家消耗完物品时, 此事件将触发 例如:(食物, 药水, 牛奶桶).
     * 人类转变吸血鬼始祖事件
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
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack itemStack = event.getItem();
                if (!itemStack.isSimilar(RaceUtil.getItemStack())) {
                    return;
                }
                // 判断是否为人类或者吸血鬼
                String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
                if (!RaceTypeEnum.MANKIND.getType().equals(raceType) && !RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
                    return;
                }
                // 判断是否已经是吸血鬼了
                if (RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
                    RecoveryEnergyUtil.restoreEnergy(player, RaceTypeEnum.VAMPIRE, ConfigUtil.raceConfig.getInt("vampire.cainBlood"));
                    return;
                }
                // 判断是否为第一只吸血鬼
                Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.VAMPIRE.getType());
                if (count != 0) {
                    return;
                }
                // 设置玩家种族为吸血鬼始祖
                Boolean rst = RacePlayerService.getInstance().updateRaceType(player, player.getName(), RaceTypeEnum.VAMPIRE.getType(), 1);
                if (rst) {
                    player.sendMessage(BaseUtil.getLangMsg("vampire.ancestorSucceedMsg"));
                }

            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 当一个玩家死亡时触发本事件
     * 吸血鬼转换人类
     *
     * @param event 事件
     */
    @EventHandler
    public void playerToWolf(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Player killer = player.getKiller();
        if (killer == null) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                // 判断是否为人类
                String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
                if (!RaceTypeEnum.MANKIND.getType().equals(raceType)) {
                    return;
                }

                // 判断击杀者是不是吸血鬼
                RacePlayer racePlayerDamage = RacePlayerService.getInstance().findByPlayerName(killer.getName());
                if (racePlayerDamage == null || !RaceTypeEnum.VAMPIRE.getType().equals(racePlayerDamage.getRaceType())) {
                    return;
                }

                // 设置玩家种族为吸血鬼-等级变低一级
                Boolean rst = RacePlayerService.getInstance().updateRaceType(player, player.getName(), RaceTypeEnum.VAMPIRE.getType(), racePlayerDamage.getRaceLevel() + 1);
                if (rst) {
                    player.sendMessage(BaseUtil.getLangMsg("werwolf.succeedMsg"));
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 吸血鬼攻击加成
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Player)) {
            return;
        }
        Player player = (Player) damager;

        // 判断是否为吸血鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
            return;
        }
        // 判断是否为吸血鬼
        RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
        if (racePlayer == null || !RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
            return;
        }

        // 伤害倍数
        double damageModifier = ConfigUtil.raceConfig.getDouble("vampire.damage");

        // 转换天数
        int differDay = RaceUtil.getDifferDay(racePlayer.getTransferTime());
        if (differDay > 0) {
            double transferTime = ConfigUtil.raceConfig.getDouble("vampire.transferTime" + differDay);
            if (transferTime > 0) {
                damageModifier = (int) Math.ceil(damageModifier * transferTime);
            }
        }
        player.sendMessage("修改前:" + event.getDamage());

        event.setDamage(event.getDamage() * damageModifier);

        player.sendMessage("修改后:" + event.getDamage());

        // 被伤害者
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            RaceConstants.VICTIM_ENTITY.put(player.getUniqueId(), (Player) entity);
        }
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 吸血鬼防御加成
     *
     * @param event 事件
     */
    @EventHandler
    public void onDefenseBonus(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        // 判断是否为吸血鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
            return;
        }

        // 造成伤害的玩家
        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            Player damagerPlayer = (Player) damager;
            ItemStack itemInHand = damagerPlayer.getItemInHand();

            String material = "WOOD_SWORD";
            if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_12.getVersionId()) {
                material = "WOODEN_SWORD";
            }
            if (Material.valueOf(material).equals(itemInHand.getType())) {
                // 被木剑伤害增加倍数
                double damageModifier = ConfigUtil.raceConfig.getDouble("vampire.woodenSwordDamageMultiplier");
                event.setDamage(event.getDamage() + damageModifier);
                return;
            }
        }

        // 判断是否为吸血鬼
        RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
        if (racePlayer == null || !RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
            return;
        }
        // 伤害减少倍数
        double damageModifier = ConfigUtil.raceConfig.getDouble("vampire.defenseBonus");

        // 转换天数
        int differDay = RaceUtil.getDifferDay(racePlayer.getTransferTime());
        if (differDay > 0) {
            double transferTime = ConfigUtil.raceConfig.getDouble("vampire.transferTime" + differDay);
            if (transferTime > 0) {
                damageModifier = (int) Math.ceil(damageModifier * transferTime);
            }
        }

        player.sendMessage("修改前:" + event.getDamage());
        event.setDamage(event.getDamage() - damageModifier);
        player.sendMessage("修改后:" + event.getDamage());
    }

    /**
     * 当任何一个实体死亡时触发本事件
     * 吸血鬼击杀玩家或者怪物恢复能量
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

        int amount = 0;
        if (livingEntity instanceof Player) {
            amount = ConfigUtil.raceConfig.getInt("vampire.killPlayer");
        }
        if (livingEntity instanceof Monster) {
            amount = ConfigUtil.raceConfig.getInt("vampire.killMonster");
        }
        if (livingEntity instanceof Animals) {
            amount = ConfigUtil.raceConfig.getInt("vampire.killAnimals");
        }
        RecoveryEnergyUtil.restoreEnergy(player, RaceTypeEnum.VAMPIRE, amount);
    }

    /**
     * 存储健康恢复事件的数据
     * 吸血鬼恢复生命双倍
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

        // 判断是否为吸血鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
            return;
        }

        player.sendMessage("修改前:" + event.getAmount());
        event.setAmount(event.getAmount() + ConfigUtil.raceConfig.getInt("vampire.regainHealth"));
        player.sendMessage("修改后:" + event.getAmount());
    }


    /**
     * 储存伤害事件的数据
     * 吸血鬼水下呼吸伤害减倍
     *
     * @param event 事件
     */
    @EventHandler
    public void onDrowningDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        // 判断伤害来源是否溺水
        if (!EntityDamageEvent.DamageCause.DROWNING.equals(event.getCause())) {
            return;
        }

        // 判断是否为吸血鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
            return;
        }

        player.sendMessage("修改前:" + event.getDamage());
        event.setDamage(event.getDamage() - ConfigUtil.raceConfig.getInt("vampire.drowning"));
        player.sendMessage("修改后:" + event.getDamage());
    }

    /**
     * 储存伤害事件的数据
     * 吸血鬼掉落无伤
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

        // 判断是否为吸血鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * 当玩家消耗完物品时, 此事件将触发 例如:(食物, 药水, 牛奶桶).
     * 吸血鬼只能吃生肉
     *
     * @param event 事件
     */
    @EventHandler
    public void consume(PlayerItemConsumeEvent event) {
        // 事件是否被取消
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();

        // 判断是否为吸血鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
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
        boolean rst = true;
        for (String materialStr : materials) {
            Material material = Material.valueOf(materialStr);
            if (itemStackType.equals(material)) {
                rst = false;
            }
        }
        if (rst) {
            event.setCancelled(true);
        }
    }

    /**
     * 当生物攻击或解除目标时调用
     * 吸血鬼和怪物休战
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!EntityTargetEvent.TargetReason.CLOSEST_PLAYER.equals(event.getReason())) {
            return;
        }
        Entity entity = event.getTarget();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        // 判断是否为吸血鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
            return;
        }
        event.setCancelled(true);
    }


    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 吸血鬼主动技能-吸血
     *
     * @param event 事件
     */
    @EventHandler
    public void onSummonWolf(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 判断是否为吸血鬼
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.VAMPIRE.getType().equals(raceType)) {
            return;
        }

        // 判断是否为红石粉
        ItemStack item = event.getItem();
        if (item == null || !Material.REDSTONE.equals(item.getType())) {
            return;
        }

        // 判断是否有敌人
        Player entity = RaceConstants.VICTIM_ENTITY.get(player.getUniqueId());
        if (entity == null || !player.canSee(entity)) {
            MessageApi.sendActionbar(player, BaseUtil.getLangMsg("vampire.noTarget"));
            return;
        }

        // 判断是否为吸血鬼
        RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
        if (racePlayer == null || !RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
            return;
        }

        int amount = ConfigUtil.raceConfig.getInt("vampire.hematophagia");
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

        // 进行吸血
        int hematophagiaNum = ConfigUtil.raceConfig.getInt("vampire.hematophagiaNum");

        // 目标剩余生命值
        double health = entity.getHealth() - hematophagiaNum;
        if (health > 0) {
            entity.setHealth(health);
            player.setHealth(player.getHealth() + hematophagiaNum);
        } else if (health == 0) {
            entity.setHealth(1);

            player.setHealth(player.getHealth() + hematophagiaNum);
        } else {
            entity.setHealth(1);
            player.setHealth(player.getHealth() + (hematophagiaNum - entity.getHealth()));
        }

        String hematophagiaSucceedMsg = BaseUtil.getLangMsg("vampire.hematophagiaSucceedMsg");
        hematophagiaSucceedMsg = hematophagiaSucceedMsg
                .replaceAll("\\$\\{".concat("player").concat("\\}"), entity.getName())
                .replaceAll("\\$\\{".concat("amount").concat("\\}"), hematophagiaNum + "");

        MessageApi.sendActionbar(player, BaseUtil.replaceChatColor(hematophagiaSucceedMsg));

        RaceConstants.VICTIM_ENTITY.remove(player.getUniqueId());
    }

}
