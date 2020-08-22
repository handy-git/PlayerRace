package com.handy.playerrace.constants;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/21 17:58
 */
public class RaceConstants {

    /**
     * 玩家种族缓存
     */
    public static Map<String, String> PLAYER_RACE = new HashMap();

    /**
     * 吸血鬼主动技能吸血缓存
     */
    public static Map<UUID, Player> VICTIM_ENTITY = new HashMap();

}
