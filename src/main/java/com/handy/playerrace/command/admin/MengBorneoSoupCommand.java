package com.handy.playerrace.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author handy
 * @date 2021-01-20 09:46
 **/
public class MengBorneoSoupCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "getMengBorneoSoup";
    }

    @Override
    public String permission() {
        return "playerRace.getMengBorneoSoup";
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
                player.getInventory().addItem(RaceUtil.getMengBorneoSoup());
                sender.sendMessage(BaseUtil.getLangMsg("succeedMsg"));
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

}
