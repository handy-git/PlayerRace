package com.handy.playerrace.service;


import com.handy.lib.api.MessageApi;
import com.handy.lib.constants.BaseConstants;
import com.handy.lib.core.StrUtil;
import com.handy.lib.util.BaseUtil;
import com.handy.lib.util.SqlManagerUtil;
import com.handy.playerrace.constants.RaceConstants;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.constants.sql.RacePlayerSqlEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.util.ConfigUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
     * 建表
     */
    public void create() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = SqlManagerUtil.getInstance().getConnection();
            String sql = RacePlayerSqlEnum.CREATE_SQ_LITE.getCommand();
            if (BaseConstants.MYSQL.equals(BaseConstants.STORAGE_CONFIG.getString(BaseConstants.STORAGE_METHOD))) {
                sql = RacePlayerSqlEnum.CREATE_MYSQL.getCommand();
            }
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
    }

    /**
     * 新增
     *
     * @param racePlayer 数据
     * @return true 成功
     */
    public Boolean add(RacePlayer racePlayer) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String addStr = RacePlayerSqlEnum.ADD_DATA.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
            ps = conn.prepareStatement(addStr);
            ps.setString(1, racePlayer.getPlayerName().toLowerCase());
            ps.setString(2, racePlayer.getPlayerUuid());
            ps.setString(3, racePlayer.getRaceType());
            ps.setInt(4, racePlayer.getRaceLevel() != null ? racePlayer.getRaceLevel() : 0);
            ps.setInt(5, racePlayer.getAmount());
            ps.setInt(6, racePlayer.getMaxAmount() != null ? racePlayer.getMaxAmount() : 0);
            ps.setLong(7, racePlayer.getTransferTime() != null ? racePlayer.getTransferTime() : 0L);
            return ps.executeUpdate() > 0;
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
     * @param playerName 玩家名
     * @return 种族
     */
    public RacePlayer findByPlayerName(String playerName) {
        playerName = StrUtil.toLowerCase(playerName);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rst = null;
        RacePlayer racePlayer = null;
        try {
            String selectStr = RacePlayerSqlEnum.SELECT_BY_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
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
     * @param playerName 玩家名
     * @return 种族名
     */
    public String findRaceType(String playerName) {
        playerName = StrUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rst = null;
        String raceType = null;
        try {
            String selectStr = RacePlayerSqlEnum.SELECT_RACE_TYPE_BY_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
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
        return raceType;
    }

    /**
     * 根据类型查询总数
     *
     * @param raceType 类型
     * @return 总数
     */
    public Integer findCount(String raceType) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rst = null;
        int count = 0;
        try {
            String selectStr = RacePlayerSqlEnum.SELECT_COUNT_BY_RACE_TYPE.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
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
     * @param playerName 玩家名
     * @param amount     数量
     * @return true 成功
     */
    public Boolean updateAdd(String playerName, Integer amount) {
        playerName = StrUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_ADD_BY_PLAYER_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
            ps = conn.prepareStatement(selectStr);
            ps.setLong(1, amount);
            ps.setString(2, playerName);
            ps.setLong(3, amount);
            return ps.executeUpdate() > 0;
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
     * @param playerName 玩家名
     * @param amount     数量
     * @return true 成功
     */
    public Boolean updateSubtract(String playerName, Integer amount) {
        playerName = StrUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_SUBTRACT_BY_PLAYER_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, amount);
            ps.setString(2, playerName);
            ps.setInt(3, amount);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        return false;
    }

    /**
     * 根据名称设置数量
     *
     * @param playerName 玩家名
     * @param amount     数量
     * @return true 成功
     */
    public Boolean updateAmountByPlayerName(String playerName, Integer amount) {
        playerName = StrUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_AMOUNT_BY_PLAYER_NAME.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, amount);
            ps.setString(2, playerName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        return false;
    }

    /**
     * 根据id置数量
     *
     * @param racePlayer 信息
     * @return true 成功
     */
    public Boolean updateAmountById(RacePlayer racePlayer) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_AMOUNT_BY_ID.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
            ps = conn.prepareStatement(selectStr);
            ps.setInt(1, racePlayer.getAmount());
            ps.setLong(2, racePlayer.getId());
            return ps.executeUpdate() > 0;
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
        playerName = StrUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        int rst = 0;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_BY_RACE_TYPE.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
            ps = conn.prepareStatement(selectStr);
            ps.setString(1, raceType);
            ps.setInt(2, raceLevel);
            ps.setInt(3, maxAmount);
            ps.setInt(4, maxAmount);
            ps.setLong(5, System.currentTimeMillis());
            ps.setString(6, playerName);
            rst = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlManagerUtil.getInstance().closeSql(conn, ps, null);
        }
        if (rst > 0) {
            String raceMsg = BaseUtil.getLangMsg("raceMsg");
            raceMsg = raceMsg.replace("${player}", name).replace("${race}", RaceTypeEnum.getTypeName(raceType));
            MessageApi.sendAllMessage(raceMsg);
            RaceConstants.PLAYER_RACE.put(playerName, this.findByPlayerName(playerName));
        }
        return rst > 0;
    }

    /**
     * 更新种族等级
     *
     * @param playerName 玩家名w
     * @param raceLevel  种族等级
     * @return true 成功
     */
    public Boolean updateRaceLevel(String playerName, int raceLevel) {
        playerName = StrUtil.toLowerCase(playerName);
        Connection conn = null;
        PreparedStatement ps = null;
        int rst = 0;
        try {
            String selectStr = RacePlayerSqlEnum.UPDATE_BY_RACE_LEVEL.getCommand();
            conn = SqlManagerUtil.getInstance().getConnection();
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
