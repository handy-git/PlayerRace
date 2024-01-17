package cn.handyplus.race.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.AssertUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.RaceUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author handy
 */
public class SetRaceCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "setRace";
    }

    @Override
    public String permission() {
        return "playerRace.setRace";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        AssertUtil.notTrue(args.length < 3, sender, BaseUtil.getLangMsg("paramFailureMsg"));
        // 类型
        RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnumThrow(args[2]);
        // 设置玩家种族
        OfflinePlayer offlinePlayer = BaseUtil.getOfflinePlayer(args[1]);
        int raceLevel = 1;
        if (args.length > 3) {
            raceLevel = AssertUtil.isNumericToInt(args[3], sender, "&a只能为数字");
        }
        boolean rst = CacheUtil.updateRaceType(offlinePlayer.getUniqueId(), raceTypeEnum, raceLevel);
        // 发送提醒消息
        RaceUtil.sendRaceMsg(args[1], raceTypeEnum.getType());
        MessageUtil.sendMessage(sender, BaseUtil.getLangMsg(rst ? "succeedMsg" : "failureMsg"));
    }

}
