package com.megaease.easeagent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DaemonExecutors {

    public static ExecutorService newCached(String name) {
        return Executors.newCachedThreadPool(new NamedDaemonThreadFactory(name));
    }

    public static ScheduledExecutorService newScheduled(String name, int coreSize) {
        return Executors.newScheduledThreadPool(coreSize, new NamedDaemonThreadFactory(name));
    }

    public static <T extends ExecutorService> T shutdownAware(final T service) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                service.shutdown();
            }
        }));
        return service;
    }

    private DaemonExecutors() { }

    private static class NamedDaemonThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String name;

        public NamedDaemonThreadFactory(String name) {
            this.name = name;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, name + threadNumber.getAndIncrement(), 0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
