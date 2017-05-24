/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.api;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.listener.fixture.ElasticJobListenerCaller;
import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.TestSimpleJob;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.schedule.SchedulerFacade;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.unitils.util.ReflectionUtils;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JobSchedulerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private SchedulerFacade schedulerFacade;
    
    @Mock
    private ElasticJobListenerCaller caller;
    
    private LiteJobConfiguration liteJobConfig;
    
    private JobScheduler jobScheduler;
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        liteJobConfig = LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "* * 0/10 * * ? 2050", 3).build(), TestSimpleJob.class.getCanonicalName())).build();
        jobScheduler = new JobScheduler(regCenter, liteJobConfig);
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobScheduler, "regCenter", regCenter);
        ReflectionUtils.setFieldValue(jobScheduler, "schedulerFacade", schedulerFacade);
    }
    
    @Test
    public void assertInit() throws NoSuchFieldException, SchedulerException {
        when(schedulerFacade.updateJobConfiguration(liteJobConfig)).thenReturn(liteJobConfig);
        jobScheduler.init();
        verify(schedulerFacade).registerStartUpInfo(true);
        Scheduler scheduler = ReflectionUtils.getFieldValue(JobRegistry.getInstance().getJobScheduleController("test_job"), JobScheduleController.class.getDeclaredField("scheduler"));
        assertTrue(scheduler.isStarted());
    }
}
