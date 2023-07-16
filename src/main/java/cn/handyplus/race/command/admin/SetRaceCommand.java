package cn.handyplus.race.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.race.PlayerRace;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.RaceUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

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
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(BaseUtil.getLangMsg("paramFailureMsg"));
            return;
        }
        // 类型
        RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnum(args[2]);
        if (raceTypeEnum == null) {
            sender.sendMessage(BaseUtil.getLangMsg("typeFailureMsg"));
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                // 设置玩家种族
                Boolean rst = RacePlayerService.getInstance().updateRaceType(args[1], args[2]);
                if (rst) {
                    sender.sendMessage(BaseUtil.getLangMsg("succeedMsg"));
                    RaceUtil.refreshCache(args[1]);
                } else {
                    sender.sendMessage(BaseUtil.getLangMsg("failureMsg"));
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

}
