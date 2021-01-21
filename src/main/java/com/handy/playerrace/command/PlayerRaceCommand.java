package com.handy.playerrace.command;

import com.handy.lib.util.BaseUtil;
import com.handy.playerrace.command.admin.*;
import com.handy.playerrace.constants.TabListEnum;
import com.handy.playerrace.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/10 11:44
 */
public class PlayerRaceCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 判断指令是否正确
        if (args.length < 1) {
            return sendHelp(sender);
        }
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("playerrace.reload")) {
                    sender.sendMessage(BaseUtil.getLangMsg("noPermission"));
                    return true;
                }
                ReloadCommand.getSingleton().onCommand(sender, cmd, label, args);
                break;
            case "setrace":
                if (!sender.hasPermission("playerrace.setrace")) {
                    sender.sendMessage(BaseUtil.getLangMsg("noPermission"));
                    return true;
                }
                SetRaceCommand.getSingleton().onCommand(sender, cmd, label, args);
                break;
            case "gethelpbook":
                if (!sender.hasPermission("playerrace.gethelpbook")) {
                    sender.sendMessage(BaseUtil.getLangMsg("noPermission"));
                    return true;
                }
                HelpBookCommand.getSingleton().onCommand(sender, cmd, label, args);
                break;
            case "getmengborneosoup":
                if (!sender.hasPermission("playerrace.getmengborneosoup")) {
                    sender.sendMessage(BaseUtil.getLangMsg("noPermission"));
                    return true;
                }
                MengBorneoSoupCommand.getSingleton().onCommand(sender, cmd, label, args);
                break;
            case "findcount":
                if (!sender.hasPermission("playerrace.findcount")) {
                    sender.sendMessage(BaseUtil.getLangMsg("noPermission"));
                    return true;
                }
                FindCountCommand.getSingleton().onCommand(sender, cmd, label, args);
                break;
            default:
                return sendHelp(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
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
     * @param sender
     * @return
     */
    private Boolean sendHelp(CommandSender sender) {
        if (!sender.hasPermission("playerrace.reload")) {
            return true;
        }

        List<String> helps = ConfigUtil.langConfig.getStringList("helps");
        for (String help : helps) {
            sender.sendMessage(BaseUtil.replaceChatColor(help));
        }
        return true;
    }

}
