package com.handy.playerrace.entity;

import lombok.Data;

/**
 * @author handy
 */
@Data
public class RacePlayer {
    private Long id;

    /**
     * 玩家名称
     */
    private String playerName;

    /**
     * 玩家uuid
     */
    private String playerUuid;

    /**
     * 种族类型
     */
    private String raceType;

    /**
     * 种族等级
     */
    private Integer raceLevel;

    /**
     * 玩家能量值
     */
    private Integer amount;

    /**
     * 玩家能量值上限
     */
    private Integer maxAmount;

    /**
     * 转换时间
     */
    private Long transferTime;
}
