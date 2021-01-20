package com.handy.playerrace.constants.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hs
 * @Description: {}
 * @date 2020/3/25 13:55
 */
@Getter
@AllArgsConstructor
public enum RacePlayerSqlEnum {
    /**
     * 创建mysql表
     */
    CREATE_MYSQL(
            "CREATE TABLE IF NOT EXISTS  `race_player`  (" +
                    "  `id` bigint(20) NOT NULL AUTO_INCREMENT," +
                    "  `player_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '玩家名称'," +
                    "  `player_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '玩家uuid'," +
                    "  `race_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '种族'," +
                    "  `race_level` int(11) NULL DEFAULT NULL COMMENT '种族级别'," +
                    "  `amount` int(11) NOT NULL COMMENT '当前能量值'," +
                    "  `max_amount` int(11) NOT NULL COMMENT '最大能量值'," +
                    "  `transfer_time` bigint(20) NULL COMMENT '转换时间'," +
                    "  PRIMARY KEY (`id`) USING BTREE," +
                    "  INDEX `idx_name`(`player_name`) USING BTREE," +
                    "  INDEX `idx_uuid`(`player_uuid`) USING BTREE" +
                    ") ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '玩家种族表' ROW_FORMAT = Dynamic;"
    ),
    /**
     * 创建sqLite表
     */
    CREATE_SQ_LITE(
            "CREATE TABLE IF NOT EXISTS `race_player` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`player_name` text NOT NULL," +
                    "`player_uuid` text NOT NULL," +
                    "`race_type` text NOT NULL," +
                    "`race_level` INTEGER NULL," +
                    "`amount` INTEGER NOT NULL," +
                    "`max_amount` INTEGER NOT NULL," +
                    "`transfer_time` INTEGER NULL" +
                    ");"
    ),
    ADD_DATA(
            "INSERT INTO `race_player`" +
                    "(`id`, `player_name`,`player_uuid`,`race_type`,`race_level`, `amount`, `max_amount`,`transfer_time`)" +
                    " VALUES (null, ?, ?, ?, ?, ?, ?, ?);"
    ),
    SELECT_ALL(
            "SELECT * FROM `race_player`"
    ),
    SELECT_BY_NAME(
            "SELECT * FROM `race_player` WHERE `player_name` = ?"
    ),
    SELECT_RACE_TYPE_BY_NAME(
            "SELECT `race_type` FROM `race_player` WHERE `player_name` = ?"
    ),
    SELECT_COUNT_BY_RACE_TYPE(
            "SELECT count(*) FROM `race_player` WHERE `race_type` = ?"
    ),
    UPDATE_ADD_BY_PLAYER_NAME(
            "UPDATE `race_player` SET `amount` = `amount` + ? WHERE `player_name` = ? AND `amount` + ? <= `max_amount`"
    ),
    UPDATE_SUBTRACT_BY_PLAYER_NAME(
            "UPDATE `race_player` SET `amount` = `amount` - ? WHERE `player_name` = ? AND `amount` - ? >= 0"
    ),
    UPDATE_AMOUNT_BY_PLAYER_NAME(
            "UPDATE `race_player` SET `amount` = ? WHERE `player_name` = ?"
    ),
    UPDATE_AMOUNT_BY_ID(
            "UPDATE `race_player` SET `amount` = ? WHERE `id` = ?"
    ),
    UPDATE_BY_RACE_TYPE(
            "UPDATE `race_player` SET `race_type` = ? , `race_level` = ? , `amount` = ? , `max_amount`= ?, `transfer_time` = ? WHERE `player_name` = ?"
    ),
    UPDATE_BY_RACE_LEVEL(
            "UPDATE `race_player` SET `race_level` = ? WHERE `player_name` = ?"
    ),
    ;

    private final String command;
}
