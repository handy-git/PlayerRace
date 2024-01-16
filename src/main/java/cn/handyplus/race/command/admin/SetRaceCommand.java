package cn.handyplus.race.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.AssertUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.RaceUtil;
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
        Boolean rst = RacePlayerService.getInstance().updateRaceType(args[1], raceTypeEnum.getType());
        if (rst) {
            MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("succeedMsg"));
            RaceUtil.refreshCache(args[1]);
        } else {
            MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("failureMsg"));
        }
    }

}
