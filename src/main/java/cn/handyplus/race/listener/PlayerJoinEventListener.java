package cn.handyplus.race.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.HandyHttpUtil;
import cn.handyplus.race.constants.AbstractRaceConstants;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 玩家加入游戏的事件
 *
 * @author handy
 */
@HandyListener
public class PlayerJoinEventListener implements Listener {

    /**
     * 玩家加入初始化种族
     *
     * @param event 进入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        HandySchedulerUtil.runTaskAsynchronously(() -> {
            CacheUtil.db2Cache(event.getPlayer());
        });
    }

    /**
     * op加入发送更新提醒 进入事件
     *
     * @param event 事件
     */
    @EventHandler
    public void onOpPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigUtil.CONFIG.getBoolean(BaseConstants.IS_CHECK_UPDATE_TO_OP_MSG)) {
            return;
        }
        HandyHttpUtil.checkVersion(event.getPlayer(), AbstractRaceConstants.CHECK_VERSION_URL);
    }

}
