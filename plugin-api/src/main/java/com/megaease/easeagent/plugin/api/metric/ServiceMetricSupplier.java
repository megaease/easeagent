/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
