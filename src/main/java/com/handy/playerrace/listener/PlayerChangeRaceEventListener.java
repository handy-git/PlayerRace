package com.handy.playerrace.listener;

import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.event.PlayerChangeRaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/22 13:21
 */
public class PlayerChangeRaceEventListener implements Listener {

    /**
     * 玩家种族变更事件
     *
     * @param event 事件
     */
    @EventHandler
    public void onPlayerChangeRace(PlayerChangeRaceEvent event) {
        PlayerRace.getInstance().getLogger().info("玩家变更种族事件: 玩家" + event.getPlayer() + "变更后的种族:" + event.getRaceTypeEnum());
    }

}
