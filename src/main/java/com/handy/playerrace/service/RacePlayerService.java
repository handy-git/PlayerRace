package com.handy.playerrace.service;


import com.handy.lib.api.MessageApi;
import com.handy.lib.api.StorageApi;
import com.handy.lib.util.BaseUtil;
import com.handy.lib.util.SqlManagerUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.constants.sql.RacePlayerSqlEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.util.ConfigUtil;
import lombok.val;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author hs
 * @Description: {}
 * @date 2020/7/13 14:30
 */
public class RacePlayerService {
    private RacePlayerService() {
    }

    private static class SingletonHolder {
        private static final RacePlayerService INSTANCE = new RacePlayerService();
    }

    public static RacePlayerService getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 建表
     *
     * @return
     */
    public Boolean create() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            String sql = RacePlayerSqlEnum.CREATE_SQ_LITE.getCommand();
            if ("MySQL".equals(StorageApi.storageConfig.getString("storage-method"))) {
                sql = RacePlayerSqlEnum.CREATE_MYSQL.getCommand();
            }
            ps = conn.prepareStatement(sql);
            val rst = ps.executeUpdate();
            return rst > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        return false;
    }

    /**
     * 新增
     *
     * @param racePlayer 数据
     * @return
     */
    public Boolean add(RacePlayer racePlayer) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String addStr = RacePlayerSqlEnum.ADD_DATA.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(addStr);
            ps.setString(1, racePlayer.getPlayerName().toLowerCase());
            ps.setString(2, racePlayer.getPlayerUuid());
            ps.setString(3, racePlayer.getRaceType());
            ps.setInt(4, racePlayer.getRaceLevel() != null ? racePlayer.getRaceLevel() : 0);
            ps.setInt(5, racePlayer.getAmount());
            ps.setInt(6, racePlayer.getMaxAmount() != null ? racePlayer.getMaxAmount() : 0);
            ps.setLong(7, racePlayer.getTransferTime() != null ? racePlayer.getTransferTime() : 0L);
            val rst = ps.executeUpdate();
            return rst > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        return false;
    }

    /**
     * 根据playerName查询
     *
     * @param playerName
     * @return
     */
    public RacePlayer findByPlayerName(String playerName) {
        playerName = BaseUtil.toLowerCase(playerName);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rst = null;
        RacePlayer racePlayer = null;
        try {
            String selectStr = RacePlayerSqlEnum.SELECT_BY_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setString(1, playerName);
            rst = ps.executeQuery();
            while (rst.next()) {
                racePlayer = new RacePlayer();
                racePlayer.setId(rst.getLong(1));
                racePlayer.setPlayerName(rst.getString(2));
                racePlayer.setPlayerUuid(rst.getString(3));
                racePlayer.setRaceType(rst.getString(4));
                racePlayer.setRaceLevel(rst.getInt(5));
                racePlayer.setAmount(rst.getInt(6));
                racePlayer.setMaxAmount(rst.getInt(7));
                racePlayer.setTransferTime(rst.getLong(8));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, rst);
        }
        return racePlayer;
    }

    /**
     * 根据playerName查询种族
     *
     * @param playerName
     * @return
     */
    public String findRaceType(String playerName) {
        playerName = BaseUtil.toLowerCase(playerName);

        String raceType = RaceConstants.PLAYER_RACE.get(playerName);
        if (raceType != null) {
            return raceType;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rst = null;
        raceType = RaceTypeEnum.MANKIND.getType();
        try {
            String selectStr = RacePlayerSqlEnum.SELECT_RACE_TYPE_BY_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setString(1, playerName);
            rst = ps.executeQuery();
            while (rst.next()) {
                raceType = rst.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, rst);
        }
        RaceConstants.PLAYER_RACE.put(playerName, raceType);
        return raceType;
    }

    /**
     * 根据类型查询总数
     *
     * @param raceType 类型
     * @return
     */
    public Integer findCount(String raceType) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rst = null;
        int count = 0;
        try {
            String selectStr = RacePlayerSqlEnum.SELECT_COUNT_BY_RACE_TYPE.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setString(1, raceType);
            rst = ps.executeQuery();
            while (rst.next()) {
                count = rst.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, rst);
        }
        return count;
    }

    /**
     * 增加
     *
     * @param playerName
     * @param amount
     * @return
     */
    public Boolean updateAdd(String playerName, Integer amount) {
        playerName = BaseUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_ADD_BY_PLAYER_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setLong(1, amount);
            ps.setString(2, playerName);
            ps.setLong(3, amount);
            val rst = ps.executeUpdate();
            return rst > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        return false;
    }

    /**
     * 减少
     *
     * @param playerName
     * @param amount
     * @return
     */
    public Boolean updateSubtract(String playerName, Integer amount) {
        playerName = BaseUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_SUBTRACT_BY_PLAYER_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, amount);
            ps.setString(2, playerName);
            ps.setInt(3, amount);
            val rst = ps.executeUpdate();
            return rst > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        return false;
    }

    /**
     * 设置
     *
     * @param playerName
     * @param amount
     * @return
     */
    public Boolean update(String playerName, Integer amount) {
        playerName = BaseUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_BY_PLAYER_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, amount);
            ps.setString(2, playerName);
            val rst = ps.executeUpdate();
            return rst > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        return false;
    }

    /**
     * 设置种族
     *
     * @param playerName 玩家名
     * @param raceType   种族类型
     * @param raceLevel  种族等级
     * @return
     */
    public Boolean updateRaceType(String playerName, String raceType, int raceLevel) {
        RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnum(raceType);
        if (raceTypeEnum == null) {
            return false;
        }
        int maxAmount = 0;
        switch (raceTypeEnum) {
            case WER_WOLF:
                maxAmount = ConfigUtil.raceConfig.getInt("werwolf.maxAmount");
                break;
            case VAMPIRE:
                maxAmount = ConfigUtil.raceConfig.getInt("vampire.maxAmount");
                maxAmount = (int) Math.ceil(maxAmount * ConfigUtil.raceConfig.getDouble("vampire.energyMultiple" + "." + raceLevel));
                break;
            case DEMON:
                maxAmount = ConfigUtil.raceConfig.getInt("demon.maxAmount");
                break;
            case ANGEL:
                maxAmount = ConfigUtil.raceConfig.getInt("angel.maxAmount");
                break;
            case GHOUL:
                maxAmount = ConfigUtil.raceConfig.getInt("ghoul.maxAmount");
                break;
            case DEMON_HUNTER:
                maxAmount = ConfigUtil.raceConfig.getInt("demonHunter.maxAmount");
                break;
            default:
                break;
        }
        String name = playerName;
        playerName = BaseUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        int rst = 0;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_BY_RACE_TYPE.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setString(1, raceType);
            ps.setInt(2, raceLevel);
            ps.setInt(3, maxAmount);
            ps.setLong(4, System.currentTimeMillis());
            ps.setString(5, playerName);
            rst = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        if (rst > 0) {
            String raceMsg = BaseUtil.getLangMsg("raceMsg");
            raceMsg = raceMsg.replaceAll("\\$\\{".concat("player").concat("\\}"), name)
                    .replaceAll("\\$\\{".concat("race").concat("\\}"), RaceTypeEnum.getTypeName(raceType));
            MessageApi.sendAllMessage(raceMsg);
            RaceConstants.PLAYER_RACE.remove(playerName);
        }
        return rst > 0;
    }

    /**
     * 更新种族等级
     *
     * @param playerName 玩家名
     * @param raceLevel  种族等级
     * @return
     */
    public Boolean updateRaceLevel(String playerName, int raceLevel) {
        playerName = BaseUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        int rst = 0;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_BY_RACE_LEVEL.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, raceLevel);
            ps.setString(2, playerName);
            rst = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        return rst > 0;
    }

}
