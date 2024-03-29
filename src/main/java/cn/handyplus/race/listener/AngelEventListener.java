package cn.handyplus.race.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.ItemStackUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import cn.handyplus.race.util.RaceUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

/**
 * 天使相关事件
 *
 * @author handy
 */
@HandyListener
public class AngelEventListener implements Listener {

    /**
     * 当任何一个实体死亡时触发本事件
     * 玩家转换天使事件
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player) livingEntity;

        // 判断伤害来源是否掉落
        EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
        if (entityDamageEvent == null || !EntityDamageEvent.DamageCause.FALL.equals(entityDamageEvent.getCause())) {
            return;
        }
        // 判断手上是否羽毛
        if (!Material.FEATHER.equals(ItemStackUtil.getItemInMainHand(player.getInventory()).getType())) {
            return;
        }

        // 判断是否为皮革装备
        PlayerInventory inventory = player.getInventory();
        ItemStack helmet = inventory.getHelmet();
        ItemStack chestPlate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();
        if (helmet == null || chestPlate == null || leggings == null || boots == null) {
            return;
        }
        if (!Material.LEATHER_HELMET.equals(helmet.getType()) || !Material.LEATHER_CHESTPLATE.equals(chestPlate.getType()) || !Material.LEATHER_LEGGINGS.equals(leggings.getType()) || !Material.LEATHER_BOOTS.equals(boots.getType())) {
            return;
        }
        // 判断玩家是否是人类
        if (!CacheUtil.isRaceType(RaceTypeEnum.MANKIND, player)) {
            return;
        }
        // 设置玩家种族为天使
        CacheUtil.updateRaceType(player, RaceTypeEnum.ANGEL);
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 天使主动技能: 上天
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damage = event.getDamager();
        Entity entity = event.getEntity();

        // 判断是否近战
        if (!(damage instanceof Player)) {
            return;
        }

        Player player = (Player) damage;

        // 判断是否拿的羽毛
        ItemStack item = ItemStackUtil.getItemInMainHand(player.getInventory());
        if (!Material.FEATHER.equals(item.getType())) {
            return;
        }

        // 判断玩家是否为天使
        if (!CacheUtil.isRaceType(RaceTypeEnum.ANGEL, player)) {
            return;
        }

        // 判断是否为npc
        if (entity.hasMetadata("NPC")) {
            return;
        }

        int amount = ConfigUtil.RACE_CONFIG.getInt("angel.diaup");
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
        event.setCancelled(true);
        // 判断是否是玩家
        if (entity instanceof Player) {
            Player entityPlayer = (Player) entity;
            entityPlayer.setVelocity(player.getLocation().getDirection().multiply(7));
            entityPlayer.setVelocity(new Vector(entityPlayer.getVelocity().getX(), 10.0D, entityPlayer.getVelocity().getZ()));
            entityPlayer.setFallDistance(-100.0F);
            // 发送提醒
            String diaupMsg = BaseUtil.getLangMsg("angel.diaupMsg");
            diaupMsg = diaupMsg.replace("${player}", player.getName());
            MessageUtil.sendActionbar(entityPlayer, BaseUtil.replaceChatColor(diaupMsg));

            String langMsg = BaseUtil.getLangMsg("angel.diaupPlayerMsg");
            langMsg = langMsg.replace("${amount}", amount + "").replace("${player}", entityPlayer.getName());
            MessageUtil.sendActionbar(player, langMsg);
        } else {
            entity.setVelocity(player.getLocation().getDirection().multiply(7));
            entity.setVelocity(new Vector(entity.getVelocity().getX(), 3.1D, entity.getVelocity().getZ()));

            String langMsg = BaseUtil.getLangMsg("angel.diaupOtherMsg");
            langMsg = langMsg.replace("${amount}", amount + "");
            MessageUtil.sendActionbar(player, langMsg);
        }
    }

    /**
     * 储存伤害事件的数据
     * 天使掉落无伤
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

        // 判断是否为天使
        if (!CacheUtil.isRaceType(RaceTypeEnum.ANGEL, player)) {
            return;
        }
        event.setCancelled(true);
    }


    /**
     * 储存伤害事件的数据
     * 天使水下无伤
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

        // 判断是否为天使
        if (!CacheUtil.isRaceType(RaceTypeEnum.ANGEL, player)) {
            return;
        }
        event.setDamage(0);
    }


    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 天使无法伤害人类
     *
     * @param event 事件
     */
    @EventHandler
    public void notDamagePlayer(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        Player player = null;
        // 判断是否远程
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                player = (Player) shooter;
            }
        }
        // 判断是否近战
        if ((damager instanceof Player)) {
            player = (Player) damager;
        }
        if (player == null) {
            return;
        }

        // 被伤害者
        Entity entity = event.getEntity();
        // 判断是否为npc
        if (entity.hasMetadata("NPC")) {
            return;
        }

        // 判断是否为动物和玩家
        if (!(entity instanceof Animals) && !(entity instanceof Player)) {
            return;
        }

        // 判断是否为人类
        if (entity instanceof Player) {
            // 判断是否为人类
            if (!CacheUtil.isRaceType(RaceTypeEnum.MANKIND, (Player) entity)) {
                return;
            }
            // 判断是否拿的面包和绿宝石
            ItemStack item = ItemStackUtil.getItemInMainHand(player.getInventory());
            if (Material.BREAD.equals(item.getType()) || Material.EMERALD.equals(item.getType())) {
                return;
            }
        }

        // 判断是否为天使
        if (!CacheUtil.isRaceType(RaceTypeEnum.ANGEL, player)) {
            return;
        }
        event.setCancelled(true);
        MessageUtil.sendActionbar(player, BaseUtil.getLangMsg("angel.notDamagePlayer"));
    }

    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 天使主动技能-召唤动物
     *
     * @param event 事件
     */
    @EventHandler
    public void onSummonWolf(PlayerInteractEvent event) {
        // 判断是否左击
        if (!Action.LEFT_CLICK_AIR.equals(event.getAction()) && !Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || Material.AIR.equals(item.getType())) {
            return;
        }

        Player player = event.getPlayer();

        // 判断是否为天使
        if (!CacheUtil.isRaceType(RaceTypeEnum.ANGEL, player)) {
            return;
        }

        // 胡萝卜
        String materialStr = "CARROT";
        if (VersionCheckEnum.getEnum().getVersionId() < VersionCheckEnum.V_1_13.getVersionId()) {
            materialStr = "CARROT_ITEM";
        }
        Material carrotMaterial = ItemStackUtil.getMaterial(materialStr);

        Material material = item.getType();
        if (!Material.WHEAT.equals(material) && !carrotMaterial.equals(material)) {
            return;
        }

        int amount = 0;
        if (Material.WHEAT.equals(material)) {
            amount = ConfigUtil.RACE_CONFIG.getInt("angel.summonCow");
        }
        if (carrotMaterial.equals(material)) {
            amount = ConfigUtil.RACE_CONFIG.getInt("angel.summonPig");
        }
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

        Location location = player.getLocation();
        // 召唤牛
        if (Material.WHEAT.equals(item.getType())) {
            location.getWorld().spawnEntity(location, EntityType.COW);
            String langMsg = BaseUtil.getLangMsg("angel.summonCowMsg");
            langMsg = langMsg.replace("${amount}", amount + "");
            MessageUtil.sendActionbar(player, langMsg);
        }
        // 召唤猪
        if (carrotMaterial.equals(item.getType())) {
            location.getWorld().spawnEntity(location, EntityType.PIG);
            String langMsg = BaseUtil.getLangMsg("angel.summonPigMsg");
            langMsg = langMsg.replace("${amount}", amount + "");
            MessageUtil.sendActionbar(player, langMsg);
        }
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 天使主动技能: 恢复能量
     *
     * @param event 事件
     */
    @EventHandler
    public void returnValue(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        // 判断是否近战
        if (!(damager instanceof Player)) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        // 判断是否为npc
        if (entity.hasMetadata("NPC")) {
            return;
        }

        Player player = (Player) damager;
        Player playerEntity = (Player) entity;

        // 判断是否拿的绿宝石
        ItemStack item = ItemStackUtil.getItemInMainHand(player.getInventory());
        if (!Material.EMERALD.equals(item.getType())) {
            return;
        }

        // 判断是否为天使
        if (!CacheUtil.isRaceType(RaceTypeEnum.ANGEL, player)) {
            return;
        }

        event.setCancelled(true);

        int amount = ConfigUtil.RACE_CONFIG.getInt("angel.returnValue");
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

        int returnAmount = ConfigUtil.RACE_CONFIG.getInt("angel.returnAmount");
        CacheUtil.add(playerEntity, returnAmount);

        String returnValueMsg = BaseUtil.getLangMsg("angel.returnValueMsg");
        returnValueMsg = returnValueMsg.replace("${amount}", amount + "").replace("${player}", playerEntity.getName()).replace("${returnAmount", returnAmount + "");
        MessageUtil.sendActionbar(player, BaseUtil.replaceChatColor(returnValueMsg));

        String playerReturnValueMsg = BaseUtil.getLangMsg("angel.playerReturnValueMsg");
        playerReturnValueMsg = playerReturnValueMsg.replace("${amount}", returnAmount + "").replace("${player}", player.getName());
        MessageUtil.sendActionbar(playerEntity, BaseUtil.replaceChatColor(playerReturnValueMsg));
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 天使主动技能: 恢复生命
     *
     * @param event 事件
     */
    @EventHandler
    public void returnHealth(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        // 判断是否近战
        if (!(damager instanceof Player)) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        // 判断是否为npc
        if (entity.hasMetadata("NPC")) {
            return;
        }

        Player player = (Player) damager;
        Player playerEntity = (Player) entity;

        // 判断是否拿的面包
        ItemStack item = ItemStackUtil.getItemInMainHand(player.getInventory());
        if (!Material.BREAD.equals(item.getType())) {
            return;
        }

        // 判断是否为天使
        if (!CacheUtil.isRaceType(RaceTypeEnum.ANGEL, player)) {
            return;
        }

        event.setCancelled(true);

        int amount = ConfigUtil.RACE_CONFIG.getInt("angel.returnHealth");
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

        int returnHealthAmount = ConfigUtil.RACE_CONFIG.getInt("angel.returnHealthAmount");
        double health = playerEntity.getHealth();
        double newHealTh = health + returnHealthAmount;
        if (newHealTh > playerEntity.getMaxHealth()) {
            newHealTh = playerEntity.getMaxHealth();
        }
        playerEntity.setHealth(newHealTh);

        String returnHealthMsg = BaseUtil.getLangMsg("angel.returnHealthMsg");
        returnHealthMsg = returnHealthMsg.replace("${amount}", amount + "").replace("${player}", playerEntity.getName()).replace("${returnAmount}", returnHealthAmount + "");
        MessageUtil.sendActionbar(player, BaseUtil.replaceChatColor(returnHealthMsg));

        String playerReturnHealthMsg = BaseUtil.getLangMsg("angel.playerReturnHealthMsg");
        playerReturnHealthMsg = playerReturnHealthMsg.replace("${amount}", returnHealthAmount + "").replace("${player}", player.getName());
        MessageUtil.sendActionbar(playerEntity, BaseUtil.replaceChatColor(playerReturnHealthMsg));
    }

}
