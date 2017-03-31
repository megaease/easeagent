package com.megaease.easeagent;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

// TODO use annotation processor to improve performance.
class EventDispatcher implements Runnable {

    private final Map<Class<?>, Consumer> consumers;
    private final Executor                executor;

    public EventDispatcher(Map<Class<?>, Consumer> consumers, Executor executor) {
        this.consumers = consumers;
        this.executor = executor;
    }

    @Override
    public void run() {
        while (true) {
            try {
                final Object e = EventBus.queue.poll(100, MILLISECONDS);

                if (e == null) continue;

                List<Object> events = new LinkedList<Object>();

                events.add(e);

                EventBus.queue.drainTo(events);

                for (final Object event : events) {
                    final Consumer consumer = consumers.get(event.getClass());
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            consumer.accept(event);
                        }
                    });
                }
            } catch (InterruptedException ignore) {
                return;
            }
        }

    }


}
