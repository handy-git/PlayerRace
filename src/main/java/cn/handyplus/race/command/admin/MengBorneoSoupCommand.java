package cn.handyplus.race.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.AssertUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.util.RaceUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author handy
 **/
public class MengBorneoSoupCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "getMengBorneoSoup";
    }

    @Override
    public String permission() {
        return "playerRace.getMengBorneoSoup";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = AssertUtil.notPlayer(sender, BaseUtil.getLangMsg("noPlayerFailureMsg"));
        player.getInventory().addItem(RaceUtil.getMengBorneoSoup());
        MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("succeedMsg"));
    }

}
