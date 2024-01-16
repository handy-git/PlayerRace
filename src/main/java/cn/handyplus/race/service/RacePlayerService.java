package cn.handyplus.race.service;

import cn.handyplus.lib.db.Db;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceConstants;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.util.ConfigUtil;

import java.util.Optional;

/**
 * 种族方法
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
     */
    public void add(RacePlayer racePlayer) {
        Db.use(RacePlayer.class).execution().insert(racePlayer);
    }

    /**
     * 根据playerName查询
     *
     * @param playerName 玩家名
     * @return 种族
     */
    public Optional<RacePlayer> findByPlayerName(String playerName) {
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerName, playerName);
        return use.execution().selectOne();
    }

    /**
     * 根据playerName查询种族
     *
     * @param playerName 玩家名
     * @return 种族名
     */
    public String findRaceType(String playerName) {
        Optional<RacePlayer> racePlayerOptional = this.findByPlayerName(playerName);
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
     * 增加
     *
     * @param playerName 玩家名
     * @param amount     数量
     * @return true 成功
     */
    public synchronized boolean updateAdd(String playerName, Integer amount) {
        Optional<RacePlayer> racePlayerOptional = this.findByPlayerName(playerName);
        if (!racePlayerOptional.isPresent()) {
            return false;
        }
        RacePlayer racePlayer = racePlayerOptional.get();
        if (racePlayer.getAmount() + amount > racePlayer.getMaxAmount()) {
            return false;
        }
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerName, playerName);
        use.update().add(RacePlayer::getAmount, RacePlayer::getAmount, amount);
        return use.execution().update() > 0;
    }

    /**
     * 减少
     *
     * @param playerName 玩家名
     * @param amount     数量
     * @return true 成功
     */
    public synchronized boolean updateSubtract(String playerName, Integer amount) {
        Optional<RacePlayer> racePlayerOptional = this.findByPlayerName(playerName);
        if (!racePlayerOptional.isPresent()) {
            return false;
        }
        RacePlayer racePlayer = racePlayerOptional.get();
        if (racePlayer.getAmount() - amount < 0) {
            return false;
        }
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerName, playerName);
        use.update().subtract(RacePlayer::getAmount, RacePlayer::getAmount, amount);
        return use.execution().update() > 0;
    }

    /**
     * 设置种族
     *
     * @param playerName 玩家名
     * @param raceType   种族类型
     * @return true 成功ø
     */
    public Boolean updateRaceType(String playerName, String raceType) {
        return this.updateRaceType(playerName, raceType, 0);
    }

    /**
     * 设置种族
     *
     * @param playerName 玩家名
     * @param raceType   种族类型
     * @param raceLevel  种族等级
     * @return true 成功
     */
    public Boolean updateRaceType(String playerName, String raceType, int raceLevel) {
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
        use.where().eq(RacePlayer::getPlayerName, playerName);
        use.update().set(RacePlayer::getRaceType, raceType)
                .set(RacePlayer::getRaceType, raceType)
                .set(RacePlayer::getRaceLevel, raceLevel)
                .set(RacePlayer::getMaxAmount, maxAmount)
                .set(RacePlayer::getAmount, maxAmount)
                .set(RacePlayer::getTransferTime, System.currentTimeMillis());
        int rst = use.execution().update();
        if (rst > 0) {
            String raceMsg = BaseUtil.getLangMsg("raceMsg");
            raceMsg = raceMsg.replace("${player}", playerName).replace("${race}", RaceTypeEnum.getTypeName(raceType));
            MessageUtil.sendAllMessage(raceMsg);
            RaceConstants.PLAYER_RACE.put(playerName, this.findByPlayerName(playerName).orElse(null));
        }
        return rst > 0;
    }

    /**
     * 更新种族等级
     *
     * @param playerName 玩家名
     * @param raceLevel  种族等级
     * @return true 成功
     */
    public boolean addRaceLevel(String playerName, int raceLevel) {
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerName, playerName);
        use.update().add(RacePlayer::getRaceLevel, RacePlayer::getRaceLevel, raceLevel);
        return use.execution().update() > 0;
    }

    /**
     * 根据playerName改更新playerName
     *
     * @param playerName 玩家名
     * @since 1.2.8
     */
    public void updatePlayerName(String playerName) {
        if (playerName.equals(playerName.toLowerCase())) {
            return;
        }
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerName, playerName.toLowerCase());
        use.update().set(RacePlayer::getPlayerName, playerName);
        use.execution().update();
    }

}