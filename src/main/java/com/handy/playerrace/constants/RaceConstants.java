package com.handy.playerrace.constants;

import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.param.PlayerCursesParam;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/21 17:58
 */
public abstract class RaceConstants {

    /**
     * 玩家种族缓存
     */
    public static Map<String, RacePlayer> PLAYER_RACE = new HashMap<>();

    /**
     * 吸血鬼主动技能吸血缓存
     */
    public static Map<UUID, Player> VICTIM_ENTITY = new HashMap<>();

    /**
     * 被诅咒的玩家
     */
    public static List<PlayerCursesParam> PLAYER_CURSES = new ArrayList<>();

    /**
     * 蜘蛛网地点
     */
    public static List<Location> LOCATIONS = new ArrayList<>();

    /**
     * 恶魔猎手的弓类型
     */
    public static Map<UUID, DemonHunterBowTypeEnum> DEMON_HUNTER_BOW = new HashMap<>();

    /**
     * 检查更新的版本url
     */
    public final static String CHECK_VERSION_URL = "https://gitee.com/api/v5/repos/handy-git/PlayerRace/releases/latest";

}