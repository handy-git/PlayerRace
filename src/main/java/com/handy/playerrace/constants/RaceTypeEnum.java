package com.handy.playerrace.constants;

import com.handy.lib.util.BaseUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hs
 * @Description: {种族类型}
 * @date 2020/8/19 18:53
 */
@Getter
@AllArgsConstructor
public enum RaceTypeEnum {
    /**
     * 种族类型
     */
    MANKIND("mankind", BaseUtil.getLangMsg("raceType.mankind"), 0L),
    WER_WOLF("wer_wolf", BaseUtil.getLangMsg("raceType.werwolf"), 1L),
    VAMPIRE("vampire", BaseUtil.getLangMsg("raceType.vampire"), 2L),
    GHOUL("ghoul", BaseUtil.getLangMsg("raceType.ghoul"), 3L),
    DEMON("demon", BaseUtil.getLangMsg("raceType.demon"), 4L),
    PASTOR("pastor", BaseUtil.getLangMsg("raceType.pastor"), 5L),
    ANGEL("angel", BaseUtil.getLangMsg("raceType.angel"), 6L),
    DEMON_HUNTER("demon_hunter", BaseUtil.getLangMsg("raceType.demonHunter"), 7L),
    ;

    private final String type;
    private final String typeName;
    private final Long typeId;

    public static RaceTypeEnum getEnum(String type) {
        for (RaceTypeEnum raceTypeEnum : RaceTypeEnum.values()) {
            if (raceTypeEnum.getType().equalsIgnoreCase(type)) {
                return raceTypeEnum;
            }
        }
        return MANKIND;
    }

    public static List<String> getEnum() {
        List<String> enumList = new ArrayList<>();
        for (RaceTypeEnum raceTypeEnum : RaceTypeEnum.values()) {
            enumList.add(raceTypeEnum.getType());
        }
        return enumList;
    }

    public static String getTypeName(String type) {
        for (RaceTypeEnum raceTypeEnum : RaceTypeEnum.values()) {
            if (raceTypeEnum.getType().equals(type)) {
                return raceTypeEnum.getTypeName();
            }
        }
        return null;
    }

    public static Long getTypeId(String type) {
        for (RaceTypeEnum raceTypeEnum : RaceTypeEnum.values()) {
            if (raceTypeEnum.getType().equals(type)) {
                return raceTypeEnum.getTypeId();
            }
        }
        return null;
    }

    public static String getType(Long typeId) {
        for (RaceTypeEnum raceTypeEnum : RaceTypeEnum.values()) {
            if (raceTypeEnum.getTypeId().equals(typeId)) {
                return raceTypeEnum.getType();
            }
        }
        return null;
    }

}
