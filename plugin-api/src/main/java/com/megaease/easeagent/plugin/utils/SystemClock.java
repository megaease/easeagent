/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.utils;

import com.megaease.easeagent.plugin.concurrent.AgentThreadFactory;

import java.sql.Timestamp;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Optimization of System.currentTimeMillis() performance problem in high concurrency scenario
 *
 * reference:
 * https://pzemtsov.github.io/2017/07/23/the-slow-currenttimemillis.html
 * https://programmer.group/5e85bd0cc8b52.html
 * </p>
 */
public class SystemClock {
    // update frequency
    private final long period;
    // currentTimeMillis
    private final AtomicLong now;

    private SystemClock(long period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    private static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    public static long now() {
        return instance().currentTimeMillis();
    }

    @SuppressWarnings("unused")
    public static String nowDate() {
        return new Timestamp(instance().currentTimeMillis()).toString();
    }

    private void scheduleClockUpdating() {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1,
            new AgentThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "System Clock");
                thread.setDaemon(true);
                return thread;
            }
        });

        scheduler.scheduleAtFixedRate(
            () -> now.set(System.currentTimeMillis()),
            period, period, TimeUnit.MILLISECONDS);
    }

    private long currentTimeMillis() {
        return now.get();
    }

    private static class InstanceHolder {
        public static final SystemClock INSTANCE = new SystemClock(1);
    }
}
