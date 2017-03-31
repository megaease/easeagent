package com.megaease.easeagent;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EventDispatcherTest {

    @Test
    public void should_dispatch_event() throws Exception {
        final String event = "event";
        final Consumer consumer = mock(Consumer.class);
        final Map<Class<?>, Consumer> consumers = ImmutableMap.<Class<?>, Consumer>of(String.class, consumer);
        final EventDispatcher dispatcher = new EventDispatcher(consumers, new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        });

        EventBus.publish(event);

        final Thread thread = new Thread(dispatcher);

        thread.start();
        Thread.sleep(10);
        thread.interrupt();
        thread.join();

        verify(consumer).accept(event);

    }
}