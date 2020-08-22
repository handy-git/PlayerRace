package com.handy.playerrace.listener;

import com.handy.playerrace.constants.RaceConstants;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/22 12:06
 */
public class PlayerQuitEventListener implements Listener {

    /**
     * 玩家离开服务器事件.
     *
     * @param event
     */
    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        clearCache(event.getPlayer());
    }

    /**
     * 玩家被服务器踢出事件.
     *
     * @param event
     */
    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        clearCache(event.getPlayer());
    }

    private void clearCache(Player player) {
        RaceConstants.VICTIM_ENTITY.remove(player.getUniqueId());
        RaceConstants.PLAYER_RACE.remove(player.getName().toLowerCase());
    }
}
