package cn.handyplus.race.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.race.constants.AbstractRaceConstants;
import cn.handyplus.race.util.CacheUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家离开服务器事件
 *
 * @author handy
 */
@HandyListener
public class PlayerQuitEventListener implements Listener {

    /**
     * 玩家离开服务器事件.
     *
     * @param event 离开事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        clearCache(event.getPlayer());
    }

    /**
     * 玩家被服务器踢出事件.
     *
     * @param event 踢出事件
     */
    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        clearCache(event.getPlayer());
    }

    private void clearCache(Player player) {
        CacheUtil.cache2Db(player.getUniqueId());
        CacheUtil.removeCache(player.getUniqueId());
        AbstractRaceConstants.VICTIM_ENTITY.remove(player.getUniqueId());
        AbstractRaceConstants.DEMON_HUNTER_BOW.remove(player.getUniqueId());
    }

}
