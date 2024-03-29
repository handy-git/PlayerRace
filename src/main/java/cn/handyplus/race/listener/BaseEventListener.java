package cn.handyplus.race.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import cn.handyplus.race.util.RaceUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * @author handy
 */
@HandyListener
public class BaseEventListener implements Listener {

    /**
     * 当任何一个实体死亡时触发本事件
     * 掉落孟婆汤知识之书
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
        int anInt = ConfigUtil.RACE_CONFIG.getInt("mankind.dropRate");
        if (anInt == 0) {
            return;
        }
        int dropRate = new Random().nextInt(anInt);
        if (dropRate == 0) {
            event.getDrops().add(RaceUtil.getMengBorneoSoupBook());
            MessageUtil.sendMessage(player, BaseUtil.getLangMsg("mankind.bookSucceedMsg"));
        }
    }

    /**
     * 当玩家消耗完物品时, 此事件将触发 例如:(食物, 药水, 牛奶桶).
     * 超自然种族转换人类
     *
     * @param event 事件
     */
    @EventHandler
    public void mengBorneoSoup(PlayerItemConsumeEvent event) {
        // 事件是否被取消
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();
        if (!itemStack.isSimilar(RaceUtil.getMengBorneoSoup())) {
            return;
        }
        // 设置玩家种族为人类
        CacheUtil.updateRaceType(player, RaceTypeEnum.MANKIND);
        player.setHealth(0);
    }

}
