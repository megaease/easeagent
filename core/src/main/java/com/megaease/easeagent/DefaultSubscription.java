package com.megaease.easeagent;

import java.lang.reflect.Method;
import java.util.Map;

class DefaultSubscription implements Subscription {

    private final Map<Class<?>, Consumer> consumers;

    DefaultSubscription(Map<Class<?>, Consumer> consumers) {
        this.consumers = consumers;
    }

    @Override
    public void register(Object host) {
        for (final Method method : host.getClass().getMethods()) {

            final Subscription.Consume a = method.getAnnotation(Subscription.Consume.class);

            if (a == null) continue;

            final Class<?>[] types = method.getParameterTypes();

            if (types.length != 1) {
                throw new MayBeABug("Only one parameter can be declared on " + method);
            }

            final Class<?> key = types[0];
            if (consumers.containsKey(key)) {
                final Consumer c = consumers.get(key);
                throw new MayBeABug("Duplicated subscription between " + c.method + " and " + method);
            }

            consumers.put(key, new Consumer(method, host));
        }
    }
}
