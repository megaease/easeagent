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

package com.megaease.easeagent.core;

import com.megaease.easeagent.config.*;
import com.megaease.easeagent.context.ContextManager;
import com.megaease.easeagent.core.config.*;
import com.megaease.easeagent.core.info.AgentInfoFactory;
import com.megaease.easeagent.core.plugin.BaseLoader;
import com.megaease.easeagent.core.plugin.BridgeDispatcher;
import com.megaease.easeagent.core.plugin.PluginLoader;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandlerProvider;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.metric.MetricProvider;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.trace.TracingProvider;
import com.megaease.easeagent.plugin.bean.AgentInitializingBean;
import com.megaease.easeagent.plugin.bean.BeanProvider;
import com.megaease.easeagent.plugin.bridge.AgentInfo;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;
import com.megaease.easeagent.report.DefaultAgentReport;
import lombok.SneakyThrows;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import org.apache.commons.lang3.StringUtils;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.matcher.ElementMatchers.*;

@SuppressWarnings("unused")
public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private static final String AGENT_SERVER_PORT_KEY = "easeagent.server.port";
    private static final String AGENT_CONFIG_PATH = "easeagent.config.path";
    private static final String AGENT_SERVER_ENABLED_KEY = "easeagent.server.enabled";

    private static final String AGENT_MIDDLEWARE_UPDATE = "easeagent.middleware.update";

    private static final int DEF_AGENT_SERVER_PORT = 9900;

    static final String MX_BEAN_OBJECT_NAME = "com.megaease.easeagent:type=ConfigManager";

    private static ContextManager contextManager;

    private Bootstrap() {
    }

    @SneakyThrows
    public static void start(String args, Instrumentation inst) {
        long begin = System.nanoTime();

        // add bootstrap classes
        Set<String> bootstrapClassSet = AppendBootstrapClassLoaderSearch.by(inst, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Injected class: {}", bootstrapClassSet);
        }

        // initiate configuration
        String configPath = System.getProperty(AGENT_CONFIG_PATH);
        if (StringUtils.isEmpty(configPath)) {
            configPath = args;
        }

        ClassLoader classLoader = Bootstrap.class.getClassLoader();
        final AgentInfo agentInfo = AgentInfoFactory.loadAgentInfo(classLoader);
        EaseAgent.agentInfo = agentInfo;
        final GlobalConfigs conf = ConfigFactory.loadConfigs(configPath, classLoader);
        wrapConfig(conf);

        // loader check
        GlobalAgentHolder.setAgentClassLoader((URLClassLoader) Bootstrap.class.getClassLoader());
        EaseAgent.agentClassloader = GlobalAgentHolder::getAgentClassLoader;

        // init Context/API
        contextManager = ContextManager.build(conf);
        EaseAgent.dispatcher = new BridgeDispatcher();

        // initInnerHttpServer
        initHttpServer(conf);

        // redirection
        RedirectProcessor.INSTANCE.init();

        // reporter
        final AgentReport agentReport = DefaultAgentReport.create(conf);
        GlobalAgentHolder.setAgentReport(agentReport);
        EaseAgent.agentReport = agentReport;

        // load plugins
        AgentBuilder builder = getAgentBuilder(conf, false);
        builder = PluginLoader.load(builder, conf);

        // provider & beans
        loadProvider(conf, agentReport);

        long installBegin = System.currentTimeMillis();
        builder.installOn(inst);
        LOGGER.info("installBegin use time: {}ms", (System.currentTimeMillis() - installBegin));

        LOGGER.info("Initialization has took {}ns", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
    }

    private static void initHttpServer(Configs conf) {
        // inner httpserver
        Integer port = conf.getInt(AGENT_SERVER_PORT_KEY);
        if (port == null) {
            port = DEF_AGENT_SERVER_PORT;
        }
        String portStr = System.getProperty(AGENT_SERVER_PORT_KEY, String.valueOf(port));
        port = Integer.parseInt(portStr);

        AgentHttpServer agentHttpServer = new AgentHttpServer(port);

        boolean httpServerEnabled = conf.getBoolean(AGENT_SERVER_ENABLED_KEY);
        if (httpServerEnabled) {
            agentHttpServer.startServer();
            LOGGER.info("start agent http server on port:{}", port);
        }
        GlobalAgentHolder.setAgentHttpServer(agentHttpServer);

        // add httpHandler
        agentHttpServer.addHttpRoute(new ServiceUpdateAgentHttpHandler());
        agentHttpServer.addHttpRoute(new CanaryUpdateAgentHttpHandler());
        agentHttpServer.addHttpRoute(new CanaryListUpdateAgentHttpHandler());
        agentHttpServer.addHttpRoute(new PluginPropertyHttpHandler());
        agentHttpServer.addHttpRoute(new PluginPropertiesHttpHandler());
    }

    private static void loadProvider(final Configs conf, final AgentReport agentReport) {
        List<BeanProvider> providers = BaseLoader.loadOrdered(BeanProvider.class);
        providers.forEach(input -> provider(input, conf, agentReport));
    }

    private static void provider(final BeanProvider beanProvider, final Configs conf, final AgentReport agentReport) {
        if (beanProvider instanceof ConfigAware) {
            ((ConfigAware) beanProvider).setConfig(conf);
        }
        if (beanProvider instanceof AgentReportAware) {
            ((AgentReportAware) beanProvider).setAgentReport(agentReport);
        }

        if (beanProvider instanceof AgentHttpHandlerProvider) {
            GlobalAgentHolder.getAgentHttpServer()
                .addHttpRoutes(((AgentHttpHandlerProvider) beanProvider).getAgentHttpHandlers());
        }
        if (beanProvider instanceof AgentInitializingBean) {
            ((AgentInitializingBean) beanProvider).afterPropertiesSet();
        }
        if (beanProvider instanceof TracingProvider) {
            TracingProvider tracingProvider = (TracingProvider) beanProvider;
            contextManager.setTracing(tracingProvider);
        }
        if (beanProvider instanceof MetricProvider) {
            contextManager.setMetric((MetricProvider) beanProvider);
        }
    }

    public static AgentBuilder getAgentBuilder(Configs config, boolean test) {
        // config may use to add some classes to be ignored in future
        long buildBegin = System.currentTimeMillis();
        AgentBuilder builder = new AgentBuilder.Default()
            .with(LISTENER)
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
            .with(AgentBuilder.LocationStrategy.ForClassLoader.STRONG
                .withFallbackTo(ClassFileLocator.ForClassLoader.ofSystemLoader()));
        AgentBuilder.Ignored ignore = builder.ignore(isSynthetic())
            .or(nameStartsWith("sun."))
            .or(nameStartsWith("com.sun."))
            .or(nameStartsWith("brave."))
            .or(nameStartsWith("zipkin2."))
            .or(nameStartsWith("com.fasterxml"))
            .or(nameStartsWith("org.apache.logging")
                .and(not(hasSuperClass(named("org.apache.logging.log4j.spi.AbstractLogger")))))
            .or(nameStartsWith("kotlin."))
            .or(nameStartsWith("javax."))
            .or(nameStartsWith("net.bytebuddy."))
            .or(nameStartsWith("com\\.sun\\.proxy\\.\\$Proxy.+"))
            .or(nameStartsWith("java\\.lang\\.invoke\\.BoundMethodHandle\\$Species_L.+"))
            .or(nameStartsWith("org.junit."))
            .or(nameStartsWith("junit."))
            .or(nameStartsWith("com.intellij."));

        // config used here to avoid warning of unused
        if (!test && config != null) {
            builder = ignore
                .or(nameStartsWith("com.megaease.easeagent."));
        } else {
            builder = ignore;
        }
        LOGGER.info("AgentBuilder use time: {}", (System.currentTimeMillis() - buildBegin));
        return builder;
    }

    @SneakyThrows
    static void registerMBeans(ConfigManagerMXBean conf) {
        long begin = System.currentTimeMillis();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mxBeanName = new ObjectName(MX_BEAN_OBJECT_NAME);
        ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
        mbs.registerMBean(conf, mxBeanName);
        LOGGER.info("Register {} as MBean {}, use time: {}",
            conf.getClass().getName(), mxBeanName, (System.currentTimeMillis() - begin));
    }

    private static ElementMatcher<ClassLoader> protectedLoaders() {
        return isBootstrapClassLoader().or(is(Bootstrap.class.getClassLoader()));
    }

    private static void wrapConfig(GlobalConfigs configs) {
        WrappedConfigManager wrappedConfigManager = new WrappedConfigManager(Bootstrap.class.getClassLoader(), configs);
        registerMBeans(wrappedConfigManager);
        GlobalAgentHolder.setWrappedConfigManager(wrappedConfigManager);
    }

    private static final AgentBuilder.Listener LISTENER = new AgentBuilder.Listener() {
        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            // ignored
        }

        @Override
        public void onTransformation(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded, DynamicType dt) {
            LOGGER.debug("onTransformation: {} loaded: {} from classLoader {}", td, loaded, ld);
        }

        @Override
        public void onIgnored(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded) {
            // ignored
        }

        @Override
        public void onError(String name, ClassLoader ld, JavaModule m, boolean loaded, Throwable error) {
            LOGGER.debug("Just for Debug-log, transform ends exceptionally, " +
                    "which is sometimes normal and sometimes there is an error: {} error:{} loaded: {} from classLoader {}",
                name, error, loaded, ld);
        }

        @Override
        public void onComplete(String name, ClassLoader ld, JavaModule m, boolean loaded) {
            // ignored
        }
    };
}
