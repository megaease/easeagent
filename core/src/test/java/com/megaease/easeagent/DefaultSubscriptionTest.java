package com.megaease.easeagent;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DefaultSubscriptionTest {

    @Test
    public void should_call_receive_of_bar() throws Exception {
        final Bar bar = mock(Bar.class);
        final Foo foo = new Foo(bar);

        final HashMap<Class<?>, Consumer> consumers = Maps.newHashMap();
        final DefaultSubscription subscription = new DefaultSubscription(consumers);
        subscription.register(foo);

        final String event = "hi";
        consumers.get(String.class).accept(event);
        verify(bar).receive(event);
    }

    static class Foo {
        final Bar bar;

        Foo(Bar bar) {this.bar = bar;}

        @Subscription.Consume
        public void receive(String event) {
            bar.receive(event);
        }
    }

    interface Bar {
        void receive(String event);
    }
}