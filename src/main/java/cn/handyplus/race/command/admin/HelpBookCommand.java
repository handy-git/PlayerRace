package cn.handyplus.race.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.AssertUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.util.CacheUtil;
import cn.handyplus.race.util.RaceUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author handy
 **/
public class HelpBookCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "getHelpBook";
    }

    @Override
    public String permission() {
        return "playerRace.getHelpBook";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = AssertUtil.notPlayer(sender, BaseUtil.getLangMsg("noPlayerFailureMsg"));
        // 判断种族
        RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnumThrow(CacheUtil.getRacePlayer(player.getUniqueId()).getRaceType());
        player.getInventory().addItem(RaceUtil.getRaceHelpBook(raceTypeEnum));
        MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("succeedMsg"));
    }

}
