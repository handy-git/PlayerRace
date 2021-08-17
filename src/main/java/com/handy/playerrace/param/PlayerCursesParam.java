package com.handy.playerrace.param;

import com.handy.playerrace.constants.RaceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;

/**
 * @author handy
 */
@Data
@AllArgsConstructor
public class PlayerCursesParam {
    /**
     * 玩家
     */
    private Player player;

    /**
     * 玩家名
     */
    private String playerName;

    /**
     * 添加时间
     */
    private long addTime;

    /**
     * 种族
     */
    private RaceTypeEnum raceTypeEnum;
}
