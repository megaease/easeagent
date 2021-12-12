package com.megaease.easeagent.plugin.tools.metrics;

import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A {@link NameFactory} Supplier
 *
 * @param <T> the type of ServiceMetric by this Supplier
 */
public interface ServiceMetricSupplier<T extends ServiceMetric> {

    /**
     * the type of ServiceMetric
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
     * new a ServiceMetric
     *
     * @param metricRegistry {@link MetricRegistry}
     * @param nameFactory    {@link NameFactory}
     * @return a type of ServiceMetric
     */
    T newInstance(MetricRegistry metricRegistry, NameFactory nameFactory);
}
