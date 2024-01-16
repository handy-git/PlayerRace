package cn.handyplus.race.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.service.RacePlayerService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author handy
 **/
public class FindRaceCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "findRace";
    }

    @Override
    public String permission() {
        return "playerRace.findRace";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 2) {
            OfflinePlayer offlinePlayer = BaseUtil.getOfflinePlayer(args[1]);
            RaceTypeEnum anEnum = RaceTypeEnum.getEnum(RacePlayerService.getInstance().findRaceType(offlinePlayer.getUniqueId()));
            if (anEnum == null) {
                MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("typeFailureMsg"));
            } else {
                MessageUtil.sendMessage(sender, args[1] + ": " + anEnum.getTypeName());
            }
            return;
        }
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("succeedMsg"));
            return;
        }
        // 查询全部在线玩家
        for (Player player : Bukkit.getOnlinePlayers()) {
            RaceTypeEnum anEnum = RaceTypeEnum.getEnum(RacePlayerService.getInstance().findRaceType(player.getUniqueId()));
            if (anEnum != null) {
                MessageUtil.sendMessage(sender, player.getName() + ": " + anEnum.getTypeName());
            }
        }
    }

}
