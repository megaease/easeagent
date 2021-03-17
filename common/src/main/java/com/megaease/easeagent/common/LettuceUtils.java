package com.megaease.easeagent.common;

import io.lettuce.core.RedisURI;

public class LettuceUtils {

    public static boolean checkRedisUriInfo(Object data) {
        if (data instanceof RedisURI) {
            return true;
        }
        if (!(data instanceof Iterable)) {
            return false;
        }
        Iterable<?> it = (Iterable<?>) data;
        for (Object o : it) {
            if (!(o instanceof RedisURI)) {
                return false;
            }
        }
        return true;
    }

    public static RedisURI getOneRedisURI(Object data) {
        if (data instanceof RedisURI) {
            return (RedisURI) data;
        }
        Iterable<?> it = (Iterable<?>) data;
        for (Object o : it) {
            return (RedisURI) o;
        }
        return null;
    }
}
