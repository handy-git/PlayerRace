package com.handy.playerrace.listener;

import com.handy.lib.api.MessageApi;
import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.DemonHunterBowTypeEnum;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.ConfigUtil;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author hs
 * @Description: {恶魔猎手事件}
 * @date 2020/8/27 11:01
 */
public class DemonHunterEventListener implements Listener {

    /**
     * 当一个玩家死亡时触发本事件
     * 人类转换恶魔猎手
     *
     * @param event 事件
     */
    @EventHandler
    public void playerToDemonHunter(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Player killer = player.getKiller();
        if (killer == null) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                // 判断被击杀者是否为为其他种族
                String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
                if (RaceTypeEnum.MANKIND.getType().equals(raceType)) {
                    return;
                }

                // 判断击杀者是不是人类
                RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(killer.getName());
                if (racePlayer == null || !RaceTypeEnum.MANKIND.getType().equals(racePlayer.getRaceType())) {
                    return;
                }

                // 击杀超过3人,进行转换
                if (racePlayer.getRaceLevel() >= 3) {
                    // 设置玩家种族为恶魔猎手
                    Boolean rst = RacePlayerService.getInstance().updateRaceType(player.getName(), RaceTypeEnum.DEMON_HUNTER.getType(), 0);
                    if (rst) {
                        killer.sendMessage(BaseUtil.getLangMsg("mankind.killsucceedMsg"));
                    }
                } else {
                    // 设置人类等级提升
                    Boolean rst = RacePlayerService.getInstance().updateRaceLevel(player.getName(), racePlayer.getRaceLevel() + 1);
                    if (rst) {
                        killer.sendMessage(BaseUtil.getLangMsg("mankind.killMsg"));
                    }
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 当一个实体受到另外一个实体伤害时触发该事件
     * 恶魔猎手弓
     *
     * @param event 事件
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
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

        if (player == null) {
            return;
        }

        // 判断是否为恶魔猎手
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.DEMON_HUNTER.getType().equals(raceType)) {
            return;
        }

        // 判断弓类型
        DemonHunterBowTypeEnum demonHunterBowTypeEnum = RaceConstants.DEMON_HUNTER_BOW.get(player.getUniqueId());
        if (demonHunterBowTypeEnum == null) {
            return;
        }
        Entity entity = event.getEntity();
        switch (demonHunterBowTypeEnum) {
            case STRENGTH:
                event.setDamage(event.getDamage() + ConfigUtil.raceConfig.getInt("demonHunter.strengthDamage"));
                break;
            case FIRE:
                entity.setFireTicks(20 * ConfigUtil.raceConfig.getInt("demonHunter.fireTime"));
                break;
            case WEB:
                // 召唤蜘蛛网
                String web = "WEB";
                if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_12.getVersionId()) {
                    web = "COBWEB";
                }
                Location location = entity.getLocation();
                location.getWorld().getBlockAt(location).setType(Material.valueOf(web));
                break;
            default:
                break;
        }
    }

    /**
     * 当玩家对一个对象或空气进行交互时触发本事件.
     * 恶魔猎手主动技能-切换弓
     *
     * @param event 事件
     */
    @EventHandler
    public void cutBow(PlayerInteractEvent event) {
        // 判断是否左击
        if (!Action.LEFT_CLICK_AIR.equals(event.getAction())) {
            return;
        }
        // 判断是否为弓
        ItemStack item = event.getItem();
        if (item == null || !Material.BOW.equals(item.getType())) {
            return;
        }
        Player player = event.getPlayer();

        // 判断是否为恶魔猎手
        String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
        if (!RaceTypeEnum.DEMON_HUNTER.getType().equals(raceType)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                int amount = ConfigUtil.raceConfig.getInt("demonHunter.cutBow");
                Boolean rst = RacePlayerService.getInstance().updateSubtract(player.getName(), amount);
                if (!rst) {
                    MessageApi.sendActionbar(player, RaceUtil.getEnergyShortageMsg(amount));
                    return;
                }

                DemonHunterBowTypeEnum demonHunterBowTypeEnum = RaceConstants.DEMON_HUNTER_BOW.get(player.getUniqueId());

                DemonHunterBowTypeEnum cutBow = DemonHunterBowTypeEnum.STRENGTH;
                if (demonHunterBowTypeEnum == null) {
                    RaceConstants.DEMON_HUNTER_BOW.put(player.getUniqueId(), cutBow);
                } else {
                    switch (demonHunterBowTypeEnum) {
                        case STRENGTH:
                            RaceConstants.DEMON_HUNTER_BOW.put(player.getUniqueId(), DemonHunterBowTypeEnum.FIRE);
                            cutBow = DemonHunterBowTypeEnum.FIRE;
                            break;
                        case FIRE:
                            RaceConstants.DEMON_HUNTER_BOW.put(player.getUniqueId(), DemonHunterBowTypeEnum.WEB);
                            cutBow = DemonHunterBowTypeEnum.WEB;
                            break;
                        case WEB:
                            RaceConstants.DEMON_HUNTER_BOW.put(player.getUniqueId(), DemonHunterBowTypeEnum.STRENGTH);
                            cutBow = DemonHunterBowTypeEnum.STRENGTH;
                            break;
                        default:
                            break;
                    }
                }

                String cutBowMsg = BaseUtil.getLangMsg("demonHunter.cutBowMsg");
                cutBowMsg = cutBowMsg
                        .replaceAll("\\$\\{".concat("amount").concat("\\}"), amount + "")
                        .replaceAll("\\$\\{".concat("type").concat("\\}"), cutBow.getTypeName());
                MessageApi.sendActionbar(player, BaseUtil.replaceChatColor(cutBowMsg));
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

}
