package cn.handyplus.race.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.service.RacePlayerService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author handy
 **/
public class FindCountCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "findCount";
    }

    @Override
    public String permission() {
        return "playerRace.findCount";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        RaceTypeEnum raceTypeEnum = null;
        if (args.length == 2) {
            // 类型
            raceTypeEnum = RaceTypeEnum.getEnumThrow(args[1]);
        }
        // 查询全部
        if (raceTypeEnum == null) {
            StringBuilder stringBuffer = new StringBuilder();
            for (RaceTypeEnum raceType : RaceTypeEnum.values()) {
                // 查询对应种族的
                Integer count = RacePlayerService.getInstance().findCount(raceType.getType());
                stringBuffer.append(RaceTypeEnum.getDesc(raceType.getType())).append(": ").append(count).append("\n");
            }
            MessageUtil.sendMessage(sender, stringBuffer.toString());
            return;
        }
        // 查询对应种族的
        Integer count = RacePlayerService.getInstance().findCount(raceTypeEnum.getType());
        MessageUtil.sendMessage(sender, RaceTypeEnum.getDesc(raceTypeEnum.getType()) + ": " + count);
    }

}
