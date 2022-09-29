/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.*;
import com.megaease.easeagent.plugin.api.context.IContextManager;
import com.megaease.easeagent.plugin.api.dispatcher.IDispatcher;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.logging.Mdc;
import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.report.AgentReport;

import java.net.URLClassLoader;
import java.util.function.Supplier;

/**
 * the bridge api will be initiated when agent startup
 */
public final class EaseAgent {
    public static AgentInfo agentInfo;
    public static MetricRegistrySupplier metricRegistrySupplier = NoOpMetrics.NO_OP_METRIC_SUPPLIER;
    public static IContextManager initializeContextSupplier = () -> NoOpContext.NO_OP_CONTEXT;
    public static ILoggerFactory loggerFactory = NoOpLoggerFactory.INSTANCE;
    public static Mdc loggerMdc = NoOpLoggerFactory.NO_OP_MDC_INSTANCE;
    public static IConfigFactory configFactory = new NoOpConfigFactory();
    public static AgentReport agentReport = new NoOpAgentReporter();

    public static IDispatcher dispatcher = new NoOpDispatcher();

    public static Supplier<URLClassLoader> agentClassloader = () -> null;

    public static URLClassLoader getAgentClassLoader() {
        return agentClassloader.get();
    }

    public static AgentReport getAgentReport() {
        return agentReport;
    }

    /**
     * @see ILoggerFactory#getLogger(Class)
     */
    public static Logger getLogger(Class clazz) {
        return loggerFactory.getLogger(clazz);
    }

    /**
     * @see MetricRegistrySupplier#newMetricRegistry(IPluginConfig, NameFactory, Tags)
     */
    public static MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags) {
        return metricRegistrySupplier.newMetricRegistry(config, nameFactory, tags);
    }

    public static <T extends ServiceMetric> T getOrCreateServiceMetric(IPluginConfig config, Tags tags, ServiceMetricSupplier<T> supplier) {
        return ServiceMetricRegistry.getOrCreate(config, tags, supplier);
    }

    /**
     * @see MetricRegistrySupplier#reporter(IPluginConfig)
     */
    public static Reporter metricReporter(IPluginConfig config) {
        return metricRegistrySupplier.reporter(config);
    }

    public static Config getConfig() {
        return configFactory.getConfig();
    }

    /**
     * Returns a configuration property from the agent's global configuration.
     *
     * @return The configuration of this Java agent.
     */
    public static String getConfig(String property) {
        return configFactory.getConfig(property);
    }

    /**
     * find the configuration property from the agent's global configuration.
     * if not exist, then return @{defaultValue}
     *
     * @param defaultValue default value returned when the property is not exist
     * @return The configuration of this Java agent.
     */
    public static String getConfig(String property, String defaultValue) {
        return configFactory.getConfig(property, defaultValue);
    }

    /**
     * get a Config by domain, namespace and name
     *
     * @param domain
     * @param namespace
     * @param name
     * @return {@link IPluginConfig}
     */
    public static IPluginConfig getConfig(String domain, String namespace, String name) {
        return configFactory.getConfig(domain, namespace, name);
    }

    /**
     * get or create an AutoRefreshPluginConfigImpl
     *
     * @param domain    String
     * @param namespace String
     * @param name      String
     * @return AutoRefreshPluginConfigImpl
     * @see AutoRefreshPluginConfigRegistry#getOrCreate(String, String, String)
     */
    public static AutoRefreshPluginConfigImpl getOrCreateAutoRefreshConfig(String domain, String namespace, String name) {
        return AutoRefreshPluginConfigRegistry.getOrCreate(domain, namespace, name);
    }

    /**
     * @return current tracing {@link Context} for session
     */
    public static Context getContext() {
        return initializeContextSupplier.getContext();
    }

    public static AgentInfo getAgentInfo() {
        return agentInfo;
    }
}
