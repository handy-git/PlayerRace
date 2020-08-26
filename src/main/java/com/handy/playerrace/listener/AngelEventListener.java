package com.handy.playerrace.listener;

import com.handy.lib.api.MessageApi;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;
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

        // 判断伤害来源是否掉落
        EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
        if (entityDamageEvent == null || !EntityDamageEvent.DamageCause.FALL.equals(entityDamageEvent.getCause())) {
            return;
        }
        // 判断手上是否羽毛
        if (!Material.FEATHER.equals(player.getInventory().getItemInHand().getType())) {
            return;
        }

        // 判断是否为皮革装备
        PlayerInventory inventory = player.getInventory();
        ItemStack helmet = inventory.getHelmet();
        ItemStack chestplate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();

        if (helmet == null || chestplate == null || leggings == null || boots == null) {
            return;
        }

        if (!Material.LEATHER_HELMET.equals(helmet.getType()) || !Material.LEATHER_CHESTPLATE.equals(chestplate.getType())
                || !Material.LEATHER_LEGGINGS.equals(leggings.getType()) || !Material.LEATHER_BOOTS.equals(boots.getType())) {
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
                    player.sendMessage(BaseUtil.getLangMsg("angel.succeedMsg"));
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

        // 判断是否为npc
        if (entity.hasMetadata("NPC")) {
            return;
        }

        int amount = ConfigUtil.raceConfig.getInt("angel.diaup");
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
        event.setCancelled(true);
        // 判断是否是玩家
        if (entity instanceof Player) {
            Player entityPlayer = (Player) entity;
            entityPlayer.setVelocity(player.getLocation().getDirection().multiply(7));
            entityPlayer.setVelocity(new Vector(entityPlayer.getVelocity().getX(), 10.0D, entityPlayer.getVelocity().getZ()));
            entityPlayer.setFallDistance(-100.0F);
            // 发送提醒
            String diaupMsg = BaseUtil.getLangMsg("angel.diaupMsg");
            diaupMsg = diaupMsg.replaceAll("\\$\\{".concat("player").concat("\\}"), player.getName());
            MessageApi.sendActionbar(entityPlayer, BaseUtil.replaceChatColor(diaupMsg));

            String langMsg = BaseUtil.getLangMsg("angel.diaupPlayerMsg");
            langMsg = langMsg.replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "")
                    .replaceAll("\\$\\{".concat("player").concat("\\}"), entityPlayer.getName() + "");
            MessageApi.sendActionbar(player, langMsg);
        } else {
            entity.setVelocity(player.getLocation().getDirection().multiply(7));
            entity.setVelocity(new Vector(entity.getVelocity().getX(), 3.1D, entity.getVelocity().getZ()));

            String langMsg = BaseUtil.getLangMsg("angel.diaupOtherMsg");
            langMsg = langMsg.replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "");
            MessageApi.sendActionbar(player, langMsg);
        }
    }

    /**
     * 当玩家点击物品栏中的格子时触发事件事件..
     * 天使无法穿除了皮革以外的装备
     *
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player)) {
            return;
        }
        Player player = (Player) whoClicked;

        // 判断是否为天使
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.ANGEL.getType().equals(raceType)) {
            return;
        }

        InventoryAction action = event.getAction();
        // 判断是否移动物品
        if (!InventoryAction.PICKUP_ALL.equals(action) && !InventoryAction.SWAP_WITH_CURSOR.equals(action)) {
            return;
        }

        //返回点击的格子序号，可传递给Inventory.getItem(int)。
        int slot = event.getSlot();
        if (slot > 39 || slot < 36) {
            return;
        }

        // 获取被光标所拿起来的物品
        ItemStack cursor = event.getCursor();
        if (cursor == null || Material.AIR.equals(cursor.getType())) {
            return;
        }

        if (slot == 39 && !Material.LEATHER_HELMET.equals(cursor.getType())) {
            event.setCancelled(true);
            player.sendMessage(BaseUtil.getLangMsg("angel.wearEquipmentMsg"));
            return;
        }
        if (slot == 38 && !Material.LEATHER_CHESTPLATE.equals(cursor.getType())) {
            event.setCancelled(true);
            player.sendMessage(BaseUtil.getLangMsg("angel.wearEquipmentMsg"));
            return;
        }
        if (slot == 37 && !Material.LEATHER_LEGGINGS.equals(cursor.getType())) {
            event.setCancelled(true);
            player.sendMessage(BaseUtil.getLangMsg("angel.wearEquipmentMsg"));
            return;
        }
        if (slot == 36 && !Material.LEATHER_BOOTS.equals(cursor.getType())) {
            event.setCancelled(true);
            player.sendMessage(BaseUtil.getLangMsg("angel.wearEquipmentMsg"));
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
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.ANGEL.getType().equals(raceType)) {
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
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.ANGEL.getType().equals(raceType)) {
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
            Player player1 = (Player) entity;
            String raceType = RacePlayerService.getInstance().findRaceType(player1.getName());
            if (RaceTypeEnum.MANKIND.getType().equals(raceType)) {
                return;
            }
        }

        // 判断是否为天使
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.ANGEL.getType().equals(raceType)) {
            return;
        }
        event.setCancelled(true);
        MessageApi.sendActionbar(player, BaseUtil.getLangMsg("angel.notDamagePlayer"));
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
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.ANGEL.getType().equals(raceType)) {
            return;
        }

        // 判断是否为小麦,胡萝卜
        if (!Material.WHEAT.equals(item.getType()) && !Material.CARROT.equals(item.getType())) {
            return;
        }

        int amount = 0;
        if (Material.WHEAT.equals(item.getType())) {
            amount = ConfigUtil.raceConfig.getInt("angel.summonCow");
        }
        if (Material.CARROT.equals(item.getType())) {
            amount = ConfigUtil.raceConfig.getInt("angel.summonPig");
        }
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
        // 召唤牛
        if (Material.WHEAT.equals(item.getType())) {
            location.getWorld().spawnEntity(location, EntityType.COW);
            String langMsg = BaseUtil.getLangMsg("angel.summonCowMsg");
            langMsg = langMsg.replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "");
            MessageApi.sendActionbar(player, langMsg);
        }
        // 召唤猪
        if (Material.CARROT.equals(item.getType())) {
            location.getWorld().spawnEntity(location, EntityType.PIG);
            String langMsg = BaseUtil.getLangMsg("angel.summonPigMsg");
            langMsg = langMsg.replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "");
            MessageApi.sendActionbar(player, langMsg);
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
        ItemStack item = player.getItemInHand();
        if (!Material.EMERALD.equals(item.getType())) {
            return;
        }

        // 判断是否为天使
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.ANGEL.getType().equals(raceType)) {
            return;
        }

        int amount = ConfigUtil.raceConfig.getInt("angel.returnValue");
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
        int returnAmount = ConfigUtil.raceConfig.getInt("angel.returnAmount");
        RacePlayerService.getInstance().updateAdd(playerEntity.getName(), returnAmount);

        String returnValueMsg = BaseUtil.getLangMsg("angel.returnValueMsg");
        returnValueMsg = returnValueMsg
                .replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "")
                .replaceAll("\\$\\{".concat("player").concat("\\}"), playerEntity.getName() + "")
                .replaceAll("\\$\\{".concat("returnAmount").concat("\\}"), returnAmount + "");
        MessageApi.sendActionbar(player, BaseUtil.replaceChatColor(returnValueMsg));

        String playerReturnValueMsg = BaseUtil.getLangMsg("angel.playerReturnValueMsg");
        playerReturnValueMsg = playerReturnValueMsg
                .replaceAll("\\$\\{".concat("amount").concat("\\}"), returnAmount + "")
                .replaceAll("\\$\\{".concat("player").concat("\\}"), player.getName() + "");
        MessageApi.sendActionbar(playerEntity, BaseUtil.replaceChatColor(playerReturnValueMsg));
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
        ItemStack item = player.getItemInHand();
        if (!Material.BREAD.equals(item.getType())) {
            return;
        }

        // 判断是否为天使
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.ANGEL.getType().equals(raceType)) {
            return;
        }

        int amount = ConfigUtil.raceConfig.getInt("angel.returnHealth");
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
        int returnHealthAmount = ConfigUtil.raceConfig.getInt("angel.returnHealthAmount");
        RacePlayerService.getInstance().updateAdd(playerEntity.getName(), returnHealthAmount);

        String returnHealthMsg = BaseUtil.getLangMsg("angel.returnHealthMsg");
        returnHealthMsg = returnHealthMsg
                .replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "")
                .replaceAll("\\$\\{".concat("player").concat("\\}"), playerEntity.getName() + "")
                .replaceAll("\\$\\{".concat("returnAmount").concat("\\}"), returnHealthAmount + "");
        MessageApi.sendActionbar(player, BaseUtil.replaceChatColor(returnHealthMsg));

        String playerReturnHealthMsg = BaseUtil.getLangMsg("angel.playerReturnHealthMsg");
        playerReturnHealthMsg = playerReturnHealthMsg
                .replaceAll("\\$\\{".concat("amount").concat("\\}"), returnHealthAmount + "")
                .replaceAll("\\$\\{".concat("player").concat("\\}"), player.getName() + "");
        MessageApi.sendActionbar(playerEntity, BaseUtil.replaceChatColor(playerReturnHealthMsg));
    }

}
