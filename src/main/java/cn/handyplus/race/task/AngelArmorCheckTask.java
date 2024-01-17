package cn.handyplus.race.task;

import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.util.CacheUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * 天使禁止皮革以外装备
 *
 * @author handy
 */
public class AngelArmorCheckTask {

    public static void start() {
        HandySchedulerUtil.runTaskTimerAsynchronously(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // 判断是否为天使
                if (!CacheUtil.isRaceType(RaceTypeEnum.ANGEL, player)) {
                    continue;
                }
                PlayerInventory inv = player.getInventory();
                ItemStack helmet = inv.getHelmet();
                ItemStack chest = inv.getChestplate();
                ItemStack leggings = inv.getLeggings();
                ItemStack boots = inv.getBoots();
                if (helmet != null && !Material.LEATHER_HELMET.equals(helmet.getType())) {
                    inv.setHelmet(null);
                    dropItem(player, helmet);
                }
                if (chest != null && !Material.LEATHER_CHESTPLATE.equals(chest.getType())) {
                    inv.setChestplate(null);
                    dropItem(player, chest);
                }
                if (leggings != null && !Material.LEATHER_LEGGINGS.equals(leggings.getType())) {
                    inv.setLeggings(null);
                    dropItem(player, leggings);
                }
                if (boots != null && !Material.LEATHER_BOOTS.equals(boots.getType())) {
                    inv.setBoots(null);
                    dropItem(player, boots);
                }
                MessageUtil.sendActionbar(player, BaseUtil.getLangMsg("angel.wearEquipmentMsg"));
            }
        }, 0, 20);
    }

    private static void dropItem(Player player, ItemStack item) {
        HandySchedulerUtil.runTask(() -> player.getWorld().dropItem(player.getLocation(), item));
    }

}
