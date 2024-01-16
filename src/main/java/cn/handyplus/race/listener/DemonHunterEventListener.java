package cn.handyplus.race.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.PlayerRace;
import cn.handyplus.race.constants.AbstractRaceConstants;
import cn.handyplus.race.constants.DemonHunterBowTypeEnum;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import cn.handyplus.race.util.RaceUtil;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
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
        HandySchedulerUtil.runTaskAsynchronously(() -> {
            // 判断被击杀者是否为为其他种族
            if (CacheUtil.isRaceType(RaceTypeEnum.MANKIND, player)) {
                return;
            }

            // 判断击杀者是不是人类
            RacePlayer racePlayer = CacheUtil.isRaceTypeAndGetRace(RaceTypeEnum.MANKIND, killer);
            if (racePlayer == null) {
                return;
            }

            // 击杀超过3人,进行转换
            if (racePlayer.getRaceLevel() != null && racePlayer.getRaceLevel() >= 3) {
                // 设置玩家种族为恶魔猎手
                Boolean rst = RacePlayerService.getInstance().updateRaceType(killer.getUniqueId(), RaceTypeEnum.DEMON_HUNTER.getType());
                if (rst) {
                    killer.getInventory().addItem(RaceUtil.getRaceHelpBook(RaceTypeEnum.DEMON_HUNTER));
                    killer.sendMessage(BaseUtil.getLangMsg("mankind.killsucceedMsg"));
                }
            } else {
                // 设置人类等级提升
                boolean rst = RacePlayerService.getInstance().addRaceLevel(killer.getUniqueId(), 1);
                if (rst) {
                    killer.sendMessage(BaseUtil.getLangMsg("mankind.killMsg"));
                }
            }
            CacheUtil.db2Cache(killer);
        });
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
        if (!CacheUtil.isRaceType(RaceTypeEnum.DEMON_HUNTER, player)) {
            return;
        }

        // 判断弓类型
        DemonHunterBowTypeEnum demonHunterBowTypeEnum = AbstractRaceConstants.DEMON_HUNTER_BOW.get(player.getUniqueId());
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
                    AbstractRaceConstants.LOCATIONS.add(location);
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
        if (!CacheUtil.isRaceType(RaceTypeEnum.DEMON_HUNTER, player)) {
            return;
        }

        HandySchedulerUtil.runTaskAsynchronously(() -> {
            int amount = ConfigUtil.RACE_CONFIG.getInt("demonHunter.cutBow");
            boolean rst = CacheUtil.subtract(player, amount);
            if (!rst) {
                MessageUtil.sendActionbar(player, RaceUtil.getEnergyShortageMsg(amount));
                return;
            }

            DemonHunterBowTypeEnum demonHunterBowTypeEnum = AbstractRaceConstants.DEMON_HUNTER_BOW.get(player.getUniqueId());

            DemonHunterBowTypeEnum cutBow = DemonHunterBowTypeEnum.STRENGTH;
            if (demonHunterBowTypeEnum == null) {
                AbstractRaceConstants.DEMON_HUNTER_BOW.put(player.getUniqueId(), cutBow);
            } else {
                switch (demonHunterBowTypeEnum) {
                    case STRENGTH:
                        AbstractRaceConstants.DEMON_HUNTER_BOW.put(player.getUniqueId(), DemonHunterBowTypeEnum.FIRE);
                        cutBow = DemonHunterBowTypeEnum.FIRE;
                        break;
                    case FIRE:
                        AbstractRaceConstants.DEMON_HUNTER_BOW.put(player.getUniqueId(), DemonHunterBowTypeEnum.WEB);
                        cutBow = DemonHunterBowTypeEnum.WEB;
                        break;
                    case WEB:
                        AbstractRaceConstants.DEMON_HUNTER_BOW.put(player.getUniqueId(), DemonHunterBowTypeEnum.STRENGTH);
                        cutBow = DemonHunterBowTypeEnum.STRENGTH;
                        break;
                    default:
                        break;
                }
            }

            String cutBowMsg = BaseUtil.getLangMsg("demonHunter.cutBowMsg");
            cutBowMsg = cutBowMsg.replace("${amount}", amount + "").replace("${type}", cutBow.getTypeName());
            MessageUtil.sendActionbar(player, BaseUtil.replaceChatColor(cutBowMsg));
        });
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
        if (!CacheUtil.isRaceType(RaceTypeEnum.MANKIND, player)) {
            return;
        }
        // 判断是否牛奶
        Material milkBucket = Material.MILK_BUCKET;
        if (!event.getItem().getType().equals(milkBucket)) {
            return;
        }
        if (CollUtil.isNotEmpty(AbstractRaceConstants.PLAYER_CURSES)) {
            AbstractRaceConstants.PLAYER_CURSES.removeIf(playerCursesParam -> playerCursesParam.getPlayer().equals(player));
        }
    }

}
