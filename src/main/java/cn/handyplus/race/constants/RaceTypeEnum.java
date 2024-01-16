package cn.handyplus.race.constants;

import cn.handyplus.lib.exception.HandyException;
import cn.handyplus.lib.util.BaseUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 种族类型
 *
 * @author handy
 */

@AllArgsConstructor
public enum RaceTypeEnum {
    /**
     * 种族类型
     */
    MANKIND("mankind", "raceType.mankind", "mankind.mengBorneoSoupMsg"),
    WER_WOLF("wer_wolf", "raceType.werwolf", "werwolf.succeedMsg"),
    VAMPIRE("vampire", "raceType.vampire", "vampire.ancestorSucceedMsg"),
    GHOUL("ghoul", "raceType.ghoul", "ghoul.succeedMsg"),
    DEMON("demon", "raceType.demon", "demon.succeedMsg"),
    ANGEL("angel", "raceType.angel", "angel.succeedMsg"),
    DEMON_HUNTER("demon_hunter", "raceType.demonHunter", "mankind.killsucceedMsg");

    @Getter
    private final String type;
    private final String desc;
    private final String tip;

    public static RaceTypeEnum getEnumThrow(String type) {
        for (RaceTypeEnum raceTypeEnum : RaceTypeEnum.values()) {
            if (raceTypeEnum.getType().equalsIgnoreCase(type)) {
                return raceTypeEnum;
            }
        }
        throw new HandyException(BaseUtil.getMsgNotColor("typeFailureMsg"));
    }

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

    public static String getDesc(String type) {
        RaceTypeEnum raceTypeEnum = getEnumThrow(type);
        return BaseUtil.getLangMsg(raceTypeEnum.desc);
    }

    public static String getTip(RaceTypeEnum raceTypeEnum) {
        return BaseUtil.getLangMsg(raceTypeEnum.tip);
    }

}
