package cn.handyplus.race.util;

import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.core.StrUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.ItemStackUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.PlayerRace;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 禁止的世界
 *
 * @author handy
 */
public class RaceUtil {

    /**
     * 是否为禁止的世界
     *
     * @param player 玩家
     * @return true 禁止
     * @since 1.3.3
     */
    public static boolean isWorld(Player player) {
        List<String> noWorld = ConfigUtil.CONFIG.getStringList("noWorld");
        return CollUtil.isNotEmpty(noWorld) && noWorld.contains(player.getWorld().getName());
    }

    /**
     * 发送提醒消息
     *
     * @param playerName 玩家
     * @param raceType   种族类型
     */
    public static void sendRaceMsg(String playerName, String raceType) {
        String raceMsg = BaseUtil.getMsgNotColor("raceMsg");
        raceMsg = StrUtil.replace(raceMsg, "player", playerName);
        raceMsg = StrUtil.replace(raceMsg, "race", raceType);
        MessageUtil.sendAllMessage(raceMsg);
    }

    /**
     * 能量不足
     *
     * @param amount 消耗值
     * @return msg
     */
    public static String getEnergyShortageMsg(Integer amount) {
        Map<String, String> map = new HashMap<>();
        map.put("amount", amount.toString());

        String actionBarMsg = ConfigUtil.LANG_CONFIG.getString("energyShortageMsg");
        if (actionBarMsg == null || actionBarMsg.isEmpty()) {
            return "";
        }
        for (String str : map.keySet()) {
            actionBarMsg = actionBarMsg.replace("${" + str + "}", map.get(str));
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
        // 判断是否为对应种族
        RacePlayer racePlayer = CacheUtil.getRacePlayer(player.getUniqueId());
        if (!raceTypeEnum.getType().equals(racePlayer.getRaceType())) {
            return;
        }
        int maxFatigue = ConfigUtil.CONFIG.getInt("maxFatigue");
        if (racePlayer.getMaxAmount() != null && racePlayer.getMaxAmount() != 0) {
            maxFatigue = racePlayer.getMaxAmount();
        }
        // 吸血鬼计算最大值
        if (RaceTypeEnum.VAMPIRE.getType().equals(racePlayer.getRaceType())) {
            double energyDiscount = ConfigUtil.RACE_CONFIG.getDouble("vampire.energyDiscount" + racePlayer.getRaceLevel());
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
        boolean rst = CacheUtil.add(player, num);
        if (rst) {
            String restoreEnergyMsg = ConfigUtil.LANG_CONFIG.getString("restoreEnergyMsg");
            if (StrUtil.isNotEmpty(restoreEnergyMsg)) {
                restoreEnergyMsg = restoreEnergyMsg.replace("${amount}", amount + "");
            }
            MessageUtil.sendActionbar(player, BaseUtil.replaceChatColor(restoreEnergyMsg));
        }
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
            identifyRecipe = new ShapedRecipe(new NamespacedKey(PlayerRace.INSTANCE, "mengBorneoSoup"), itemStack);
        }
        Material gunpowder = ItemStackUtil.getMaterial(gunpowderStr);
        Material mushroomStew = ItemStackUtil.getMaterial(mushroomStewStr);

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
        ItemStack itemStack = new ItemStack(ItemStackUtil.getMaterial(mushroomStewStr));
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }
        String name = ConfigUtil.RACE_CONFIG.getString("mankind.name");
        itemMeta.setDisplayName(BaseUtil.replaceChatColor(name != null ? name : ""));
        List<String> lores = new ArrayList<>();
        List<String> loreList = ConfigUtil.RACE_CONFIG.getStringList("mankind.lores");
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
        bookMeta.addRecipe(new NamespacedKey(PlayerRace.INSTANCE, "mengBorneoSoup"));
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
            identifyRecipe = new ShapedRecipe(new NamespacedKey(PlayerRace.INSTANCE, "vampire"), itemStack);
        }
        identifyRecipe.shape("ABC", "DEF", "ABC");
        identifyRecipe.setIngredient('A', Material.EMERALD);
        identifyRecipe.setIngredient('B', Material.DIAMOND);
        identifyRecipe.setIngredient('C', Material.EMERALD);
        identifyRecipe.setIngredient('D', Material.NETHER_STAR);
        identifyRecipe.setIngredient('E', ItemStackUtil.getMaterial(mushroomStewStr));
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
        ItemStack itemStack = new ItemStack(ItemStackUtil.getMaterial(mushroomStewStr));
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }
        String name = ConfigUtil.RACE_CONFIG.getString("vampire.name");
        itemMeta.setDisplayName(BaseUtil.replaceChatColor(name != null ? name : ""));
        List<String> lores = new ArrayList<>();
        List<String> loreList = ConfigUtil.RACE_CONFIG.getStringList("vampire.lores");
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
        bookMeta.addRecipe(new NamespacedKey(PlayerRace.INSTANCE, "vampire"));
        //设置Meta数据
        book.setItemMeta(bookMeta);
        return book;
    }

    /**
     * 获取帮助之书
     *
     * @param raceTypeEnum raceTypeEnum
     * @return 帮助之书
     */
    public static ItemStack getRaceHelpBook(RaceTypeEnum raceTypeEnum) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setAuthor("§e Handy");
        bookMeta.setTitle("§e" + RaceTypeEnum.getDesc(raceTypeEnum.getType()));
        switch (raceTypeEnum) {
            case MANKIND:
                getWerWolfRaceHelpBook(bookMeta);
                getVampireRaceHelpBook(bookMeta);
                getGhoulRaceHelpBook(bookMeta);
                getDemonRaceHelpBook(bookMeta);
                getAngelRaceHelpBook(bookMeta);
                getDemonHunterRaceHelpBook(bookMeta);
                break;
            case WER_WOLF:
                getWerWolfRaceHelpBook(bookMeta);
                break;
            case VAMPIRE:
                getVampireRaceHelpBook(bookMeta);
                break;
            case GHOUL:
                getGhoulRaceHelpBook(bookMeta);
                break;
            case DEMON:
                getDemonRaceHelpBook(bookMeta);
                break;
            case ANGEL:
                getAngelRaceHelpBook(bookMeta);
                break;
            case DEMON_HUNTER:
                getDemonHunterRaceHelpBook(bookMeta);
                break;
            default:
                break;
        }
        //设置书的类型为原著
        if (VersionCheckEnum.getEnum().getVersionId() > VersionCheckEnum.V_1_8.getVersionId()) {
            bookMeta.setGeneration(BookMeta.Generation.ORIGINAL);
        }
        //完成BookMeta编写
        book.setItemMeta(bookMeta);
        return book;
    }

    /**
     * 获取吸血鬼帮助之书
     *
     * @param bookMeta bookMeta
     */
    private static void getVampireRaceHelpBook(BookMeta bookMeta) {
        String text = "§4§l吸血鬼\n\n" +
                "§1一种害怕阳光,但是拥有强大力量的黑暗生物。\n\n" +
                "§1§l始祖来源：\n" +
                "§1使用道具§4该隐之血§1,第一个使用的人类,将会变为吸血鬼始祖。\n\n" +
                "§1§l后裔来源: \n" +
                "§1人类被吸血鬼击杀会被转换为吸血鬼,但是种族之力会降低。\n";
        bookMeta.addPage(text);

        String text1 = "§1§l种族天赋:\n\n" +
                "§11.等级加成:血统越纯净的吸血鬼,能量值越高\n\n" +
                "§12.时间加成:越古老的吸血鬼,攻守能力加成越高\n\n" +
                "§13.会随着时间缓慢恢复能量值\n";
        bookMeta.addPage(text1);

        String text2 = "§1§l主动技能:\n\n" +
                "§11.手持§4红石粉§1左击消耗一定能量值,可以对视线范围内上一次攻击的玩家进行吸血(不会导致对手死亡)\n";
        bookMeta.addPage(text2);

        String text3 = "§1§l被动技能:\n\n" +
                "§11.水下伤害减少\n" +
                "§12.快速回血\n" +
                "§13.近战攻击力提升\n" +
                "§14.防御力提升\n" +
                "§15.与一切怪物休战\n" +
                "§16.掉落无伤\n";
        bookMeta.addPage(text3);

        String text4 = "§1§l种族弱点:\n\n" +
                "§11.阳光下不带§4金头盔§1会被燃烧\n" +
                "§12.受到§4木制武器§1的伤害大幅提升\n" +
                "§13.只能吃§4生肉§1来吸血生存,其他任何食物都无法下嘴\n";
        bookMeta.addPage(text4);
    }

    /**
     * 获取狼人帮助之书
     *
     * @param bookMeta bookMeta
     */
    private static void getWerWolfRaceHelpBook(BookMeta bookMeta) {
        String text = "§4§l狼人\n\n" +
                "§1一种在夜晚拥有强大力量的黑暗生物。\n\n" +
                "§1§l种族来源：\n" +
                "§1在夜黑风高之时,人类被§4狼§1击杀,将会转换成狼人。\n";
        bookMeta.addPage(text);

        String text1 = "§1§l种族天赋:\n\n" +
                "§11.晚上击杀生物会恢复能量值\n\n" +
                "§12.晚上吃§4生肉§1会恢复能量值\n\n" +
                "§13.会随着时间来极慢恢复能量值\n";
        bookMeta.addPage(text1);

        String text2 = "§1§l主动技能:\n\n" +
                "§11.召唤狼:手持§4生猪肉§1左击,消耗一定能量值,可以召唤§4狼\n";
        bookMeta.addPage(text2);

        String text3 = "§1§l被动技能:\n\n" +
                "§11.晚上会快速回血\n" +
                "§12.晚上近战攻击力提升\n" +
                "§13.晚上远程攻击力大幅度提升\n" +
                "§14.晚上掉落减伤\n";
        bookMeta.addPage(text3);

        String text4 = "§1§l种族弱点:\n\n" +
                "§11.狼人害怕§4阳光§1,在白天会失去大部分能力\n";
        bookMeta.addPage(text4);
    }

    /**
     * 获取食尸鬼帮助之书
     *
     * @param bookMeta 书信息
     */
    private static void getGhoulRaceHelpBook(BookMeta bookMeta) {
        String text = "§4§l食尸鬼\n\n" +
                "§1一种自愿放弃做人,拥有诡异力量的邪恶生物。\n\n" +
                "§1§l种族来源：\n" +
                "§1人类被猪人击杀转换而来。\n" +
                "§1人类被食尸鬼使用§4邪恶诅咒§1转换而来。\n";
        bookMeta.addPage(text);

        String text1 = "§1§l种族天赋:\n\n" +
                "§11.会随着时间正常恢复能量值\n\n";
        bookMeta.addPage(text1);

        String text2 = "§1§l主动技能:\n\n" +
                "§11.召唤猪人:手持§4金块§1左击,消耗一定能量值,可以召唤出极度贪婪的§4猪人\n" +
                "§12.邪恶诅咒:手持§4骨头§1打击人类,消耗一定能量值,可以诅咒该人类一段时间后被迫成为食尸鬼\n\n";
        bookMeta.addPage(text2);

        String text3 = "§1§l被动技能:\n\n" +
                "§11.与僵尸休战\n" +
                "§12.近战吸血(消耗能量)\n" +
                "§13.近战消耗敌人能量\n";
        bookMeta.addPage(text3);

        String text4 = "§1§l种族弱点:\n\n" +
                "§11.怕水\n";
        bookMeta.addPage(text4);
    }

    /**
     * 获取恶魔帮助之书
     *
     * @param bookMeta 书信息
     */
    private static void getDemonRaceHelpBook(BookMeta bookMeta) {
        String text = "§4§l恶魔\n\n" +
                "§1一种通过神秘仪式,转换为邪恶生物的种族。\n\n" +
                "§1§l种族来源：\n" +
                "§1人类身穿锁链套,在地狱葬身岩浆中转换而来。\n\n";
        bookMeta.addPage(text);

        String text1 = "§1§l种族天赋:\n\n" +
                "§11.会随着时间正常恢复能量值\n\n" +
                "§12.在岩浆中快速恢复能量\n\n";
        bookMeta.addPage(text1);

        String text2 = "§1§l主动技能:\n\n" +
                "§11.发射火焰弹 手持§4火焰弹§1左击,消耗能量,直接发射\n" +
                "§12. 生成蜘蛛网,手持§4墨囊§1左击,消耗能量,生成§4蜘蛛网§1保护自己\n\n";
        bookMeta.addPage(text2);

        String text3 = "§1§l被动技能:\n\n" +
                "§11.火焰免役\n" +
                "§12.岩浆免役\n" +
                "§13.在岩浆中快速恢复血量\n";
        bookMeta.addPage(text3);

        String text4 = "§1§l种族弱点:\n\n" +
                "§11.无法穿除了锁链以外的装备\n";
        bookMeta.addPage(text4);
    }

    /**
     * 获取天使帮助之书
     *
     * @param bookMeta 书信息
     */
    private static void getAngelRaceHelpBook(BookMeta bookMeta) {
        String text = "§4§l天使\n\n" +
                "§1一种通过在意外中,穿越为善良生物的种族。\n\n" +
                "§1§l种族来源：\n" +
                "§1 人类穿着皮革手拿羽毛,意外摔死....\n\n";
        bookMeta.addPage(text);

        String text1 = "§1§l种族天赋:\n\n" +
                "§11.会随着时间快速恢复能量值\n\n";
        bookMeta.addPage(text1);

        String text2 = "§1§l主动技能:\n\n" +
                "§11.咸鱼突刺,手持§4羽毛§1攻击,消耗能量,可以击飞\n" +
                "§12.召唤牛,手持§4小麦§1左击,消耗能量,召唤牛\n" +
                "§13.召唤猪,手持§4胡萝卜§1左击,消耗能量,召唤猪\n" +
                "§14.能量恢复,手持§4绿宝石§1点击玩家,可以消耗能量,给予对应玩家能量值\n" +
                "§15.血量恢复,手持§4面包§1点击玩家,可以消耗能量,治疗玩家\n";
        bookMeta.addPage(text2);

        String text3 = "§1§l被动技能:\n\n" +
                "§11.掉落无伤\n" +
                "§12.水中无伤\n";
        bookMeta.addPage(text3);

        String text4 = "§1§l种族弱点:\n\n" +
                "§11.无法伤害动物和人类\n" +
                "§12.只能穿皮革\n";
        bookMeta.addPage(text4);
    }

    /**
     * 获取恶魔猎手帮助之书
     *
     * @param bookMeta 书信息
     */
    private static void getDemonHunterRaceHelpBook(BookMeta bookMeta) {
        String text = "§4§l恶魔猎手\n\n" +
                "§1一种更强的人类,在猎杀超自然种族中,逐步释放潜力晋而成。\n\n" +
                "§1§l种族来源：\n" +
                "§1人类击杀多个其他种族进阶而来。\n\n";
        bookMeta.addPage(text);

        String text1 = "§1§l种族天赋:\n\n" +
                "§11.会随着时间缓慢恢复能量值\n\n";
        bookMeta.addPage(text1);

        String text2 = "§1§l主动技能:\n\n" +
                "§11.切换弓形态:手持弓左击空气,消耗一定能量值,可以切换弓的状态\n";
        bookMeta.addPage(text2);

        String text3 = "§1§l被动技能:\n\n" +
                "§11.力量弓:增加伤害\n" +
                "§12.火焰弓:燃烧敌人\n" +
                "§13.禁锢弓:在敌人脚下生成蜘蛛网\n";
        bookMeta.addPage(text3);

        String text4 = "§1§l种族弱点:\n\n" +
                "§1无\n";
        bookMeta.addPage(text4);
    }

}