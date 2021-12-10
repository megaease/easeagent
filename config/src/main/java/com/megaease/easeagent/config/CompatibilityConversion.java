package com.megaease.easeagent.config;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;

public class CompatibilityConversion {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompatibilityConversion.class);
    protected static final String[] REQUEST_NAMESPACE = new String[]{
        ConfigConst.Namespace.HTTPCLIENT,
        ConfigConst.Namespace.OK_HTTP,
        ConfigConst.Namespace.WEB_CLIENT,
        ConfigConst.Namespace.FEIGN_CLIENT,
        ConfigConst.Namespace.REST_TEMPLATE,
    };
    private static final Map<String, BiFunction<String, String, Conversion>> KEY_TO_NAMESPACE;
    private static final Set<String> METRIC_SKIP;
    private static final Set<String> TRACING_SKIP;

    static {
        Map<String, BiFunction<String, String, Conversion>> map = new HashMap<>();
        map.put(ConfigConst.Observability.KEY_METRICS_ACCESS, SingleBuilder.observability(ConfigConst.Namespace.ACCESS));

        map.put(ConfigConst.Observability.KEY_METRICS_REQUEST, MultipleBuilder.observability(Arrays.asList(REQUEST_NAMESPACE)));
        map.put(ConfigConst.Observability.KEY_METRICS_JDBC_STATEMENT, SingleBuilder.observability(ConfigConst.Namespace.JDBC_STATEMENT));
        map.put(ConfigConst.Observability.KEY_METRICS_JDBC_CONNECTION, SingleBuilder.observability(ConfigConst.Namespace.JDBC_CONNECTION));
        map.put(ConfigConst.Observability.KEY_METRICS_MD5_DICTIONARY, SingleBuilder.observability(ConfigConst.Namespace.MD5_DICTIONARY));
        map.put(ConfigConst.Observability.KEY_METRICS_RABBIT, SingleBuilder.observability(ConfigConst.Namespace.RABBITMQ));
        map.put(ConfigConst.Observability.KEY_METRICS_KAFKA, SingleBuilder.observability(ConfigConst.Namespace.KAFKA));
        map.put(ConfigConst.Observability.KEY_METRICS_CACHE, SingleBuilder.observability(ConfigConst.Namespace.REDIS));
        map.put(ConfigConst.Observability.KEY_METRICS_JVM_GC, null);
        map.put(ConfigConst.Observability.KEY_METRICS_JVM_MEMORY, null);

        map.put(ConfigConst.Observability.KEY_TRACE_REQUEST, MultipleBuilder.observability(Arrays.asList(REQUEST_NAMESPACE)));
        map.put(ConfigConst.Observability.KEY_TRACE_REMOTE_INVOKE, SingleBuilder.observability(ConfigConst.Namespace.WEB_CLIENT));
        map.put(ConfigConst.Observability.KEY_TRACE_KAFKA, SingleBuilder.observability(ConfigConst.Namespace.KAFKA));
        map.put(ConfigConst.Observability.KEY_TRACE_JDBC, SingleBuilder.observability(ConfigConst.Namespace.JDBC));
        map.put(ConfigConst.Observability.KEY_TRACE_CACHE, SingleBuilder.observability(ConfigConst.Namespace.REDIS));
        map.put(ConfigConst.Observability.KEY_TRACE_RABBIT, SingleBuilder.observability(ConfigConst.Namespace.RABBITMQ));

        KEY_TO_NAMESPACE = map;

        TRACING_SKIP = new HashSet<>();
        TRACING_SKIP.add(ConfigConst.Observability.KEY_COMM_ENABLED);
        TRACING_SKIP.add(ConfigConst.Observability.KEY_COMM_SAMPLED_BY_QPS);
        TRACING_SKIP.add(ConfigConst.Observability.KEY_COMM_OUTPUT);

        METRIC_SKIP = new HashSet<>();
        METRIC_SKIP.add(ConfigConst.Observability.KEY_METRICS_JVM_GC);
        METRIC_SKIP.add(ConfigConst.Observability.KEY_METRICS_JVM_MEMORY);
    }

    public static Map<String, String> transform(Map<String, String> oldConfigs) {
        Map<String, Object> changedKeys = new HashMap<>();
        Map<String, String> newConfigs = new HashMap<>();
        for (Map.Entry<String, String> entry : oldConfigs.entrySet()) {
            Conversion conversion = transformConversion(entry.getKey());
            Object changed = conversion.transform(newConfigs, entry.getValue());
            if (conversion.isChange()) {
                changedKeys.put(entry.getKey(), changed);
            }
        }
        if (changedKeys.isEmpty()) {
            return oldConfigs;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("config key has transform: ");
            for (Map.Entry<String, Object> entry : changedKeys.entrySet()) {
                LOGGER.info("{} to {}", entry.getKey(), entry.getValue());
            }
        }
        return newConfigs;
    }

    private static Conversion transformConversion(String key) {
        if (key.startsWith("observability.metrics.")) {
            return metricConversion(key);
        } else if (key.startsWith("observability.tracings.")) {
            return tracingConversion(key);
        } else if (key.startsWith(ConfigConst.GlobalCanaryLabels.SERVICE_HEADERS + ".")) {
            return penetrationFieldsConversion(key);
        }
        return new FinalConversion(key, false);
    }

    private static Conversion metricConversion(String key) {
        if (key.equals(ConfigConst.Observability.METRICS_ENABLED)) {
            return new MultipleFinalConversion(Arrays.asList(new FinalConversion(ConfigConst.Observability.METRICS_ENABLED, true),
                new FinalConversion(ConfigConst.Plugin.OBSERVABILITY_GLOBAL_METRIC_ENABLED, true)), true);
        }
        return conversion(key, METRIC_SKIP, ConfigConst.PluginID.METRIC);
    }


    private static Conversion tracingConversion(String key) {
        if (key.equals(ConfigConst.Observability.TRACE_ENABLED)) {
            return new FinalConversion(ConfigConst.Plugin.OBSERVABILITY_GLOBAL_TRACING_ENABLED, true);
        }
        return conversion(key, TRACING_SKIP, ConfigConst.PluginID.TRACING);
    }

    private static Conversion penetrationFieldsConversion(String key) {
        return new FinalConversion(key.replace(ConfigConst.GlobalCanaryLabels.SERVICE_HEADERS + ".", ProgressFields.EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG + "."), true);
    }

    private static Conversion conversion(String key, Set<String> skipSet, String pluginId) {
        String[] keys = ConfigConst.split(key);
        if (keys.length < 4) {
            return new FinalConversion(key, false);
        }
        String key2 = keys[2];
        if (skipSet.contains(key2)) {
            return new FinalConversion(key, false);
        }
        BiFunction<String, String, Conversion> builder = KEY_TO_NAMESPACE.get(key2);
        if (builder == null) {
            builder = SingleBuilder.observability(key2);
        }
        String[] properties = new String[keys.length - 3];
        int index = 0;
        for (int i = 3; i < keys.length; i++) {
            properties[index++] = keys[i];
        }
        return builder.apply(pluginId, ConfigConst.join(properties));
    }

    interface Conversion<K> {
        K transform(Map<String, String> configs, String value);

        boolean isChange();
    }

    static class FinalConversion implements Conversion<String> {
        private final String key;
        private final boolean change;

        public FinalConversion(String key, boolean change) {
            this.key = key;
            this.change = change;
        }

        @Override
        public String transform(Map<String, String> configs, String value) {
            configs.put(key, value);
            return key;
        }

        public boolean isChange() {
            return change;
        }
    }

    static class MultipleFinalConversion implements Conversion<List<String>> {
        private final List<FinalConversion> conversions;
        private final boolean change;

        MultipleFinalConversion(@Nonnull List<FinalConversion> conversions, boolean change) {
            this.conversions = conversions;
            this.change = change;
        }

        @Override
        public List<String> transform(Map<String, String> configs, String value) {
            List<String> result = new ArrayList<>();
            for (FinalConversion conversion : conversions) {
                result.add(conversion.transform(configs, value));
            }
            return result;
        }

        @Override
        public boolean isChange() {
            return change;
        }
    }

    static class SingleConversion implements Conversion<String> {
        private final String domain;
        private final String namespace;
        private final String id;
        private final String properties;

        public SingleConversion(String domain, String namespace, String id, String properties) {
            this.domain = domain;
            this.namespace = namespace;
            this.id = id;
            this.properties = properties;
        }

        @Override
        public String transform(Map<String, String> configs, String value) {
            String key = ConfigUtils.buildPluginProperty(domain, namespace, id, properties);
            configs.put(key, value);
            return key;
        }

        @Override
        public boolean isChange() {
            return true;
        }
    }

    static class MultipleConversion implements Conversion<List<String>> {
        private final String domain;
        private final List<String> namespaces;
        private final String id;
        private final String properties;

        public MultipleConversion(String domain, List<String> namespaces, String id, String properties) {
            this.domain = domain;
            this.namespaces = namespaces;
            this.id = id;
            this.properties = properties;
        }

        @Override
        public List<String> transform(Map<String, String> configs, String value) {
            List<String> keys = new ArrayList<>();
            for (String namespace : namespaces) {
                String key = ConfigUtils.buildPluginProperty(domain, namespace, id, properties);
                keys.add(key);
                configs.put(key, value);
            }
            return keys;
        }

        @Override
        public boolean isChange() {
            return true;
        }
    }

    static class SingleBuilder implements BiFunction<String, String, Conversion> {
        private final String domain;
        private final String namespace;

        public SingleBuilder(String domain, String namespace) {
            this.domain = domain;
            this.namespace = namespace;
        }

        @Override
        public Conversion apply(String id, String properties) {
            return new SingleConversion(domain, namespace, id, properties);
        }

        static SingleBuilder observability(String namespace) {
            return new SingleBuilder(ConfigConst.OBSERVABILITY, namespace);
        }
    }

    static class MultipleBuilder implements BiFunction<String, String, Conversion> {
        private final String domain;
        private final List<String> namespaces;

        public MultipleBuilder(String domain, List<String> namespaces) {
            this.domain = domain;
            this.namespaces = namespaces;
        }

        @Override
        public Conversion apply(String id, String properties) {
            return new MultipleConversion(domain, namespaces, id, properties);
        }

        static MultipleBuilder observability(List<String> namespaces) {
            return new MultipleBuilder(ConfigConst.OBSERVABILITY, namespaces);
        }
    }


}
