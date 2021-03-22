package com.megaease.easeagent.config;

import java.util.function.BiFunction;

public class AutoRefreshConfigItem<T> {
    private volatile T value;

    public AutoRefreshConfigItem(Config config, String name, BiFunction<Config, String, T> func) {
        ConfigUtils.bindProp(name, config, func, v -> this.value = v);
    }

    public T getValue() {
        return value;
    }
}
