package cn.handyplus.race.constants;

import cn.handyplus.race.param.PlayerCursesParam;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 常量
 *
 * @author handy
 */
public abstract class AbstractRaceConstants {

    /**
     * 吸血鬼主动技能吸血缓存
     */
    public static final Map<UUID, Player> VICTIM_ENTITY = new ConcurrentHashMap<>();

    /**
     * 恶魔猎手的弓类型
     */
    public static final Map<UUID, DemonHunterBowTypeEnum> DEMON_HUNTER_BOW = new ConcurrentHashMap<>();

    /**
     * 被诅咒的玩家
     */
    public static final List<PlayerCursesParam> PLAYER_CURSES = new CopyOnWriteArrayList<>();

    /**
     * 蜘蛛网地点
     */
    public static final List<Location> LOCATIONS = new CopyOnWriteArrayList<>();

    /**
     * 检查更新的版本url
     */
    public final static String CHECK_VERSION_URL = "https://api.github.com/repos/handy-git/PlayerRace/releases/latest";

}