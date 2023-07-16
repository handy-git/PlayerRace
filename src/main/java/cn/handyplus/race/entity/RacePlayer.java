package cn.handyplus.race.entity;

import cn.handyplus.lib.annotation.TableField;
import cn.handyplus.lib.annotation.TableName;
import lombok.Data;

/**
 * @author handy
 */
@Data
@TableName(value = "race_player", comment = "玩家种族表")
public class RacePlayer {
    @TableField(value = "id", comment = "主键ID")
    private Integer id;

    @TableField(value = "player_name", comment = "玩家名称", notNull = true)
    private String playerName;

    @TableField(value = "player_uuid", comment = "玩家uuid", notNull = true)
    private String playerUuid;

    @TableField(value = "race_type", comment = "种族类型", notNull = true)
    private String raceType;

    @TableField(value = "race_level", comment = "种族等级", filedDefault = "1")
    private Integer raceLevel;

    @TableField(value = "amount", comment = "玩家能量值", notNull = true)
    private Integer amount;

    @TableField(value = "max_amount", comment = "玩家能量值上限", notNull = true)
    private Integer maxAmount;

    @TableField(value = "transfer_time", comment = "转换时间")
    private Long transferTime;

}