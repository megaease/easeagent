package com.megaease.easeagent.plugin.api.metric;

import com.megaease.easeagent.plugin.api.metric.name.NameFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A {@link NameFactory} Supplier
 *
 * @param <T> the type of ServiceMetric by this Supplier
 */
public abstract class ServiceMetricSupplier<T extends ServiceMetric> {

    private final Type type;

    public ServiceMetricSupplier() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    /**
     * the type of ServiceMetric
     *
     * @return {@link Type}
     */
    public Type getType() {
        return type;
    }

    public abstract NameFactory newNameFactory();

    /**
     * new a ServiceMetric
     *
     * @param metricRegistry {@link MetricRegistry}
     * @param nameFactory    {@link NameFactory}
     * @return a type of ServiceMetric
     */
    public abstract T newInstance(MetricRegistry metricRegistry, NameFactory nameFactory);
}
