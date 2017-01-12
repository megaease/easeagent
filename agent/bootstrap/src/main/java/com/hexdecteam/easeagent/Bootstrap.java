package com.hexdecteam.easeagent;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Executor;

import static com.hexdecteam.easeagent.ConfigurationDecorator.configurationClassDeclaredBy;
import static com.hexdecteam.easeagent.ConfigurationDecorator.configurationDecorator;
import static com.hexdecteam.easeagent.DaemonExecutors.newCached;
import static com.hexdecteam.easeagent.DaemonExecutors.shutdownAware;

/** bootstrap to hook all {@link Plugin}s. */
public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    @SuppressWarnings("unchecked")
    public static void premain(String args, Instrumentation inst) throws Exception {
        final long started = System.currentTimeMillis();

        final Set<String> injected = AppendBootstrapClassLoaderSearch.by(inst);

        LOGGER.debug("Injected {}", injected);

        final Map<Class<?>, Consumer> consumers = Maps.newHashMap();
        final Subscription subscription = new DefaultSubscription(consumers);
        final ConfigurationDecorator decorator = configurationDecorator(args);

        for (Plugin<Object> plugin : ServiceLoader.load(Plugin.class, Bootstrap.class.getClassLoader())) {
            LOGGER.debug("Loading {}", plugin.getClass());
            final Object conf = decorator.newInstance(configurationClassDeclaredBy(plugin.getClass()));
            plugin.hook(conf, inst, subscription);
        }

        LOGGER.debug("Subscribed {}", consumers.values());

        final Executor executor = shutdownAware(newCached("easeagent-event-dispatcher"));

        executor.execute(new EventDispatcher(Collections.unmodifiableMap(consumers), executor));

        LOGGER.debug("Takes {}ms", System.currentTimeMillis() - started);
    }

}
