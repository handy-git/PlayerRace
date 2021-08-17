package com.handy.playerrace.command.admin;

import com.handy.lib.command.IHandyCommandEvent;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.service.RacePlayerService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

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
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        new BukkitRunnable() {
            @Override
            public void run() {
                RaceTypeEnum raceTypeEnum = null;
                if (args.length == 2) {
                    // 类型
                    raceTypeEnum = RaceTypeEnum.getEnum(args[1]);
                    if (raceTypeEnum == null) {
                        sender.sendMessage(BaseUtil.getLangMsg("typeFailureMsg"));
                        return;
                    }
                }
                // 查询全部
                if (raceTypeEnum == null) {
                    StringBuilder stringBuffer = new StringBuilder();
                    for (RaceTypeEnum raceType : RaceTypeEnum.values()) {
                        // 查询对应种族的
                        Integer count = RacePlayerService.getInstance().findCount(raceType.getType());
                        stringBuffer.append(raceType.getTypeName()).append(": ").append(count).append("\n");
                    }
                    sender.sendMessage(stringBuffer.toString());
                    return;
                }
                // 查询对应种族的
                Integer count = RacePlayerService.getInstance().findCount(raceTypeEnum.getType());
                sender.sendMessage(raceTypeEnum.getTypeName() + ": " + count);
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }
}
