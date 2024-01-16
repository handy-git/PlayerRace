package cn.handyplus.race.util;

import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.service.RacePlayerService;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存
 *
 * @author handy
 */
public class CacheUtil {

    /**
     * 玩家种族缓存
     */
    private final static Map<UUID, RacePlayer> PLAYER_RACE = new ConcurrentHashMap<>();


    /**
     * 判断是否对应种族类型
     *
     * @param raceTypeEnum 类型
     * @param player       玩家
     * @return RacePlayer
     */
    public static RacePlayer isRaceTypeAndGetRace(RaceTypeEnum raceTypeEnum, Player player) {
        RacePlayer racePlayer = CacheUtil.PLAYER_RACE.get(player.getUniqueId());
        if (racePlayer == null) {
            return null;
        }
        if (RaceUtil.isWorld(player)) {
            return null;
        }
        return raceTypeEnum.getType().equals(racePlayer.getRaceType()) ? racePlayer : null;
    }

    public static RacePlayer getRacePlayer(UUID playerUuid) {
        return CacheUtil.PLAYER_RACE.get(playerUuid);
    }

    /**
     * 判断是否对应种族类型
     *
     * @param raceTypeEnum 类型
     * @param player       玩家
     * @return true/是
     */
    public static boolean isRaceType(RaceTypeEnum raceTypeEnum, Player player) {
        return isRaceTypeAndGetRace(raceTypeEnum, player) != null;
    }

    /**
     * 数据库到缓存
     *
     * @param player 玩家
     */
    public static void db2Cache(Player player) {
        Optional<RacePlayer> racePlayerOptional = RacePlayerService.getInstance().findByPlayer(player.getUniqueId());
        if (racePlayerOptional.isPresent()) {
            RacePlayer racePlayer = racePlayerOptional.get();
            RacePlayerService.getInstance().updatePlayerName(player);
            racePlayer.setPlayerName(player.getName());
            CacheUtil.PLAYER_RACE.put(player.getUniqueId(), racePlayer);
            return;
        }
        RacePlayer racePlayer = new RacePlayer();
        racePlayer.setPlayerName(player.getName());
        racePlayer.setPlayerUuid(player.getUniqueId().toString());
        racePlayer.setRaceType(RaceTypeEnum.MANKIND.getType());
        racePlayer.setAmount(0);
        racePlayer.setMaxAmount(0);
        racePlayer.setTransferTime(0L);
        racePlayer.setRaceLevel(1);
        int id = RacePlayerService.getInstance().add(racePlayer);
        racePlayer.setId(id);
        CacheUtil.PLAYER_RACE.put(player.getUniqueId(), racePlayer);
    }

    /**
     * 缓存到数据库
     *
     * @param player 玩家
     */
    public static void cache2Db(Player player) {
        RacePlayer racePlayer = CacheUtil.PLAYER_RACE.get(player.getUniqueId());
        RacePlayerService.getInstance().update(racePlayer);
    }

    public static void removeCache(Player player) {
        CacheUtil.PLAYER_RACE.remove(player.getUniqueId());
    }

    public synchronized static boolean add(Player player, Integer amount) {
        RacePlayer racePlayer = getRacePlayer(player.getUniqueId());
        if (racePlayer == null) {
            return false;
        }
        if (racePlayer.getAmount() + amount > racePlayer.getMaxAmount()) {
            racePlayer.setAmount(racePlayer.getMaxAmount());
        } else {
            racePlayer.setAmount(racePlayer.getAmount() + amount);
        }
        return true;
    }

    public synchronized static boolean subtract(Player player, Integer amount) {
        RacePlayer racePlayer = getRacePlayer(player.getUniqueId());
        if (racePlayer == null) {
            return false;
        }
        if (racePlayer.getAmount() - amount < 0) {
            return false;
        }
        racePlayer.setAmount(racePlayer.getAmount() - amount);
        return true;
    }

}