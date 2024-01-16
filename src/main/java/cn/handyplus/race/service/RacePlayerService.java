package cn.handyplus.race.service;

import cn.handyplus.lib.db.Db;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.ConfigUtil;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * 玩家种族方法
 *
 * @author handy
 */
public class RacePlayerService {
    private RacePlayerService() {
    }

    private static final RacePlayerService INSTANCE = new RacePlayerService();

    public static RacePlayerService getInstance() {
        return INSTANCE;
    }

    /**
     * 新增
     *
     * @param racePlayer 数据
     * @return id
     */
    public int add(RacePlayer racePlayer) {
        return Db.use(RacePlayer.class).execution().insert(racePlayer);
    }

    /**
     * 根据playerUuid查询
     *
     * @param playerUuid 玩家uid
     * @return 种族
     */
    public Optional<RacePlayer> findByPlayer(UUID playerUuid) {
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerUuid, playerUuid);
        return use.execution().selectOne();
    }

    /**
     * 根据playerUuid查询种族
     *
     * @param playerUuid 玩家uid
     * @return 种族名
     */
    public String findRaceType(UUID playerUuid) {
        Optional<RacePlayer> racePlayerOptional = this.findByPlayer(playerUuid);
        return racePlayerOptional.isPresent() ? racePlayerOptional.get().getRaceType() : RaceTypeEnum.MANKIND.getType();
    }

    /**
     * 根据类型查询总数
     *
     * @param raceType 类型
     * @return 总数
     */
    public Integer findCount(String raceType) {
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getRaceType, raceType);
        return use.execution().count();
    }

    /**
     * 设置种族
     *
     * @param playerUuid 玩家uid
     * @param raceType   种族类型
     * @return true 成功ø
     */
    public Boolean updateRaceType(UUID playerUuid, String raceType) {
        return this.updateRaceType(playerUuid, raceType, 0);
    }

    /**
     * 设置种族
     *
     * @param playerUuid 玩家uid
     * @param raceType   种族类型
     * @param raceLevel  种族等级
     * @return true 成功
     */
    public Boolean updateRaceType(UUID playerUuid, String raceType, int raceLevel) {
        RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnum(raceType);
        if (raceTypeEnum == null) {
            return false;
        }
        int maxAmount = 0;
        switch (raceTypeEnum) {
            case WER_WOLF:
                maxAmount = ConfigUtil.RACE_CONFIG.getInt("werwolf.maxAmount");
                break;
            case VAMPIRE:
                if (raceLevel < 1) {
                    raceLevel = 1;
                }
                if (raceLevel > 10) {
                    raceLevel = 10;
                }
                maxAmount = ConfigUtil.RACE_CONFIG.getInt("vampire.maxAmount");
                maxAmount = (int) Math.ceil(maxAmount * ConfigUtil.RACE_CONFIG.getDouble("vampire.energyMultiple" + "." + raceLevel));
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
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerUuid, playerUuid);
        use.update().set(RacePlayer::getRaceType, raceType)
                .set(RacePlayer::getRaceType, raceType)
                .set(RacePlayer::getRaceLevel, raceLevel)
                .set(RacePlayer::getMaxAmount, maxAmount)
                .set(RacePlayer::getAmount, maxAmount)
                .set(RacePlayer::getTransferTime, System.currentTimeMillis());
        int rst = use.execution().update();
        if (rst > 0) {
            Optional<Player> playerOptional = BaseUtil.getOnlinePlayer(playerUuid);
            if (playerOptional.isPresent()) {
                Player player = playerOptional.get();
                String raceMsg = BaseUtil.getLangMsg("raceMsg");
                raceMsg = raceMsg.replace("${player}", player.getName()).replace("${race}", RaceTypeEnum.getTypeName(raceType));
                MessageUtil.sendAllMessage(raceMsg);
                CacheUtil.db2Cache(player);
            }
        }
        return rst > 0;
    }

    /**
     * 更新种族等级
     *
     * @param playerUuid 玩家uid
     * @param raceLevel  种族等级
     * @return true 成功
     */
    public boolean addRaceLevel(UUID playerUuid, int raceLevel) {
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerUuid, playerUuid);
        use.update().add(RacePlayer::getRaceLevel, RacePlayer::getRaceLevel, raceLevel);
        return use.execution().update() > 0;
    }

    /**
     * 更新名称
     *
     * @param player 玩家
     * @since 2.0.0
     */
    public void updatePlayerName(Player player) {
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerUuid, player.getUniqueId());
        use.update().set(RacePlayer::getPlayerName, player.getName());
        use.execution().update();
    }

    /**
     * 更新数据
     *
     * @param racePlayer 种族数据
     * @since 2.0.0
     */
    public void update(RacePlayer racePlayer) {
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.update().set(RacePlayer::getPlayerName, racePlayer.getPlayerName())
                .set(RacePlayer::getPlayerUuid, racePlayer.getPlayerUuid())
                .set(RacePlayer::getRaceType, racePlayer.getRaceType())
                .set(RacePlayer::getRaceLevel, racePlayer.getRaceLevel())
                .set(RacePlayer::getAmount, racePlayer.getAmount())
                .set(RacePlayer::getMaxAmount, racePlayer.getMaxAmount())
                .set(RacePlayer::getTransferTime, racePlayer.getTransferTime());
        use.execution().updateById(racePlayer.getId());
    }

}