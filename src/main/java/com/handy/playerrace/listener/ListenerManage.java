package com.handy.playerrace.listener;

import com.handy.playerrace.PlayerRace;

/**
 * @author hs
 * @Description: {}
 * @date 2020/8/10 11:44
 */
public class ListenerManage {
    /**
     * 监听器注册
     *
     * @param plugin 插件
     */
    public static void enableListener(PlayerRace plugin) {
        // 玩家进入服务器事件初始化信息.
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(), plugin);
        // 玩家退出服务器事件清理缓存
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuitEventListener(), plugin);

        // 狼人相关事件
        plugin.getServer().getPluginManager().registerEvents(new WerWolfEventListener(), plugin);
        // 吸血鬼相关事件
        plugin.getServer().getPluginManager().registerEvents(new VampireEventListener(), plugin);
        // 食尸鬼相关事件
        plugin.getServer().getPluginManager().registerEvents(new GhoulEventListener(), plugin);
    }

}
