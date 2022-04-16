package com.handy.playerrace.command.admin;

import com.handy.lib.command.IHandyCommandEvent;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 重载
 *
 * @author handy
 */
public class ReloadCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "reload";
    }

    @Override
    public String permission() {
        return "playerRace.reload";
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ConfigUtil.init();
                sender.sendMessage(BaseUtil.getLangMsg("reloadMsg"));
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

}
