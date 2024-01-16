package cn.handyplus.race.service;

import cn.handyplus.lib.db.Db;
import cn.handyplus.race.entity.RacePlayer;

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
    public Optional<RacePlayer> findByPlayerUuid(UUID playerUuid) {
        Db<RacePlayer> use = Db.use(RacePlayer.class);
        use.where().eq(RacePlayer::getPlayerUuid, playerUuid);
        return use.execution().selectOne();
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