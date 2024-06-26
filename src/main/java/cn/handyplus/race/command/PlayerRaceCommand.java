package cn.handyplus.race.command;

import cn.handyplus.lib.annotation.HandyCommand;
import cn.handyplus.lib.command.HandyCommandWrapper;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.TabListEnum;
import cn.handyplus.race.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 命令
 *
 * @author handy
 */
@HandyCommand(name = "PlayerRace")
public class PlayerRaceCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 判断指令是否正确
        if (args.length < 1) {
            return sendHelp(sender);
        }
        boolean rst = HandyCommandWrapper.onCommand(sender, cmd, label, args, BaseUtil.getLangMsg("noPermission"));
        if (!rst) {
            return sendHelp(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("playerrace.reload")) {
            return completions;
        }
        List<String> commands = TabListEnum.returnList(args, args.length, sender);
        if (commands == null) {
            return null;
        }
        StringUtil.copyPartialMatches(args[args.length - 1].toLowerCase(), commands, completions);
        Collections.sort(completions);
        return completions;
    }

    /**
     * 发送帮助
     *
     * @param sender 发送人
     * @return ture
     */
    private Boolean sendHelp(CommandSender sender) {
        if (!sender.hasPermission("playerrace.reload")) {
            return true;
        }
        List<String> helps = ConfigUtil.LANG_CONFIG.getStringList("helps");
        for (String help : helps) {
            MessageUtil.sendMessage(sender, BaseUtil.replaceChatColor(help));
        }
        return true;
    }

}
