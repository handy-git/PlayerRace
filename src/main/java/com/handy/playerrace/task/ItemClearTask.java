package com.handy.playerrace.task;

import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.lib.util.ItemStackUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceConstants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

/**
 * @author handy
 **/
public class ItemClearTask {

    /**
     * 同步定时清理生成出来的蜘蛛网
     */
    public static void setItemClearTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Location> iterator = RaceConstants.LOCATIONS.iterator();
                while (iterator.hasNext()) {
                    Location location = iterator.next();
                    Block block = location.getBlock();
                    // 如果为蜘蛛网就清理
                    String web = "WEB";
                    if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_12.getVersionId()) {
                        web = "COBWEB";
                    }
                    Material material = ItemStackUtil.getMaterial(web);
                    if (block.getType().equals(material)) {
                        location.getWorld().getBlockAt(location).setType(Material.AIR);
                    }
                    iterator.remove();
                }
            }
        }.runTaskTimer(PlayerRace.getInstance(), 0, 20 * 60);
    }

}
