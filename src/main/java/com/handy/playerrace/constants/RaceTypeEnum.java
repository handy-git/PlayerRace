package com.handy.playerrace.constants;

import com.handy.lib.util.BaseUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 种族类型
 *
 * @author handy
 */
@Getter
@AllArgsConstructor
public enum RaceTypeEnum {
    /**
     * 种族类型
     */
    MANKIND("mankind", BaseUtil.getLangMsg("raceType.mankind")),
    WER_WOLF("wer_wolf", BaseUtil.getLangMsg("raceType.werwolf")),
    VAMPIRE("vampire", BaseUtil.getLangMsg("raceType.vampire")),
    GHOUL("ghoul", BaseUtil.getLangMsg("raceType.ghoul")),
    DEMON("demon", BaseUtil.getLangMsg("raceType.demon")),
    ANGEL("angel", BaseUtil.getLangMsg("raceType.angel")),
    DEMON_HUNTER("demon_hunter", BaseUtil.getLangMsg("raceType.demonHunter")),
    ;

    private final String type;
    private final String typeName;

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
        return "";
    }

}
