package com.handy.playerrace.util;

import com.google.common.collect.Maps;
import com.handy.lib.api.MessageApi;
import com.handy.lib.constants.VersionCheckEnum;
import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.PlayerRace;
import com.handy.playerrace.constants.RaceTypeEnum;
import com.handy.playerrace.entity.RacePlayer;
import com.handy.playerrace.service.RacePlayerService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
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
     * @param amount 消耗值
     * @return msg
     */
    public static String getEnergyShortageMsg(Integer amount) {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(1);
        map.put("amount", amount.toString());

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
     * 恢复能量
     *
     * @param player       玩家
     * @param raceTypeEnum 种族
     * @param amount       能量
     */
    public static void restoreEnergy(Player player, RaceTypeEnum raceTypeEnum, int amount) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 判断是否为对应种族
                RacePlayer racePlayer = RacePlayerService.getInstance().findByPlayerName(player.getName());
                if (racePlayer == null || !raceTypeEnum.getType().equals(racePlayer.getRaceType())) {
                    return;
                }
                int maxFatigue = ConfigUtil.config.getInt("maxFatigue");
                if (racePlayer.getMaxAmount() != null && racePlayer.getMaxAmount() != 0) {
                    maxFatigue = racePlayer.getMaxAmount();
                }
                // 吸血鬼计算最大值
                if (RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
                    double energyDiscount = ConfigUtil.raceConfig.getDouble("vampire.energyDiscount" + racePlayer.getRaceLevel());
                    if (energyDiscount > 0) {
                        maxFatigue = (int) Math.ceil(maxFatigue * energyDiscount);
                    }
                }

                if (racePlayer.getAmount() >= maxFatigue) {
                    return;
                }
                int num = amount;
                if (racePlayer.getAmount() + amount > maxFatigue) {
                    num = maxFatigue - racePlayer.getAmount();
                }
                Boolean rst = RacePlayerService.getInstance().updateAdd(player.getName(), num);
                if (rst) {
                    String restoreEnergyMsg = ConfigUtil.langConfig.getString("restoreEnergyMsg");
                    restoreEnergyMsg = restoreEnergyMsg.replaceAll("\\$\\{".concat("amount").concat("\\}")
                            , amount + "");
                    MessageApi.sendActionbar(player, BaseUtil.replaceChatColor(restoreEnergyMsg));
                }
            }
        }.runTaskAsynchronously(PlayerRace.getInstance());
    }

    /**
     * 注册合成表
     */
    public static void registerCompound() {
        // 孟婆汤
        addMengBorneoSoup();
        // 该隐之血
        addVampire();
    }

    /**
     * 生成孟婆汤合成秘籍
     */
    public static void addMengBorneoSoup() {
        ItemStack itemStack = getMengBorneoSoup();
        ShapedRecipe identifyRecipe;
        if (VersionCheckEnum.getEnum().getVersionId() < VersionCheckEnum.V_1_13.getVersionId()) {
            identifyRecipe = new ShapedRecipe(itemStack);
        } else {
            identifyRecipe = new ShapedRecipe(new NamespacedKey(PlayerRace.getInstance(), "mengBorneoSoup"), itemStack);
        }
        identifyRecipe.shape("ABC", "DEF", "GHL");
        identifyRecipe.setIngredient('A', Material.APPLE);
        identifyRecipe.setIngredient('B', Material.GUNPOWDER);
        identifyRecipe.setIngredient('C', Material.WHEAT);
        identifyRecipe.setIngredient('D', Material.EMERALD);
        identifyRecipe.setIngredient('E', Material.MUSHROOM_STEW);
        identifyRecipe.setIngredient('F', Material.EMERALD);
        identifyRecipe.setIngredient('G', Material.CARROT);
        identifyRecipe.setIngredient('H', Material.GUNPOWDER);
        identifyRecipe.setIngredient('L', Material.POTATO);
        Bukkit.addRecipe(identifyRecipe);
    }

    /**
     * 获取孟婆汤物品
     *
     * @return 物品
     */
    public static ItemStack getMengBorneoSoup() {
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
        String name = ConfigUtil.raceConfig.getString("mankind.name");
        itemMeta.setDisplayName(BaseUtil.replaceChatColor(name != null ? name : ""));
        List<String> lores = new ArrayList<>();
        List<String> loreList = ConfigUtil.raceConfig.getStringList("mankind.lores");
        for (String lore : loreList) {
            lores.add(BaseUtil.replaceChatColor(lore));
        }
        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * 知识之书-孟婆汤合成配方
     *
     * @return 知识之书
     */
    public static ItemStack getMengBorneoSoupBook() {
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
        bookMeta.addRecipe(new NamespacedKey(PlayerRace.getInstance(), "mengBorneoSoup"));
        //设置Meta数据
        book.setItemMeta(bookMeta);
        return book;
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
     * @return 该隐物品
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

}
