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
 * 恶魔禁止锁链以外装备
 *
 * @author handy
 */
public class DemonArmorCheckTask {

    public static void start() {
        HandySchedulerUtil.runTaskTimerAsynchronously(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // 判断是否为恶魔
                if (!CacheUtil.isRaceType(RaceTypeEnum.DEMON, player)) {
                    continue;
                }
                PlayerInventory inv = player.getInventory();
                ItemStack helmet = inv.getHelmet();
                ItemStack chest = inv.getChestplate();
                ItemStack leggings = inv.getLeggings();
                ItemStack boots = inv.getBoots();
                if (helmet != null && !Material.CHAINMAIL_HELMET.equals(helmet.getType())) {
                    inv.setHelmet(null);
                    dropItem(player, helmet);
                }
                if (chest != null && !Material.CHAINMAIL_CHESTPLATE.equals(chest.getType())) {
                    inv.setChestplate(null);
                    dropItem(player, chest);
                }
                if (leggings != null && !Material.CHAINMAIL_LEGGINGS.equals(leggings.getType())) {
                    inv.setLeggings(null);
                    dropItem(player, leggings);
                }
                if (boots != null && !Material.CHAINMAIL_BOOTS.equals(boots.getType())) {
                    inv.setBoots(null);
                    dropItem(player, boots);
                }
                MessageUtil.sendActionbar(player, BaseUtil.getLangMsg("demon.wearEquipmentMsg"));
            }
        }, 0, 20);
    }

    private static void dropItem(Player player, ItemStack item) {
        HandySchedulerUtil.runTask(() -> player.getWorld().dropItem(player.getLocation(), item));
    }

}
