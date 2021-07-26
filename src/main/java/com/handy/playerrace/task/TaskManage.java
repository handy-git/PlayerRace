package com.handy.playerrace.task;

/**
 * @author hs
 */
public class TaskManage {

    /**
     * 定时任务注册
     */
    public static void enableTask() {
        // 运行异步自动恢复能量
        RecoveryEnergyTask.setRecoveryFatigueTask();

        // 运行异步烧吸血鬼
        VampireCombustTask.setVampireCombustTask();

        // 运行异步食尸鬼害怕水
        GhoulWaterDamageTask.setWaterDamageTask();

        // 运行异步转换种族
        PlayerCursesTask.setPlayerCursesTask();

        // 同步定时清理生成出来的蜘蛛网
        ItemClearTask.setItemClearTask();
    }

}
