package cn.handyplus.race.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.ItemStackUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.PlayerRace;
import cn.handyplus.race.constants.AbstractRaceConstants;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import cn.handyplus.race.util.RaceUtil;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 吸血鬼相关事件
 *
 * @author handy
 */
@HandyListener
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
        int anInt = ConfigUtil.RACE_CONFIG.getInt("vampire.dropRate");
        if (anInt == 0) {
            return;
        }
        int dropRate = new Random().nextInt(anInt);
        if (dropRate == 0) {
            event.getDrops().add(RaceUtil.getKnowledgeBook());
            MessageUtil.sendMessage(player, BaseUtil.getLangMsg("vampire.bookSucceedMsg"));
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
        ItemStack itemStack = event.getItem();
        if (!itemStack.isSimilar(RaceUtil.getItemStack())) {
            return;
        }
        // 判断是否为人类或者吸血鬼
        RacePlayer racePlayer = CacheUtil.getRacePlayer(player.getUniqueId());
        if (racePlayer == null) {
            return;
        }
        if (!RaceTypeEnum.MANKIND.getType().equals(racePlayer.getRaceType()) && !RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
            return;
        }
        // 判断是否已经是吸血鬼了
        if (RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
            RaceUtil.restoreEnergy(player, RaceTypeEnum.VAMPIRE, ConfigUtil.RACE_CONFIG.getInt("vampire.cainBlood"));
            return;
        }
        // 判断是否为第一只吸血鬼
        Integer count = RacePlayerService.getInstance().findCount(RaceTypeEnum.VAMPIRE.getType());
        if (count != 0) {
            return;
        }
        // 设置玩家种族为吸血鬼始祖
        int cainBloodLevel = ConfigUtil.RACE_CONFIG.getInt(RaceTypeEnum.VAMPIRE.getType() + ".cainBloodLevel", 5);
        CacheUtil.updateRaceType(player, RaceTypeEnum.VAMPIRE, cainBloodLevel);
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

        // 判断是否为人类
        if (!CacheUtil.isRaceType(RaceTypeEnum.MANKIND, player)) {
            return;
        }

        // 判断击杀者是不是吸血鬼
        RacePlayer racePlayerDamage = CacheUtil.getRacePlayer(killer.getUniqueId());
        if (racePlayerDamage == null || !RaceTypeEnum.VAMPIRE.getType().equals(racePlayerDamage.getRaceType())) {
            return;
        }

        // 设置玩家种族为吸血鬼
        boolean rst = CacheUtil.updateRaceType(player.getUniqueId(), RaceTypeEnum.VAMPIRE);
        if (rst) {
            player.getInventory().addItem(RaceUtil.getRaceHelpBook(RaceTypeEnum.VAMPIRE));
            MessageUtil.sendMessage(player, BaseUtil.getLangMsg("vampire.succeedMsg"));
            MessageUtil.sendActionbar(player, BaseUtil.getLangMsg("vampire.succeedPlayerMsg").replace("${player}", player.getName()));
        }
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 吸血鬼攻击加成
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damage = event.getDamager();
        if (!(damage instanceof Player)) {
            return;
        }
        Player player = (Player) damage;

        // 判断是否为吸血鬼
        RacePlayer racePlayer = CacheUtil.isRaceTypeAndGetRace(RaceTypeEnum.VAMPIRE, player);
        if (racePlayer == null) {
            return;
        }

        // 伤害倍数
        double damageModifier = ConfigUtil.RACE_CONFIG.getDouble("vampire.damage");

        event.setDamage(event.getDamage() * damageModifier);

        // 被伤害者
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            AbstractRaceConstants.VICTIM_ENTITY.put(player.getUniqueId(), (Player) entity);
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
        RacePlayer racePlayer = CacheUtil.isRaceTypeAndGetRace(RaceTypeEnum.VAMPIRE, player);
        if (racePlayer == null) {
            return;
        }

        // 造成伤害的玩家
        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            Player damagerPlayer = (Player) damager;
            ItemStack itemInHand = ItemStackUtil.getItemInMainHand(damagerPlayer.getInventory());
            if (ItemStackUtil.getMaterial("WOODEN_SWORD").equals(itemInHand.getType())) {
                // 被木剑伤害增加倍数
                double damageModifier = ConfigUtil.RACE_CONFIG.getDouble("vampire.woodenSwordDamageMultiplier");
                event.setDamage(event.getDamage() + damageModifier);
                return;
            }
        }

        // 伤害减少倍数
        double damageModifier = ConfigUtil.RACE_CONFIG.getDouble("vampire.defenseBonus");

        event.setDamage(event.getDamage() - damageModifier);
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
            amount = ConfigUtil.RACE_CONFIG.getInt("vampire.killPlayer");
        }
        if (livingEntity instanceof Monster) {
            amount = ConfigUtil.RACE_CONFIG.getInt("vampire.killMonster");
        }
        if (livingEntity instanceof Animals) {
            amount = ConfigUtil.RACE_CONFIG.getInt("vampire.killAnimals");
        }
        RaceUtil.restoreEnergy(player, RaceTypeEnum.VAMPIRE, amount);
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
        if (!CacheUtil.isRaceType(RaceTypeEnum.VAMPIRE, player)) {
            return;
        }
        event.setAmount(event.getAmount() + ConfigUtil.RACE_CONFIG.getInt("vampire.regainHealth"));
    }


    /**
     * 储存伤害事件的数据
     * 吸血鬼水下呼吸伤害减少
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
        if (!CacheUtil.isRaceType(RaceTypeEnum.VAMPIRE, player)) {
            return;
        }
        event.setDamage(event.getDamage() - ConfigUtil.RACE_CONFIG.getInt("vampire.drowning"));
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
        if (!CacheUtil.isRaceType(RaceTypeEnum.VAMPIRE, player)) {
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
        if (!CacheUtil.isRaceType(RaceTypeEnum.VAMPIRE, player)) {
            return;
        }
        // 判断物品是否配置过可使用
        ItemStack itemStack = event.getItem();
        List<String> itemMaterialList = ConfigUtil.RACE_CONFIG.getStringList("vampire.itemMaterial");
        if (CollUtil.isNotEmpty(itemMaterialList)) {
            for (String itemMaterial : itemMaterialList) {
                if (itemStack.getType().name().equalsIgnoreCase(itemMaterial)) {
                    return;
                }
            }
        }
        // 判断不是孟婆汤
        if (itemStack.isSimilar(RaceUtil.getMengBorneoSoup())) {
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
            MessageUtil.sendActionbar(player, BaseUtil.getLangMsg("vampire.consumeMsg"));
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
        if (!CacheUtil.isRaceType(RaceTypeEnum.VAMPIRE, player)) {
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
        // 判断是否左击
        if (!Action.LEFT_CLICK_AIR.equals(event.getAction()) && !Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
            return;
        }

        Player player = event.getPlayer();

        // 判断是否为吸血鬼
        if (!CacheUtil.isRaceType(RaceTypeEnum.VAMPIRE, player)) {
            return;
        }

        // 判断是否为红石粉
        ItemStack item = event.getItem();
        if (item == null || !Material.REDSTONE.equals(item.getType())) {
            return;
        }

        // 判断是否有敌人
        Player entity = AbstractRaceConstants.VICTIM_ENTITY.get(player.getUniqueId());
        if (entity == null || !player.canSee(entity)) {
            MessageUtil.sendActionbar(player, BaseUtil.getLangMsg("vampire.noTarget"));
            return;
        }

        // 判断是否为npc
        if (entity.hasMetadata("NPC")) {
            return;
        }

        // 判断是否领地
        if (PlayerRace.RES_API != null) {
            ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(entity.getPlayer());
            if (res != null) {
                return;
            }
        }

        int amount = ConfigUtil.RACE_CONFIG.getInt("vampire.hematophagia");
        boolean rst = CacheUtil.subtract(player, amount);
        if (!rst) {
            MessageUtil.sendActionbar(player, RaceUtil.getEnergyShortageMsg(amount));
            return;
        }

        // 删除物品
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }

        // 进行吸血
        int hematophagiaNum = ConfigUtil.RACE_CONFIG.getInt("vampire.hematophagiaNum");

        // 目标剩余生命值
        double health = entity.getHealth() - hematophagiaNum;
        if (health > 0) {
            entity.setHealth(health);
        } else if (health == 0) {
            entity.setHealth(1);
        } else {
            entity.setHealth(1);
        }
        double healthValue = player.getHealth() + hematophagiaNum;

        player.setHealth(Math.min(healthValue, player.getMaxHealth()));

        AbstractRaceConstants.VICTIM_ENTITY.remove(player.getUniqueId());

        String hematophagiaSucceedMsg = BaseUtil.getLangMsg("vampire.hematophagiaSucceedMsg");
        hematophagiaSucceedMsg = hematophagiaSucceedMsg
                .replace("${amount}", amount + "")
                .replace("${player}", entity.getName())
                .replace("${health}", hematophagiaNum + "");

        MessageUtil.sendActionbar(player, BaseUtil.replaceChatColor(hematophagiaSucceedMsg));

        String hematophagiaPlayerSucceedMsg = BaseUtil.getLangMsg("vampire.hematophagiaPlayerSucceedMsg");
        hematophagiaPlayerSucceedMsg = hematophagiaPlayerSucceedMsg
                .replace("${amount}", hematophagiaNum + "")
                .replace("${player}", player.getName());

        MessageUtil.sendActionbar(entity, BaseUtil.replaceChatColor(hematophagiaPlayerSucceedMsg));
    }

}
