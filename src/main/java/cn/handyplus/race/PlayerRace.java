package cn.handyplus.race;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 主类
 *
 * @author handy
 */
public final class PlayerRace extends JavaPlugin {
    public static PlayerRace INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
    }

}