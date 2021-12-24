/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
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
 */

package com.megaease.easeagent.plugin.async;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ScheduleHelper {
    public static final ScheduleHelper DEFAULT = new ScheduleHelper();

    private final ThreadFactory threadFactory = new AgentThreadFactory();
    private ScheduledExecutorService scheduleService = Executors.newSingleThreadScheduledExecutor(threadFactory);

    public void nonStopExecute(int initialDelay, int delay, Runnable command) {
        Executors.newSingleThreadScheduledExecutor(threadFactory)
            .scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.SECONDS);
    }

    public void execute(int initialDelay, int delay, Runnable command) {
        this.scheduleService.scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.SECONDS);
    }

    public void shutdown() {
        this.scheduleService.shutdown();
    }
}
