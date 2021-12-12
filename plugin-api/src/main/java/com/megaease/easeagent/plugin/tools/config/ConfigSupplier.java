package com.megaease.easeagent.plugin.tools.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * a AutoRefreshConfig Supplier
 *
 * @param <T> the type of Config by this Supplier
 */
public interface ConfigSupplier<T extends AutoRefreshConfig> {
    /**
     * the type of AutoRefreshConfig
     *
     * @return {@link Type}
     */
    default Type getType() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }
        return ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }


    /**
     * new a AutoRefreshConfig
     *
     * @return AutoRefreshConfig
     */
    T newInstance();
}
