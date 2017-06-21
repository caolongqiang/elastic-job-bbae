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

package com.dangdang.ddframe.job.event.jmonitor;

import com.dangdang.ddframe.job.event.JobEventListener;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.jimu.common.jmonitor.JMonitor;
import lombok.extern.slf4j.Slf4j;

/**
 * 记录jmonitor.
 *
 * @author zhenbao.zhouzhou
 */
@Slf4j
public final class JobEventJMonitorListener extends JobEventJMonitorIdentity implements JobEventListener {

    private final static String PREFIX = "ElasticJOB_";
    private final static String SUCCESS = "_success";
    private final static String FAIL = "_fail";

    @Override
    public void listen(final JobExecutionEvent executionEvent) {
        String key = PREFIX + executionEvent.getNameSpace() + "_" +executionEvent.getJobName();
        if (null == executionEvent.getCompleteTime()) {
            // 刚开始, 我就不记了
        } else {
            if (executionEvent.isSuccess()) {
                log.info("key:" + (key+SUCCESS));
                log.info("time:" + (executionEvent.getCompleteTime().getTime() - executionEvent.getStartTime().getTime()));
                JMonitor.recordOne(key + SUCCESS, executionEvent.getCompleteTime().getTime() - executionEvent.getStartTime().getTime());
            } else {
                JMonitor.recordOne(key + FAIL, executionEvent.getCompleteTime().getTime() - executionEvent.getStartTime().getTime());
            }
        }
    }

    @Override
    public void listen(final JobStatusTraceEvent jobStatusTraceEvent) {
        // 执行中的状态, 不需要记录
    }
}
