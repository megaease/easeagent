package com.megaease.easeagent.log4j2.api;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Mdc {
    private BiFunction<String, String, Void> putFuntion;
    private Function<String, Void> removeFuntion;
    private Function<String, String> getFuntion;

    public Mdc(@Nonnull BiFunction<String, String, Void> putFuntion, @Nonnull Function<String, Void> removeFuntion, @Nonnull Function<String, String> getFuntion) {
        this.putFuntion = putFuntion;
        this.removeFuntion = removeFuntion;
        this.getFuntion = getFuntion;
    }

    public void put(String key, String value) {
        putFuntion.apply(key, value);
    }

    public void remove(String key) {
        removeFuntion.apply(key);
    }

    public String get(String key) {
        return getFuntion.apply(key);
    }
}
