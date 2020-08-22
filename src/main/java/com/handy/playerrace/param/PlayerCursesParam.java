package com.handy.playerrace.param;

import com.handy.playerrace.constants.RaceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/22 15:43
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
