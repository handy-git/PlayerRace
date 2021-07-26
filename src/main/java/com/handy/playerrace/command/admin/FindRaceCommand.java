package com.handy.playerrace.command.admin;

import com.handy.lib.command.IHandyCommandEvent;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.service.RacePlayerService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author hs
 * @date 2021-01-21 16:04
 **/
public class FindRaceCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "findRace";
    }

    @Override
    public String permission() {
        return "playerRace.findRace";
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (args.length == 2) {
                    RaceTypeEnum anEnum = RaceTypeEnum.getEnum(RacePlayerService.getInstance().findRaceType(args[1]));
                    if (anEnum == null) {
                        sender.sendMessage(BaseUtil.getLangMsg("typeFailureMsg"));
                    } else {
                        sender.sendMessage(args[1] + ": " + anEnum.getTypeName());
                    }
                    return;
                }
                if (Bukkit.getOnlinePlayers().size() == 0) {
                    sender.sendMessage(BaseUtil.getLangMsg("succeedMsg"));
                    return;
                }

                // 查询全部在线玩家
                for (Player player : Bukkit.getOnlinePlayers()) {
                    RaceTypeEnum anEnum = RaceTypeEnum.getEnum(RacePlayerService.getInstance().findRaceType(player.getName()));
                    if (anEnum != null) {
                        sender.sendMessage(player.getName() + ": " + anEnum.getTypeName());
                    }
                }
            }

        }.runTaskAsynchronously(PlayerRace.getInstance());
    }
}
