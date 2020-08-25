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
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
        if (cursor == null) {
            return;
        }

        if (slot == 39 && !Material.LEATHER_HELMET.equals(cursor.getType())) {
            event.setCancelled(true);
            return;
        }
        if (slot == 38 && !Material.LEATHER_CHESTPLATE.equals(cursor.getType())) {
            event.setCancelled(true);
            return;
        }
        if (slot == 37 && !Material.LEATHER_LEGGINGS.equals(cursor.getType())) {
            event.setCancelled(true);
            return;
        }
        if (slot == 36 && !Material.LEATHER_BOOTS.equals(cursor.getType())) {
            event.setCancelled(true);
        }
    }

}
