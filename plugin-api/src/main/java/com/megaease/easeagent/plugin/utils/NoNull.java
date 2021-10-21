package com.megaease.easeagent.plugin.utils;

import java.util.function.Function;

public class NoNull {
    public static <O> O of(O o, O defaultValue) {
        return o == null ? defaultValue : o;
    }

    public static <T, R> R newOf(Function<T, R> f, T t, R defaultValue) {
        return t == null ? defaultValue : f.apply(t);
    }

}
