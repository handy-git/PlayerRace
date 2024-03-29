package cn.handyplus.race.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author handy
 */
@Getter
@AllArgsConstructor
public enum TabListEnum {
    /**
     * 第一层提醒
     */
    FIRST(Arrays.asList("setRace", "reload", "getHelpBook", "getMengBorneoSoup", "findCount", "findRace"), 0, null, 1),

    SET_RACE_TWO(null, 1, "setRace", 2),
    SET_RACE_THREE(RaceTypeEnum.getEnum(), 1, "setRace", 3),

    FIND_COUNT_TWO(RaceTypeEnum.getEnum(), 1, "findCount", 2),

    FIND_RACE_TWO(null, 1, "findRace", 2),

    ;

    /**
     * 返回的List
     */
    private final List<String> list;
    /**
     * 识别的上个参数的位置
     */
    private final int befPos;
    /**
     * 上个参数的内容
     */
    private final String bef;
    /**
     * 这个参数可以出现的位置
     */
    private final int num;

    /**
     * 获取提醒
     *
     * @param args       参数
     * @param argsLength 参数长度
     * @param sender     发送人
     * @return 提醒参数
     */
    public static List<String> returnList(String[] args, int argsLength, CommandSender sender) {
        List<String> completions = new ArrayList<>();
        for (TabListEnum tabListEnum : TabListEnum.values()) {
            if (tabListEnum.getBefPos() - 1 >= args.length) {
                continue;
            }
            if ((tabListEnum.getBef() == null || tabListEnum.getBef().equalsIgnoreCase(args[tabListEnum.getBefPos() - 1]))
                    && tabListEnum.getNum() == argsLength) {
                completions = tabListEnum.getList();
            }
        }
        return completions;
    }

}
