package com.handy.playerrace.command.admin;

import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.util.RaceUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author hs
 * @date 2021-01-20 09:46
 **/
public class MengBorneoSoupCommand {
    private MengBorneoSoupCommand() {
    }

    private static volatile MengBorneoSoupCommand instance;

    public static MengBorneoSoupCommand getSingleton() {
        if (instance == null) {
            synchronized (MengBorneoSoupCommand.class) {
                if (instance == null) {
                    instance = new MengBorneoSoupCommand();
                }
            }
        }
        return instance;
    }

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
