package com.megaease.easeagent;

import lombok.EqualsAndHashCode;

import java.lang.reflect.Method;

/** A invocation wrapper of {@link Method} */
@EqualsAndHashCode
class Consumer {
    final Method method;
    final Object target;

    Consumer(Method method, Object target) {
        this.method = method;
        this.target = target;
    }

    void accept(Object event) {
        try {
            method.invoke(target, event);
        } catch (Exception ex) {
            throw new MayBeABug(ex);
        }
    }

    @Override
    public String toString() {
        return "Consumer{method=" + method + '}';
    }
}
