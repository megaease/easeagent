package com.megaease.easeagent;

import com.google.auto.service.AutoService;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public abstract class EventBus {
    public static final BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1024);

    public static boolean publish(Object event) {
        return queue.offer(event);
    }

    private EventBus() { }
}
