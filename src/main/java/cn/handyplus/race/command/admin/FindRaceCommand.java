package cn.handyplus.race.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.race.constants.RaceTypeEnum;
import cn.handyplus.race.entity.RacePlayer;
import cn.handyplus.race.util.CacheUtil;
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
            RacePlayer racePlayer = CacheUtil.getRacePlayer(offlinePlayer.getUniqueId());
            RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnumThrow(racePlayer.getRaceType());
            MessageUtil.sendMessage(sender, args[1] + ": " + RaceTypeEnum.getDesc(raceTypeEnum.getType()));
            return;
        }
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("succeedMsg"));
            return;
        }
        // 查询全部在线玩家
        for (Player player : Bukkit.getOnlinePlayers()) {
            RaceTypeEnum raceTypeEnum = RaceTypeEnum.getEnumThrow(CacheUtil.getRacePlayer(player.getUniqueId()).getRaceType());
            MessageUtil.sendMessage(sender, player.getName() + ": " + RaceTypeEnum.getDesc(raceTypeEnum.getType()));
        }
    }

}
