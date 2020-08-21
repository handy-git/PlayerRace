package com.handy.playerrace.command.admin;

import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/11 9:36
 */
public class ReloadCommand {
    private ReloadCommand() {
    }

    private static volatile ReloadCommand instance;

    public static ReloadCommand getSingleton() {
        if (instance == null) {
            synchronized (ReloadCommand.class) {
                if (instance == null) {
                    instance = new ReloadCommand();
                }
            }
        }
        return instance;
    }

    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ConfigUtil.lordConfig();
                ConfigUtil.lordLangConfig();
                ConfigUtil.lordRaceConfig();
                sender.sendMessage(BaseUtil.getLangMsg("reloadMsg"));
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

}
