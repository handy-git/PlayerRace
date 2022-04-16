package com.handy.playerrace.listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.handy.lib.annotation.HandyListener;
import com.handy.lib.api.MessageApi;
import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.core.CollUtil;
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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 恶魔猎手事件
 *
 * @author handy
 */
@HandyListener
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
                if (RaceUtil.isRaceType(RaceTypeEnum.MANKIND, player.getName())) {
                    return;
                }

                // 判断击杀者是不是人类
                RacePlayer racePlayer = RaceConstants.PLAYER_RACE.get(killer.getName());
                if (racePlayer == null || !RaceTypeEnum.MANKIND.getType().equals(racePlayer.getRaceType())) {
                    return;
                }

                // 击杀超过3人,进行转换
                if (racePlayer.getRaceLevel() >= 3) {
                    // 设置玩家种族为恶魔猎手
                    Boolean rst = RacePlayerService.getInstance().updateRaceType(killer.getName(), RaceTypeEnum.DEMON_HUNTER.getType(), 0);
                    if (rst) {
                        killer.getInventory().addItem(RaceUtil.getRaceHelpBook(RaceTypeEnum.DEMON_HUNTER));
                        killer.sendMessage(BaseUtil.getLangMsg("mankind.killsucceedMsg"));
                    }
                } else {
                    // 设置人类等级提升
                    Boolean rst = RacePlayerService.getInstance().updateRaceLevel(killer.getName(), racePlayer.getRaceLevel() + 1);
                    if (rst) {
                        killer.sendMessage(BaseUtil.getLangMsg("mankind.killMsg"));
                    }
                }
                RaceUtil.refreshCache(player);
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
        if (!RaceUtil.isRaceType(RaceTypeEnum.DEMON_HUNTER, player.getName())) {
            return;
        }

        // 判断弓类型
        DemonHunterBowTypeEnum demonHunterBowTypeEnum = RaceConstants.DEMON_HUNTER_BOW.get(player.getUniqueId());
        if (demonHunterBowTypeEnum == null) {
            return;
        }
        Entity entity = event.getEntity();

        // 判断是否为npc
        if (entity.hasMetadata("NPC")) {
            return;
        }

        switch (demonHunterBowTypeEnum) {
            case STRENGTH:
                event.setDamage(event.getDamage() + ConfigUtil.RACE_CONFIG.getInt("demonHunter.strengthDamage"));
                break;
            case FIRE:
                entity.setFireTicks(20 * ConfigUtil.RACE_CONFIG.getInt("demonHunter.fireTime"));
                break;
            case WEB:
                // 召唤蜘蛛网
                String web = "WEB";
                if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_12.getVersionId()) {
                    web = "COBWEB";
                }
                Location location = entity.getLocation();

                // 判断是否领地
                if (PlayerRace.getResidenceApi() != null) {
                    ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(location);
                    if (res != null) {
                        return;
                    }
                }
                // 如果是空气才能生成网
                if (Material.AIR.equals(location.getWorld().getBlockAt(location).getType())) {
                    location.getWorld().getBlockAt(location).setType(Material.valueOf(web));
                    RaceConstants.LOCATIONS.add(location);
                }
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
        if (!RaceUtil.isRaceType(RaceTypeEnum.DEMON_HUNTER, player.getName())) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                int amount = ConfigUtil.RACE_CONFIG.getInt("demonHunter.cutBow");
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
                cutBowMsg = cutBowMsg.replace("${amount}", amount + "").replace("${type}", cutBow.getTypeName());
                MessageApi.sendActionbar(player, BaseUtil.replaceChatColor(cutBowMsg));
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }


    /**
     * 当玩家消耗完物品时, 此事件将触发 例如:(食物, 药水, 牛奶桶).
     * 人类吃牛奶处理诅咒
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
        // 判断是否为人类
        if (!RaceUtil.isRaceType(RaceTypeEnum.MANKIND, player.getName())) {
            return;
        }
        // 判断是否牛奶
        Material milkBucket = Material.MILK_BUCKET;
        if (!event.getItem().getType().equals(milkBucket)) {
            return;
        }
        if (CollUtil.isNotEmpty(RaceConstants.PLAYER_CURSES)) {
            RaceConstants.PLAYER_CURSES.removeIf(playerCursesParam -> playerCursesParam.getPlayer().equals(player));
        }
    }

}
