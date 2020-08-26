package com.handy.playerrace.command.admin;

import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.service.RacePlayerService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/10 15:45
 */
public class SetRaceCommand {
    private SetRaceCommand() {
    }

    private static volatile SetRaceCommand instance;

    public static SetRaceCommand getSingleton() {
        if (instance == null) {
            synchronized (SetRaceCommand.class) {
                if (instance == null) {
                    instance = new SetRaceCommand();
                }
            }
        }
        return instance;
    }

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
                Boolean rst = RacePlayerService.getInstance().updateRaceType(args[1], args[2], 0);
                if (rst) {
                    sender.sendMessage(BaseUtil.getLangMsg("succeedMsg"));
                } else {
                    sender.sendMessage(BaseUtil.getLangMsg("failureMsg"));
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

}
