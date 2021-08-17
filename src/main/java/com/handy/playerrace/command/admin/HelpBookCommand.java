package com.handy.playerrace.command.admin;

import com.handy.lib.command.IHandyCommandEvent;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.service.RacePlayerService;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author handy
 * @date 2021-01-16 14:55
 **/
public class HelpBookCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "getHelpBook";
    }

    @Override
    public String permission() {
        return "playerRace.getHelpBook";
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (BaseUtil.isNotPlayer(sender)) {
            sender.sendMessage(BaseUtil.getLangMsg("noPlayerFailureMsg"));
            return;
        }
        Player player = (Player) sender;
        new BukkitRunnable() {
            @Override
            public void run() {
                // 判断种族
                String raceType = RacePlayerService.getInstance().findRaceType(player.getName());
                RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnum(raceType);
                if (raceTypeEnum == null) {
                    sender.sendMessage(BaseUtil.getLangMsg("typeFailureMsg"));
                    return;
                }
                player.getInventory().addItem(RaceUtil.getRaceHelpBook(raceTypeEnum));
                sender.sendMessage(BaseUtil.getLangMsg("succeedMsg"));
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

}
