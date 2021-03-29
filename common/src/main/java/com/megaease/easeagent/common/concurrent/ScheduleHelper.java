package com.megaease.easeagent.common.concurrent;

import com.megaease.easeagent.core.AgentThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ScheduleHelper {

    public static final ScheduleHelper DEFAULT = new ScheduleHelper();

    private final ThreadFactory threadFactory = new AgentThreadFactory();

    public void execute(int initialDelay, int delay, Runnable command) {
        Executors.newSingleThreadScheduledExecutor(threadFactory).scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.SECONDS);
    }

}
