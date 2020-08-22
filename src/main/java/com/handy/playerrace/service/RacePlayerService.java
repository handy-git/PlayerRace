package com.handy.playerrace.service;


import com.handy.lib.api.StorageApi;
import com.handy.lib.util.BaseUtil;
import com.handy.lib.util.SqlManagerUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.constants.sql.RacePlayerSqlEnum;
import com.handy.playerrace.entity.RacePlayer;
import lombok.val;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
     * 根据playerUuid查询
     *
     * @param playerUuid
     * @return
     */
    public RacePlayer findByPlayerUuid(String playerUuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rst = null;
        try {
            String selectStr = RacePlayerSqlEnum.SELECT_BY_UUID.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setString(1, playerUuid);
            rst = ps.executeQuery();
            while (rst.next()) {
                RacePlayer racePlayer = new RacePlayer();
                racePlayer.setId(rst.getLong(1));
                racePlayer.setPlayerName(rst.getString(2));
                racePlayer.setPlayerUuid(rst.getString(3));
                racePlayer.setRaceType(rst.getString(4));
                racePlayer.setRaceLevel(rst.getInt(5));
                racePlayer.setAmount(rst.getInt(6));
                racePlayer.setMaxAmount(rst.getInt(7));
                racePlayer.setTransferTime(rst.getLong(8));
                return racePlayer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, rst);
        }
        return null;
    }

    /**
     * 查询全部
     *
     * @param maxFatigue 最大限制
     * @return
     */
    public List<RacePlayer> findAll(Integer maxFatigue) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rst = null;
        List<RacePlayer> racePlayers = new ArrayList<>();
        try {
            String selectStr = RacePlayerSqlEnum.SELECT_RESTORE_ALL.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, maxFatigue);
            rst = ps.executeQuery();
            while (rst.next()) {
                RacePlayer racePlayer = new RacePlayer();
                racePlayer.setId(rst.getLong(1));
                racePlayer.setPlayerName(rst.getString(2));
                racePlayer.setPlayerUuid(rst.getString(3));
                racePlayer.setRaceType(rst.getString(4));
                racePlayer.setRaceLevel(rst.getInt(5));
                racePlayer.setAmount(rst.getInt(6));
                racePlayer.setMaxAmount(rst.getInt(7));
                racePlayer.setTransferTime(rst.getLong(8));
                racePlayers.add(racePlayer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, rst);
        }
        return racePlayers;
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
     * @param playerName 玩家
     * @param playerName 玩家名
     * @param raceType   种族类型
     * @param raceLevel  种族等级
     * @return
     */
    public Boolean updateRaceType(Player player, String playerName, String raceType, int raceLevel) {
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
            ps.setLong(3, System.currentTimeMillis());
            ps.setString(4, playerName);
            rst = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        if (rst > 0) {
            RaceConstants.PLAYER_RACE.remove(playerName);
        }
        return rst > 0;
    }

    /**
     * 增加
     *
     * @param playerUuid
     * @param amount
     * @return
     */
    public Boolean updateAddByUuid(String playerUuid, Integer amount) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_ADD_BY_PLAYER_UUID.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setLong(1, amount);
            ps.setString(2, playerUuid);
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
     * @param playerUuid
     * @param amount
     * @return
     */
    public Boolean updateSubtractByUuid(String playerUuid, Integer amount) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_SUBTRACT_BY_PLAYER_UUID.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, amount);
            ps.setString(2, playerUuid);
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
     * @param playerUuid
     * @param amount
     * @return
     */
    public Boolean updateByUuid(String playerUuid, Integer amount) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_BY_PLAYER_UUID.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, amount);
            ps.setString(2, playerUuid);
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
     * 设置最大值
     *
     * @param playerName
     * @param maxAmount
     * @return
     */
    public Boolean updateMaxAmount(String playerName, Integer maxAmount) {
        playerName = BaseUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_MAX_AMOUNT_BY_PLAYER_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection(PlayerRace.getInstance());
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, maxAmount);
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

}
