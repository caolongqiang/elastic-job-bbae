package com.dangdang.ddframe.job.lite.internal.schedule;

import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.election.LeaderService;
import com.dangdang.ddframe.job.lite.internal.instance.InstanceService;
import com.dangdang.ddframe.job.lite.internal.sharding.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingNode;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.plugins.management.ShutdownHookPlugin;
import org.quartz.spi.ClassLoadHelper;

/**
 * 作业关闭钩子.
 *
 * @author zhangliang
 */
@Slf4j
public final class JobShutdownHookPlugin extends ShutdownHookPlugin {

    private String jobName;


    @Override
    public void initialize(final String name, final Scheduler scheduler, final ClassLoadHelper classLoadHelper) throws SchedulerException {
//        super.initialize(name, scheduler, classLoadHelper);
        jobName = scheduler.getSchedulerName();

        getLog().info("Registering Quartz shutdown hook.");

        Thread t = new Thread("Quartz Shutdown-Hook "
            + scheduler.getSchedulerName()) {
            @Override
            public void run() {
                getLog().info("Shutting down Quartz...");
                try {
                    shutdown();
                    scheduler.shutdown(isCleanShutdown());
                } catch (SchedulerException e) {
                    getLog().info(
                        "Error shutting down Quartz: " + e.getMessage(), e);
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(t);

    }

    @Override
    public void shutdown() {
        CoordinatorRegistryCenter regCenter = JobRegistry.getInstance().getRegCenter(jobName);
        if (null == regCenter) {
            return;
        }

        JobNodePath jobNodePath = new JobNodePath(jobName);

        ConfigurationService configurationService = new ConfigurationService(regCenter, jobName);
        LiteJobConfiguration liteJobConfiguration = configurationService.load(true);
        int count = liteJobConfiguration.getTypeConfig().getCoreConfig().getShardingTotalCount();
        for(Integer item = 0 ; item < count; item++){
            if(regCenter.isExisted(jobNodePath.getShardingNodePath(String.valueOf(item), ShardingNode.RUNNING_APPENDIX))){
//                log.error("********************{}-{}**************************", jobName, item);
                log.error("Exception: job:{}-{}-{}, is runing and killed! ", regCenter.getNameSpaceOnly(), jobName, item);
            }
        }

        LeaderService leaderService = new LeaderService(regCenter, jobName);
        if (leaderService.isLeader()) {
            leaderService.removeLeader();
        }

        new InstanceService(regCenter, jobName).removeInstance();
    }
}
