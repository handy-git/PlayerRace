package cn.handyplus.race.task;

import cn.handyplus.lib.expand.adapter.HandySchedulerUtil;
import cn.handyplus.race.util.CacheUtil;

/**
 * 同步缓存到数据库
 *
 * @author handy
 */
public class Cache2DbTask {

    public static void start() {
        HandySchedulerUtil.runTaskTimerAsynchronously(CacheUtil::cache2Db, 0, 20 * 60 * 10);
    }

}
