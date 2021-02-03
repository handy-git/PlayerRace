package com.handy.playerrace.listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.handy.lib.api.MessageApi;
import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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

        // 判断伤害来源是否岩浆
        EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
        if (entityDamageEvent == null || !EntityDamageEvent.DamageCause.LAVA.equals(entityDamageEvent.getCause())) {
            return;
        }

        // 判断是否为锁链装备
        PlayerInventory inventory = player.getInventory();
        ItemStack helmet = inventory.getHelmet();
        ItemStack chestplate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();

        if (helmet == null || chestplate == null || leggings == null || boots == null) {
            return;
        }

        if (!Material.CHAINMAIL_HELMET.equals(helmet.getType()) || !Material.CHAINMAIL_CHESTPLATE.equals(chestplate.getType())
                || !Material.CHAINMAIL_LEGGINGS.equals(leggings.getType()) || !Material.CHAINMAIL_BOOTS.equals(boots.getType())) {
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
                // 设置玩家种族为恶魔
                Boolean rst = RacePlayerService.getInstance().updateRaceType(player.getName(), RaceTypeEnum.DEMON.getType(), 0);
                if (rst) {
                    player.getInventory().addItem(RaceUtil.getRaceHelpBook(RaceTypeEnum.DEMON));
                    player.sendMessage(BaseUtil.getLangMsg("demon.succeedMsg"));
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 储存伤害事件的数据
     * 恶魔免疫火焰
     *
     * @param event 事件
     */
    @EventHandler
    public void onFireDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        // 判断伤害来源是否火焰
        if (!EntityDamageEvent.DamageCause.FIRE.equals(event.getCause()) && !EntityDamageEvent.DamageCause.FIRE_TICK.equals(event.getCause())) {
            return;
        }

        // 判断是否为恶魔
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.DEMON.getType().equals(raceType)) {
            return;
        }
        event.setDamage(0);
    }

    /**
     * 储存伤害事件的数据
     * 恶魔岩浆中恢复血量和能量
     *
     * @param event 事件
     */
    @EventHandler
    public void onLavaDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        // 判断伤害来源是否岩浆
        if (!EntityDamageEvent.DamageCause.LAVA.equals(event.getCause())) {
            return;
        }

        // 判断是否为恶魔
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.DEMON.getType().equals(raceType)) {
            return;
        }
        event.setDamage(0);
        // 恢复能量
        RaceUtil.restoreEnergy(player, RaceTypeEnum.DEMON, ConfigUtil.raceConfig.getInt("demon.energyHealth"));

        // 恢复血量
        if (player.getHealth() == player.getMaxHealth()) {
            return;
        }
        double health = player.getHealth() + ConfigUtil.raceConfig.getInt("demon.restoreHealth");
        if (health > player.getMaxHealth()) {
            health = player.getMaxHealth();
        }
        player.setHealth(health);
    }

    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 恶魔主动技能-火焰弹
     *
     * @param event 事件
     */
    @EventHandler
    public void onSprint(PlayerInteractEvent event) {
        // 判断是否左击
        if (!Action.LEFT_CLICK_AIR.equals(event.getAction())) {
            return;
        }

        // 判断是否为火焰弹
        String material = "FIREBALL";
        if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_12.getVersionId()) {
            material = "FIRE_CHARGE";
        }
        ItemStack item = event.getItem();
        if (item == null || !Material.valueOf(material).equals(item.getType())) {
            return;
        }
        Player player = event.getPlayer();

        // 判断是否为恶魔
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.DEMON.getType().equals(raceType)) {
            return;
        }
        int amount = ConfigUtil.raceConfig.getInt("demon.fireBall");
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

        // 发射火焰弹
        Vector direction = player.getEyeLocation().getDirection().multiply(10);
        Projectile projectile = (Projectile) player.getWorld().spawn(player.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ()), Fireball.class);
        projectile.setShooter(player);
        projectile.setVelocity(direction);

        String langMsg = BaseUtil.getLangMsg("demon.fireBallMsg");
        langMsg = langMsg.replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "");
        MessageApi.sendActionbar(player, langMsg);
    }

    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 恶魔主动技能-生成蜘蛛网
     *
     * @param event 事件
     */
    @EventHandler
    public void web(PlayerInteractEvent event) {
        // 判断是否左击方块
        if (!Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
            return;
        }

        // 判断是否为墨囊
        String material = "INK_SACK";
        if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_12.getVersionId()) {
            material = "INK_SAC";
        }
        ItemStack item = event.getItem();
        if (item == null || !Material.valueOf(material).equals(item.getType())) {
            return;
        }
        Player player = event.getPlayer();

        // 判断是否为恶魔
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.DEMON.getType().equals(raceType)) {
            return;
        }

        // 判断点击的方块位置
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Location location = block.getLocation();

        // 判断是否领地
        if (PlayerRace.getResidenceApi() != null) {
            ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(location);
            if (res != null) {
                return;
            }
        }

        location.setY(location.getY() + 1);

        if (location.getY() >= 254.0D || !Material.AIR.equals(location.getBlock().getType())) {
            return;
        }
        int amount = ConfigUtil.raceConfig.getInt("demon.web");
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
        // 召唤蜘蛛网
        String web = "WEB";
        if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_12.getVersionId()) {
            web = "COBWEB";
        }

        block.getWorld().getBlockAt(location).setType(Material.valueOf(web));

        String langMsg = BaseUtil.getLangMsg("demon.webMsg");
        langMsg = langMsg.replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "");
        MessageApi.sendActionbar(player, langMsg);
    }

    /**
     * 当玩家点击物品栏中的格子时触发事件事件..
     * 恶魔无法穿除了锁链以外的装备
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

        // 判断是否为恶魔
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.DEMON.getType().equals(raceType)) {
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

        if (slot == 39 && !Material.CHAINMAIL_HELMET.equals(cursor.getType())) {
            event.setCancelled(true);
            player.sendMessage(BaseUtil.getLangMsg("demon.wearEquipmentMsg"));
            return;
        }
        if (slot == 38 && !Material.CHAINMAIL_CHESTPLATE.equals(cursor.getType())) {
            event.setCancelled(true);
            player.sendMessage(BaseUtil.getLangMsg("demon.wearEquipmentMsg"));
            return;
        }
        if (slot == 37 && !Material.CHAINMAIL_LEGGINGS.equals(cursor.getType())) {
            event.setCancelled(true);
            player.sendMessage(BaseUtil.getLangMsg("demon.wearEquipmentMsg"));
            return;
        }
        if (slot == 36 && !Material.CHAINMAIL_BOOTS.equals(cursor.getType())) {
            event.setCancelled(true);
            player.sendMessage(BaseUtil.getLangMsg("demon.wearEquipmentMsg"));
        }
    }

}
