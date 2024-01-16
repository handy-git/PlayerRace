package cn.handyplus.race.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.AssertUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.service.RacePlayerService;
import cn.handyplus.race.util.CacheUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author handy
 */
public class SetRaceCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "setRace";
    }

    @Override
    public String permission() {
        return "playerRace.setRace";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        AssertUtil.notTrue(args.length < 3, sender, BaseUtil.getLangMsg("paramFailureMsg"));
        // 类型
        RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnumThrow(args[2]);
        // 设置玩家种族
        OfflinePlayer offlinePlayer = BaseUtil.getOfflinePlayer(args[1]);
        Boolean rst = RacePlayerService.getInstance().updateRaceType(offlinePlayer.getUniqueId(), raceTypeEnum.getType());
        if (rst) {
            MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("succeedMsg"));
            Optional<Player> onlinePlayer = BaseUtil.getOnlinePlayer(args[1]);
            onlinePlayer.ifPresent(CacheUtil::db2Cache);
        } else {
            MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("failureMsg"));
        }
    }

}
