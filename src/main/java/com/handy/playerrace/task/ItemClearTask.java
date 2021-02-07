package com.handy.playerrace.task;

import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

/**
 * @author hs
 * @date 2021-02-07 15:01
 **/
public class ItemClearTask {

    /**
     * 异步对清理生成出来的蜘蛛网
     */
    public static void setItemClearTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Location> iterator = RaceConstants.LOCATIONS.iterator();
                while (iterator.hasNext()) {
                    Location location = iterator.next();
                    location.getWorld().getBlockAt(location).setType(Material.AIR);
                    iterator.remove();
                }
            }
        }.runTaskTimer(PlayerRace.getInstance(), 0, 20 * 60);
    }

}
