package com.hexdecteam.easeagent;

import java.util.Iterator;

public abstract class ReduceF {

    public static <T> T reduce(Iterator<T> iterator, BiFunction<T> function) {
        T t = null;
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (t == null) t = next;
            else t = function.apply(t, next);
        }
        return t;
    }

    public interface BiFunction<T> {
        T apply(T l, T r);
    }

    private ReduceF() { }
}
