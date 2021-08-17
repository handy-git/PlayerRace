package com.handy.playerrace.constants;

import com.handy.lib.util.BaseUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author handy
 */
@Getter
@AllArgsConstructor
public enum DemonHunterBowTypeEnum {

    /**
     * 弓类型
     */
    STRENGTH("strength", BaseUtil.getLangMsg("demonHunterBowType.strength")),
    FIRE("fire", BaseUtil.getLangMsg("demonHunterBowType.fire")),
    WEB("web", BaseUtil.getLangMsg("demonHunterBowType.web"));;

    private final String type;
    private final String typeName;

    public static DemonHunterBowTypeEnum getEnum(String type) {
        for (DemonHunterBowTypeEnum demonHunterBowTypeEnum : DemonHunterBowTypeEnum.values()) {
            if (demonHunterBowTypeEnum.getType().equalsIgnoreCase(type)) {
                return demonHunterBowTypeEnum;
            }
        }
        return STRENGTH;
    }

}
