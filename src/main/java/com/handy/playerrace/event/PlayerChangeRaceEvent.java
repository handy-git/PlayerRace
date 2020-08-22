package com.handy.playerrace.event;

import com.handy.playerrace.constants.RaceTypeEnum;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author hs
 * @Description: {玩家种族变更事件}
 * @date 2020/8/22 13:03
 */
public class PlayerChangeRaceEvent extends Event {
    /**
     * 有可能为空(使用指令的情况下为空)
     */
    private final Player player;
    /**
     * 必填
     */
    private final String playerName;
    /**
     * 必填
     */
    private final RaceTypeEnum raceTypeEnum;

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PlayerChangeRaceEvent(Player player, String playerName, RaceTypeEnum raceTypeEnum) {
        this.player = player;
        this.playerName = playerName;
        this.raceTypeEnum = raceTypeEnum;
    }

    public Player getPlayer() {
        return player;
    }

    public RaceTypeEnum getRaceTypeEnum() {
        return raceTypeEnum;
    }

    public String getPlayerName() {
        return playerName;
    }
}
