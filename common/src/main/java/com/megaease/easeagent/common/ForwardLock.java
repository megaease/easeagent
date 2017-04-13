package com.megaease.easeagent.common;

import com.google.common.collect.Maps;

import java.util.Map;

public class ForwardLock {
    private static final ThreadLocal<Map<Object, Object>> MARK = new ThreadLocal<Map<Object, Object>>() {
        @Override
        protected Map<Object, Object> initialValue() {
            return Maps.newHashMap();
        }
    };

    public boolean acquire(Object token) {
        final Map<Object, Object> map = MARK.get();
        if (map.containsKey(this)) return false;
        map.put(this, token);
        return true;
    }

    public boolean release(Object token) {
        final Map<Object, Object> map = MARK.get();
        if (token != map.get(this)) return false;
        map.remove(this);
        return true;
    }
}
