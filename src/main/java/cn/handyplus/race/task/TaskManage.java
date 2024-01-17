package cn.handyplus.race.task;

/**
 * 定时任务注册
 *
 * @author handy
 */
public class TaskManage {

    public static void start() {
        // 运行异步自动恢复能量
        RecoveryEnergyTask.start();

        // 运行异步烧吸血鬼
        VampireCombustTask.start();

        // 运行异步食尸鬼害怕水
        GhoulWaterDamageTask.start();

        // 运行异步转换种族
        PlayerCursesTask.start();

        // 同步定时清理生成出来的蜘蛛网
        ItemClearTask.start();

        // 同步缓存到数据库
        Cache2DbTask.start();

        // 恶魔禁止锁链以外装备
        DemonArmorCheckTask.start();

        // 天使禁止皮革以外装备
        AngelArmorCheckTask.start();
    }

}
