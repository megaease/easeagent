package com.megaease.easeagent.plugin.tools.metrics;

import com.megaease.easeagent.plugin.api.metric.name.NameFactory;

/**
 * A {@link NameFactory} Supplier
 */
public interface NameFactorySupplier {
    /**
     * new a NameFactory
     *
     * @return {@link NameFactory}
     */
    NameFactory newInstance();
}
