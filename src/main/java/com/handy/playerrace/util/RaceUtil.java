package com.handy.playerrace.util;

import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/20 15:15
 */
public class RaceUtil {

    /**
     * 能量不足
     *
     * @param amount   消耗值
     * @param myAmount 我的能量
     * @return msg
     */
    public static String getEnergyShortageMsg(Integer amount, Integer myAmount) {
        Map<String, String> map = new HashMap<>();
        map.put("amount", amount.toString());
        map.put("myAmount", myAmount.toString());

        String actionBarMsg = ConfigUtil.langConfig.getString("energyShortageMsg");
        if (actionBarMsg == null || "".equals(actionBarMsg)) {
            return "";
        }
        for (String str : map.keySet()) {
            actionBarMsg = actionBarMsg.replaceAll("\\$\\{".concat(str).concat("\\}")
                    , map.get(str));
        }
        return BaseUtil.replaceChatColor(actionBarMsg);
    }

    /**
     * 生成该隐之血合成秘籍
     */
    public static void addVampire() {
        ItemStack itemStack = getItemStack();
        ShapedRecipe identifyRecipe;
        if (VersionCheckEnum.getEnum().getVersionId() < VersionCheckEnum.V_1_13.getVersionId()) {
            identifyRecipe = new ShapedRecipe(itemStack);
        } else {
            identifyRecipe = new ShapedRecipe(new NamespacedKey(PlayerRace.getInstance(), "vampire"), itemStack);
        }
        identifyRecipe.shape("ABC", "DEF", "ABC");
        identifyRecipe.setIngredient('A', Material.EMERALD);
        identifyRecipe.setIngredient('B', Material.DIAMOND);
        identifyRecipe.setIngredient('C', Material.EMERALD);
        identifyRecipe.setIngredient('D', Material.NETHER_STAR);
        identifyRecipe.setIngredient('E', Material.MUSHROOM_STEW);
        identifyRecipe.setIngredient('F', Material.NETHER_STAR);
        Bukkit.addRecipe(identifyRecipe);
    }

    /**
     * 获取该隐物品
     *
     * @return
     */
    public static ItemStack getItemStack() {
        Integer versionId = VersionCheckEnum.getEnum().getVersionId();
        String material = "MUSHROOM_STEW";
        if (versionId < VersionCheckEnum.V_1_13.getVersionId()) {
            material = "MUSHROOM_SOUP";
        }
        ItemStack itemStack = new ItemStack(Material.valueOf(material));
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }
        String name = ConfigUtil.raceConfig.getString("vampire.name");
        itemMeta.setDisplayName(BaseUtil.replaceChatColor(name != null ? name : ""));
        List<String> lores = new ArrayList<>();
        List<String> loreList = ConfigUtil.raceConfig.getStringList("vampire.lores");
        for (String lore : loreList) {
            lores.add(BaseUtil.replaceChatColor(lore));
        }
        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * 知识之书-该隐之血合成配方
     */
    public static ItemStack getKnowledgeBook() {
        if (VersionCheckEnum.getEnum().getVersionId() < VersionCheckEnum.V_1_13.getVersionId()) {
            return new ItemStack(Material.AIR);
        }
        //获取知识之书的ItemStack对象
        ItemStack book = new ItemStack(Material.KNOWLEDGE_BOOK);
        KnowledgeBookMeta bookMeta = (KnowledgeBookMeta) book.getItemMeta();
        if (bookMeta == null) {
            return book;
        }
        //添加合成配方，可以多个
        bookMeta.addRecipe(new NamespacedKey(PlayerRace.getInstance(), "vampire"));
        //设置Meta数据
        book.setItemMeta(bookMeta);
        return book;
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param dateTime 时间
     * @return
     */
    public static int getDifferDay(Long dateTime) {
        return (int) ((System.currentTimeMillis() - dateTime) / (1000 * 3600 * 24));
    }

    /**
     * 玩家时间是否为夜晚
     *
     * @param player 玩家
     * @return
     */
    public static boolean playerTimeIsNether(Player player) {
        return World.Environment.NETHER.equals(player.getWorld().getEnvironment());
    }

    /**
     * 玩家时间是否为夜晚
     *
     * @param player 玩家
     * @return
     */
    public static boolean playerTimeIsNotNether(Player player) {
        return !playerTimeIsNether(player);
    }

    /**
     * 世界时间是否为夜晚
     *
     * @param player 玩家
     * @return
     */
    public static boolean worldTimeIsNight(Player player) {
        long time = player.getWorld().getTime() % 24000L;
        return time < 0L || time > 12400L;
    }

    /**
     * 世界时间是否为夜晚
     *
     * @param player 玩家
     * @return
     */
    public static boolean worldTimeIsNotNight(Player player) {
        return !worldTimeIsNight(player);
    }

    /**
     * 判断是否晴天
     *
     * @param player 玩家
     * @return
     */
    public static boolean worldIsStorm(Player player) {
        return player.getWorld().hasStorm();
    }

    /**
     * 判断是否晴天
     *
     * @param player 玩家
     * @return
     */
    public static boolean worldIsNotStorm(Player player) {
        return !worldIsStorm(player);
    }

    /**
     * 玩家头上是否为空气
     *
     * @param player 玩家
     * @return
     */
    public static boolean isUnderRoof(Player player) {
        World world = player.getWorld();
        int blockYat = world.getHighestBlockYAt(player.getLocation());
        return blockYat >= 254;
    }

    /**
     * 玩家头上是否为空气
     *
     * @param player 玩家
     * @return
     */
    public static boolean isNotUnderRoof(Player player) {
        return !isUnderRoof(player);
    }

}
