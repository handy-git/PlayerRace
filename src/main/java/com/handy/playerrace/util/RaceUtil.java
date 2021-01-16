package com.handy.playerrace.util;

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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.scheduler.BukkitRunnable;

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
     * @param amount 消耗值
     * @return msg
     */
    public static String getEnergyShortageMsg(Integer amount) {
        Map<String, String> map = new HashMap<>();
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

        String gunpowderStr = "GUNPOWDER";
        String mushroomStewStr = "MUSHROOM_STEW";
        if (VersionCheckEnum.getEnum().getVersionId() < VersionCheckEnum.V_1_13.getVersionId()) {
            identifyRecipe = new ShapedRecipe(itemStack);
            mushroomStewStr = "MUSHROOM_SOUP";
            gunpowderStr = "SULPHUR";
        } else {
            identifyRecipe = new ShapedRecipe(new NamespacedKey(PlayerRace.getInstance(), "mengBorneoSoup"), itemStack);
        }
        Material gunpowder = BaseUtil.getMaterial(gunpowderStr);
        Material mushroomStew = BaseUtil.getMaterial(mushroomStewStr);

        identifyRecipe.shape("ABC", "DEF", "GHL");
        identifyRecipe.setIngredient('A', Material.APPLE);
        identifyRecipe.setIngredient('B', gunpowder);
        identifyRecipe.setIngredient('C', Material.WHEAT);
        identifyRecipe.setIngredient('D', Material.EMERALD);
        identifyRecipe.setIngredient('E', mushroomStew);
        identifyRecipe.setIngredient('F', Material.EMERALD);
        identifyRecipe.setIngredient('G', Material.CARROT);
        identifyRecipe.setIngredient('H', gunpowder);
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
        String mushroomStewStr = "MUSHROOM_STEW";
        if (versionId < VersionCheckEnum.V_1_13.getVersionId()) {
            mushroomStewStr = "MUSHROOM_SOUP";
        }
        ItemStack itemStack = new ItemStack(BaseUtil.getMaterial(mushroomStewStr));
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
        Integer versionId = VersionCheckEnum.getEnum().getVersionId();

        ItemStack itemStack = getItemStack();
        ShapedRecipe identifyRecipe;

        String mushroomStewStr = "MUSHROOM_STEW";
        if (versionId < VersionCheckEnum.V_1_13.getVersionId()) {
            mushroomStewStr = "MUSHROOM_SOUP";
        }

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
        identifyRecipe.setIngredient('E', BaseUtil.getMaterial(mushroomStewStr));
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

        String mushroomStewStr = "MUSHROOM_STEW";
        if (versionId < VersionCheckEnum.V_1_13.getVersionId()) {
            mushroomStewStr = "MUSHROOM_SOUP";
        }
        ItemStack itemStack = new ItemStack(BaseUtil.getMaterial(mushroomStewStr));
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
     * 获取种族帮助之书
     *
     * @return 种族帮助之书
     */
    public static ItemStack getVampireRaceHelpBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookmeta = (BookMeta) book.getItemMeta();
        // 调用  BookMeta 类的方法 setAuthor() 填写作者
        bookmeta.setAuthor("丶米饭");
        // 调用  BookMeta 类的方法 setTitle() 设置书的标题
        bookmeta.setTitle("玩家种族帮助手册");
        // 使用StringBuilder类快速便捷文本内容
        StringBuilder text = new StringBuilder();
        text.append("吸血鬼\n一种害怕阳光,但是拥有强大力量的黑暗生物。\n");
        text.append("始祖来源： 使用道具 **该隐之血** , 第一个使用的人类,将会变为吸血鬼始祖。\n");
        text.append("后裔来源:  人类被吸血鬼击杀会被转换为吸血鬼,但是种族之力会降低。\n");
        text.append("种族天赋:\n");
        text.append("1. 等级加成: 血统越纯净的吸血鬼,能量值越高\n");
        text.append("2. 时间加成: 越古老的吸血鬼, 攻守能力加成越高\n");
        text.append("3. 会随着时间缓慢恢复能量值\n");
        text.append("被动技能:\n");
        text.append("1. 水下伤害减少\n");
        text.append("2. 快速回血\n");
        text.append("3. 近战攻击力提升\n");
        text.append("4. 防御力提升\n");
        text.append("5. 与一切怪物休战\n");
        text.append("6. 掉落无伤\n");
        text.append("主动技能:\n");
        text.append("1. 手持红石粉左击 消耗一定能量值,可以对视线范围内上一次攻击的玩家进行吸血(不会导致对手死亡)\n");
        text.append("种族弱点:\n");
        text.append("1. 阳光下不带金头盔会被燃烧\n");
        text.append("2. 受到木制武器的伤害大幅提升\n");
        text.append("3. 只能吃 生肉 来吸血生存,其他任何食物都无法下嘴\n");
        //将这些内容写入新的一页
        //注意，每页最多256个字符，每本书最多50页
        bookmeta.addPage(text.toString());
        //设置书的类型为原著
        bookmeta.setGeneration(BookMeta.Generation.ORIGINAL);
        //完成BookMeta编写
        book.setItemMeta(bookmeta);
        return book;
    }

}
