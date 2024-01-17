package cn.handyplus.race.util;

import cn.handyplus.lib.core.DateUtil;
import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.MessageUtil;
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
        Optional<RacePlayer> racePlayerOptional = RacePlayerService.getInstance().findByPlayerUuid(player.getUniqueId());
        if (racePlayerOptional.isPresent()) {
            RacePlayer racePlayer = racePlayerOptional.get();
            racePlayer.setPlayerName(player.getName());
            // 升级处理
            levelUp(racePlayer);
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
     * @param playerUuid 玩家uid
     */
    public static void cache2Db(UUID playerUuid) {
        RacePlayer racePlayer = CacheUtil.PLAYER_RACE.get(playerUuid);
        // 升级处理
        levelUp(racePlayer);
        RacePlayerService.getInstance().update(racePlayer);
    }

    /**
     * 缓存到数据库
     */
    public static void cache2Db() {
        if (CacheUtil.PLAYER_RACE.isEmpty()) {
            return;
        }
        for (RacePlayer racePlayer : PLAYER_RACE.values()) {
            // 升级处理
            levelUp(racePlayer);
            RacePlayerService.getInstance().update(racePlayer);
        }
    }

    public static void removeCache(UUID playerUuid) {
        CacheUtil.PLAYER_RACE.remove(playerUuid);
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

    /**
     * 更新种族等级
     *
     * @param playerUuid 玩家uid
     * @param raceLevel  种族等级
     * @return true 成功
     */
    public static boolean addRaceLevel(UUID playerUuid, int raceLevel) {
        RacePlayer racePlayer = getRacePlayer(playerUuid);
        if (racePlayer == null) {
            return false;
        }
        racePlayer.setRaceLevel(racePlayer.getRaceLevel() + raceLevel);
        return true;
    }

    /**
     * 设置种族
     *
     * @param player       玩家
     * @param raceTypeEnum 种族类型
     * @return true 成功
     */
    public static boolean updateRaceType(Player player, RaceTypeEnum raceTypeEnum) {
        return updateRaceType(player, raceTypeEnum, 0);
    }

    /**
     * 设置种族
     *
     * @param playerUuid   玩家uid
     * @param raceTypeEnum 种族类型
     * @return true 成功
     */
    public static boolean updateRaceType(UUID playerUuid, RaceTypeEnum raceTypeEnum) {
        return updateRaceType(playerUuid, raceTypeEnum, 0);
    }

    /**
     * 设置种族
     *
     * @param player       玩家
     * @param raceTypeEnum 种族类型
     * @param raceLevel    种族等级
     * @return true 成功
     */
    public static boolean updateRaceType(Player player, RaceTypeEnum raceTypeEnum, int raceLevel) {
        boolean rst = updateRaceType(player.getUniqueId(), raceTypeEnum, raceLevel);
        if (rst) {
            player.getInventory().addItem(RaceUtil.getRaceHelpBook(raceTypeEnum));
            MessageUtil.sendMessage(player, RaceTypeEnum.getTip(raceTypeEnum));
        }
        return rst;
    }

    /**
     * 设置种族
     *
     * @param playerUuid   玩家uid
     * @param raceTypeEnum 种族类型
     * @param raceLevel    种族等级
     * @return true 成功
     */
    public static boolean updateRaceType(UUID playerUuid, RaceTypeEnum raceTypeEnum, int raceLevel) {
        RacePlayer racePlayer = getRacePlayer(playerUuid);
        if (racePlayer == null) {
            return false;
        }
        int maxAmount = 0;
        switch (raceTypeEnum) {
            case WER_WOLF:
                maxAmount = ConfigUtil.RACE_CONFIG.getInt("werwolf.maxAmount");
                break;
            case VAMPIRE:
                maxAmount = ConfigUtil.RACE_CONFIG.getInt("vampire.maxAmount");
                break;
            case DEMON:
                maxAmount = ConfigUtil.RACE_CONFIG.getInt("demon.maxAmount");
                break;
            case ANGEL:
                maxAmount = ConfigUtil.RACE_CONFIG.getInt("angel.maxAmount");
                break;
            case GHOUL:
                maxAmount = ConfigUtil.RACE_CONFIG.getInt("ghoul.maxAmount");
                break;
            case DEMON_HUNTER:
                maxAmount = ConfigUtil.RACE_CONFIG.getInt("demonHunter.maxAmount");
                break;
            default:
                break;
        }
        racePlayer.setRaceType(raceTypeEnum.getType());
        racePlayer.setRaceLevel(raceLevel);
        racePlayer.setAmount(maxAmount);
        racePlayer.setMaxAmount(maxAmount);
        racePlayer.setTransferTime(System.currentTimeMillis());
        // 变更种族异步立即刷到数据库
        HandySchedulerUtil.runTaskAsynchronously(() -> cache2Db(playerUuid));
        return true;
    }

    private static void levelUp(RacePlayer racePlayer) {
        // 升级需要时间
        int levelUpTime = ConfigUtil.RACE_CONFIG.getInt(racePlayer.getRaceType() + ".levelUpTime." + racePlayer.getRaceLevel() + 1, 0);
        if (levelUpTime == 0) {
            return;
        }
        // 当前转换时间
        int differDay = DateUtil.getDifferDay(racePlayer.getTransferTime());
        if (differDay >= levelUpTime) {
            racePlayer.setRaceLevel(racePlayer.getRaceLevel() + 1);
            int maxAmount = ConfigUtil.RACE_CONFIG.getInt(racePlayer.getRaceType() + ".maxAmount");
            double levelMaxAmount = ConfigUtil.RACE_CONFIG.getDouble(racePlayer.getRaceType() + ".levelMaxAmount." + racePlayer.getRaceLevel() + 1, 0.0);
            if (levelMaxAmount > 0) {
                racePlayer.setMaxAmount((int) (maxAmount * levelMaxAmount));
            }
        }
    }

}