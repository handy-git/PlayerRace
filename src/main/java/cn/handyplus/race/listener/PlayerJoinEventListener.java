package cn.handyplus.race.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.HandyHttpUtil;
import cn.handyplus.race.constants.RaceConstants;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

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
        Player player = event.getPlayer();
        String playerName = player.getName();
        HandySchedulerUtil.runTaskAsynchronously(() -> {
            // 更新遗留的大小写问题
            RacePlayerService.getInstance().updatePlayerName(playerName);
            // 查询玩家种族
            Optional<RacePlayer> racePlayerOptional = RacePlayerService.getInstance().findByPlayerName(playerName);
            if (racePlayerOptional.isPresent()) {
                RaceConstants.PLAYER_RACE.put(playerName, racePlayerOptional.get());
                return;
            }
            RacePlayer racePlayer = new RacePlayer();
            racePlayer.setPlayerName(playerName);
            racePlayer.setPlayerUuid(player.getUniqueId().toString());
            racePlayer.setRaceType(RaceTypeEnum.MANKIND.getType());
            racePlayer.setAmount(0);
            racePlayer.setMaxAmount(0);
            racePlayer.setTransferTime(0L);
            racePlayer.setRaceLevel(1);
            RacePlayerService.getInstance().add(racePlayer);
            RaceConstants.PLAYER_RACE.put(playerName, racePlayer);
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
        HandyHttpUtil.checkVersion(event.getPlayer(), RaceConstants.CHECK_VERSION_URL);
    }

}
