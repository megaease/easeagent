package com.megaease.easeagent.plugin.api.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * a AutoRefreshConfig Supplier
 *
 * @param <T> the type of Config by this Supplier
 */
public abstract class AutoRefreshConfigSupplier<T extends AutoRefreshConfig> {
    private final Type type;

    public AutoRefreshConfigSupplier() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    /**
     * the type of AutoRefreshConfig
     *
     * @return {@link Type}
     */
    public Type getType() {
        return type;
    }


    /**
     * new a AutoRefreshConfig
     *
     * @return AutoRefreshConfig
     */
    public abstract T newInstance();
}
