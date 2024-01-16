package cn.handyplus.race.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.ItemStackUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceConstants;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.param.PlayerCursesParam;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.ConfigUtil;
import cn.handyplus.race.util.RaceUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 食尸鬼事件
 *
 * @author handy
 */
@HandyListener
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
        HandySchedulerUtil.runTaskAsynchronously(() -> {
            // 判断玩家是否有种族
            if (!RaceUtil.isRaceType(RaceTypeEnum.MANKIND, player)) {
                return;
            }
            // 设置玩家种族为食尸鬼
            Boolean rst = RacePlayerService.getInstance().updateRaceType(player.getName(), RaceTypeEnum.GHOUL.getType());
            if (rst) {
                player.getInventory().addItem(RaceUtil.getRaceHelpBook(RaceTypeEnum.GHOUL));
                player.sendMessage(BaseUtil.getLangMsg("ghoul.succeedMsg"));
                RaceUtil.refreshCache(player);
            }
        });
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
        if (!RaceUtil.isRaceType(RaceTypeEnum.GHOUL, player)) {
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
        if (!RaceUtil.isRaceType(RaceTypeEnum.GHOUL, player)) {
            return;
        }


        HandySchedulerUtil.runTaskAsynchronously(() -> {
            // 扣除能量回血
            double finalDamage = event.getFinalDamage();
            boolean rst = RacePlayerService.getInstance().updateSubtract(player.getName(), (int) finalDamage);
            if (!rst) {
                return;
            }

            double health = player.getHealth() + event.getFinalDamage();
            if (health < 0) {
                return;
            }
            if (health > player.getMaxHealth()) {
                health = player.getMaxHealth();
            }
            player.setHealth(health);

            String langMsg = BaseUtil.getLangMsg("ghoul.damageMsg");
            langMsg = langMsg.replace("${amount}", (int) finalDamage + "")
                    .replace("${health}", (int) finalDamage + "");
            MessageUtil.sendActionbar(player, langMsg);

            // 如果是玩家还要扣除能量值
            Entity entity = event.getEntity();
            if (!(entity instanceof Player)) {
                return;
            }
            Player entityPlayer = (Player) entity;
            int amount = ConfigUtil.RACE_CONFIG.getInt("ghoul.absorptionValue");
            RacePlayerService.getInstance().updateSubtract(entityPlayer.getName(), amount);

            String absorptionMsg = BaseUtil.getLangMsg("ghoul.absorptionMsg");
            absorptionMsg = absorptionMsg.replace("${amount}", amount + "");
            MessageUtil.sendActionbar(entityPlayer, absorptionMsg);
        });
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
        if (!RaceUtil.isRaceType(RaceTypeEnum.GHOUL, player)) {
            return;
        }

        int amount = ConfigUtil.RACE_CONFIG.getInt("ghoul.summonPigZombie");
        boolean rst = RacePlayerService.getInstance().updateSubtract(player.getName(), amount);
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

        // 获取猪人
        String entityType = "PIG_ZOMBIE";
        if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_15.getVersionId()) {
            entityType = "ZOMBIFIED_PIGLIN";
        }
        // 召唤猪人
        Location location = player.getLocation();
        location.getWorld().spawnEntity(location, EntityType.valueOf(entityType));

        String langMsg = BaseUtil.getLangMsg("ghoul.summonPigZombieMsg");
        langMsg = langMsg.replace("${amount}", amount + "");
        MessageUtil.sendActionbar(player, langMsg);
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
        // 判断是否为npc
        if (entity.hasMetadata("NPC")) {
            return;
        }

        Player player = (Player) damager;
        Player playerEntity = (Player) entity;

        // 判断是否拿的骨头
        ItemStack item = ItemStackUtil.getItemInMainHand(player.getInventory());
        if (!Material.BONE.equals(item.getType())) {
            return;
        }

        // 判断是否为食尸鬼
        if (!RaceUtil.isRaceType(RaceTypeEnum.GHOUL, player)) {
            return;
        }

        // 判断被转换的玩家是否是人类
        if (!RaceUtil.isRaceType(RaceTypeEnum.MANKIND, playerEntity)) {
            return;
        }

        int amount = ConfigUtil.RACE_CONFIG.getInt("ghoul.curse");
        boolean rst = RacePlayerService.getInstance().updateSubtract(player.getName(), amount);
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

        RaceConstants.PLAYER_CURSES.add(new PlayerCursesParam(playerEntity, playerEntity.getName(), System.currentTimeMillis(), RaceTypeEnum.GHOUL));

        int curseSecond = ConfigUtil.RACE_CONFIG.getInt("ghoul.curseSecond");
        String diaupMsg = BaseUtil.getLangMsg("ghoul.curseMsg");
        diaupMsg = diaupMsg.replace("${time}", curseSecond + "");
        MessageUtil.sendActionbar(playerEntity, BaseUtil.replaceChatColor(diaupMsg));

        String langMsg = BaseUtil.getLangMsg("ghoul.cursePlayerMsg");
        langMsg = langMsg.replace("${amount}", amount + "")
                .replace("${player}", playerEntity.getName());
        MessageUtil.sendActionbar(player, langMsg);
    }

}