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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.megaease.easeagent.config.*;
import com.megaease.easeagent.context.ContextManager;
import com.megaease.easeagent.core.plugin.BridgeDispatcher;
import com.megaease.easeagent.core.plugin.PluginLoader;
import com.megaease.easeagent.core.utils.WrappedConfigManager;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandlerProvider;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;
import com.megaease.easeagent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.BeanProvider;
import com.megaease.easeagent.plugin.IProvider;
import com.megaease.easeagent.plugin.annotation.Injection;
import com.megaease.easeagent.plugin.api.metric.MetricProvider;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConfigProcessor;
import com.megaease.easeagent.plugin.api.trace.TracingProvider;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;
import lombok.SneakyThrows;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import org.apache.commons.lang3.StringUtils;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.collect.FluentIterable.from;
import static net.bytebuddy.matcher.ElementMatchers.*;

@SuppressWarnings("unused")
public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private static final List<AgentHttpHandler> AGENT_HTTP_HANDLER_LIST_ON_INIT = new ArrayList<>();

    private static final List<AgentHttpHandler> AGENT_HTTP_HANDLER_LIST_AFTER_PROVIDER = new ArrayList<>();

    private static final String AGENT_SERVER_PORT_KEY = "easeagent.server.port";

    private static final String AGENT_SERVER_ENABLED_KEY = "easeagent.server.enabled";

    private static final String AGENT_MIDDLEWARE_UPDATE = "easeagent.middleware.update";

    private static final int DEF_AGENT_SERVER_PORT = 9900;

    static final String MX_BEAN_OBJECT_NAME = "com.megaease.easeagent:type=ConfigManager";

    private static WrappedConfigManager wrappedConfigManager;

    private static ContextManager contextManager;

    public static void start(String args, Instrumentation inst, Iterable<Class<?>> providers,
                             Iterable<Class<? extends Transformation>> transformations) throws Exception {
        long begin = System.nanoTime();

        // add bootstrap classes
        Set<String> bootstrapClassSet = AppendBootstrapClassLoaderSearch.by(inst, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Injected class: {}", bootstrapClassSet);
        }

        // initiate configuration
        final Configs conf = load(args);
        if (LOGGER.isDebugEnabled()) {
            final String display = conf.toPrettyDisplay();
            LOGGER.debug("Loaded conf:\n{}", display);
        }
        registerMBeans(conf);
        contextManager = ContextManager.build(conf);

        EaseAgent.dispatcher = new BridgeDispatcher();

        // inner httpserver
        Integer port = conf.getInt(AGENT_SERVER_PORT_KEY);
        if (port == null) {
            port = DEF_AGENT_SERVER_PORT;
        }
        String portStr = System.getProperty(AGENT_SERVER_PORT_KEY, String.valueOf(port));
        port = Integer.parseInt(portStr);
        AgentHttpServer agentHttpServer = new AgentHttpServer(port);
        agentHttpServer.addHttpRoutes(AGENT_HTTP_HANDLER_LIST_ON_INIT);
        Boolean httpServerEnabled = conf.getBoolean(AGENT_SERVER_ENABLED_KEY);
        String httpServerEnabledInProp = System.getProperty(AGENT_SERVER_ENABLED_KEY, String.valueOf(httpServerEnabled));
        httpServerEnabled = Boolean.parseBoolean(httpServerEnabledInProp);
        if (httpServerEnabled) {
            agentHttpServer.startServer();
            LOGGER.info("start agent http server on port:{}", port);
        }

        // redirection
        MiddlewareConfigProcessor.INSTANCE.init();

        //
        AgentBuilder builder = getAgentBuilder(conf, false);

        // load plugins
        builder = PluginLoader.load(builder, conf);

        final AgentReport agentReport = AgentReport.create(conf);
        builder = define(transformations, loadProvider(conf, agentReport), builder, conf, agentReport);
        builder = define(transformations, scoped(providers, conf, agentReport), builder, conf, agentReport);

        long installBegin = System.currentTimeMillis();
        builder.installOn(inst);

        LOGGER.info("installBegin use time: {}", (System.currentTimeMillis() - installBegin));
        agentHttpServer.addHttpRoutes(AGENT_HTTP_HANDLER_LIST_AFTER_PROVIDER);
        LOGGER.info("Initialization has took {}ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
    }

    private static Map<Class<?>, Iterable<QualifiedBean>> loadProvider(final Configs conf, final AgentReport agentReport) {
        ServiceLoader<BeanProvider> loader = ServiceLoader.load(BeanProvider.class);
        Iterator<BeanProvider> iterator = loader.iterator();
        return ImmutableMap.copyOf(
            Maps.transformValues(from(loader).uniqueIndex((Function<BeanProvider, Class<?>>) BeanProvider::getClass),
                input -> {
                    provider(input, conf, agentReport);
                    return beans(input);
                }));

    }

    private static void provider(final BeanProvider beanProvider, final Configs conf, final AgentReport agentReport) {
        if (beanProvider instanceof ConfigAware) {
            ((ConfigAware) beanProvider).setConfig(conf);
        }
        if (beanProvider instanceof AgentReportAware) {
            ((AgentReportAware) beanProvider).setAgentReport(agentReport);
        }
        if (beanProvider instanceof AgentHttpHandlerProvider) {
            AGENT_HTTP_HANDLER_LIST_AFTER_PROVIDER.addAll(((AgentHttpHandlerProvider) beanProvider).getAgentHttpHandlers());
        }
        if (beanProvider instanceof IProvider) {
            ((IProvider) beanProvider).afterPropertiesSet();
        }

        if (beanProvider instanceof TracingProvider) {
            TracingProvider tracingProvider = (TracingProvider) beanProvider;
            contextManager.setTracing(tracingProvider);
        }

        if (beanProvider instanceof MetricProvider) {
            contextManager.setMetric((MetricProvider) beanProvider);
        }
    }

    private static Iterable<QualifiedBean> beans(final BeanProvider beanProvider) {
        final ImmutableList.Builder<QualifiedBean> builder = ImmutableList.builder();
        for (Method method : beanProvider.getClass().getMethods()) {
            final Injection.Bean bean = method.getAnnotation(Injection.Bean.class);
            if (bean == null) continue;
            try {
                final QualifiedBean qb = new QualifiedBean(bean.value(), method.invoke(beanProvider));
                builder.add(qb);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Provided {} ", qb);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return builder.build();
    }

    public static AgentBuilder getAgentBuilder(Configs config, boolean test) {
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
            .or(nameStartsWith("org.apache.logging"))
            .or(nameStartsWith("kotlin."))
            .or(nameStartsWith("javax."))
            .or(nameStartsWith("net.bytebuddy."))
            .or(nameStartsWith("com\\.sun\\.proxy\\.\\$Proxy.+"))
            .or(nameStartsWith("java\\.lang\\.invoke\\.BoundMethodHandle\\$Species_L.+"))
            .or(nameStartsWith("org.junit."))
            .or(nameStartsWith("junit."))
            .or(nameStartsWith("com.intellij."));

        if (!test) {
            builder = ignore
                .or(nameStartsWith("com.megaease.easeagent."));
        } else {
            builder = ignore;
        }
        LOGGER.info("AgentBuilder use time: {}", (System.currentTimeMillis() - buildBegin));

        return builder;
    }

    static MBeanServer registerMBeans(ConfigManagerMXBean conf) throws Exception {
        long begin = System.currentTimeMillis();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mxBeanName = new ObjectName(MX_BEAN_OBJECT_NAME);
        ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
        wrappedConfigManager = new WrappedConfigManager(customClassLoader, conf);
        mbs.registerMBean(wrappedConfigManager, mxBeanName);
        LOGGER.info("Register {} as MBean {}, use time: {}",
            conf.getClass().getName(), mxBeanName, (System.currentTimeMillis() - begin));
        return mbs;
    }

    private static Map<Class<?>, Iterable<QualifiedBean>> scoped(Iterable<Class<?>> providers, final Configs conf, final AgentReport agentReport) {
        return ImmutableMap.copyOf(
            Maps.transformValues(from(providers).uniqueIndex(Class::getSuperclass),
                input -> beans(input, conf, agentReport)));
    }

    private static ElementMatcher<ClassLoader> protectedLoaders() {
        return isBootstrapClassLoader().or(is(Bootstrap.class.getClassLoader()));
    }

    private static AgentBuilder define(Iterable<Class<? extends Transformation>> transformations,
                                       Map<Class<?>, Iterable<QualifiedBean>> scopedBeans, AgentBuilder ab, Configs conf, AgentReport report) {
        long begin = System.currentTimeMillis();
        for (Class<? extends Transformation> tc : transformations) {
            final Injection.Provider ann = tc.getAnnotation(Injection.Provider.class);
            final Iterable<QualifiedBean> beans = ann == null ? Collections.emptySet() : scopedBeans.get(ann.value());
            final Register register = new Register(beans);

            for (Map.Entry<ElementMatcher<? super TypeDescription>, Iterable<Definition.Transformer>> entry :
                newInstance(tc, conf, report).define(Definition.Default.EMPTY).asMap().entrySet()) {
                ab = ab.type(entry.getKey()).transform(compound(entry.getValue(), register));
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Defined {}", tc);
            }
        }
        LOGGER.info("define use time: {}", (System.currentTimeMillis() - begin));
        return ab;
    }

    private static AgentBuilder.Transformer compound(Iterable<Definition.Transformer> transformers,
                                                     final Register register) {
        List<AgentBuilder.Transformer> agentTransformers = StreamSupport.stream(transformers.spliterator(), false)
            .map(transformation -> new ForRegisterAdvice(register, transformation))
            .collect(Collectors.toList());

        return new CompoundTransformer(agentTransformers);
    }

    private static Iterable<QualifiedBean> beans(Class<?> provider, Configs conf, AgentReport agentReport) {
        final ImmutableList.Builder<QualifiedBean> builder = ImmutableList.builder();
        final Object instance = newInstance(provider, conf, agentReport);

        if (instance instanceof IProvider) {
            ((IProvider) instance).afterPropertiesSet();
        }

        if (instance instanceof TracingProvider) {
            TracingProvider tracingProvider = (TracingProvider) instance;
            contextManager.setTracing(tracingProvider);
        }

        if (instance instanceof MetricProvider) {
            contextManager.setMetric((MetricProvider) instance);
        }
        for (Method method : provider.getMethods()) {
            final Injection.Bean bean = method.getAnnotation(Injection.Bean.class);
            if (bean == null) continue;
            try {
                final QualifiedBean qb = new QualifiedBean(bean.value(), method.invoke(instance));
                builder.add(qb);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Provided {} ", qb);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        return builder.build();
    }

    private static <T> T newInstance(Class<T> aClass, Configs conf, AgentReport agentReport) {
        final Configurable configurable = aClass.getAnnotation(Configurable.class);
        try {
            final T t = configurable == null ? aClass.newInstance()
                : aClass.getConstructor(Config.class).newInstance(conf);
            if (t instanceof ConfigAware) {
                ((ConfigAware) t).setConfig(conf);
            }
            if (t instanceof AgentReportAware) {
                ((AgentReportAware) t).setAgentReport(agentReport);
            }
            if (t instanceof AgentHttpHandlerProvider) {
                AGENT_HTTP_HANDLER_LIST_AFTER_PROVIDER.addAll(((AgentHttpHandlerProvider) t).getAgentHttpHandlers());
            }
            return t;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Configs load(String pathname) {
        Configs configs = ConfigFactory.loadFromClasspath(Bootstrap.class.getClassLoader());
        if (StringUtils.isNotEmpty(pathname)) {
            Configs configsFromOuterFile = ConfigFactory.loadFromFile(new File(pathname));
            configs.updateConfigsNotNotify(configsFromOuterFile.getConfigs());
        }
        AGENT_HTTP_HANDLER_LIST_ON_INIT.add(new ServiceUpdateAgentHttpHandler());
        AGENT_HTTP_HANDLER_LIST_ON_INIT.add(new CanaryUpdateAgentHttpHandler());
        AGENT_HTTP_HANDLER_LIST_ON_INIT.add(new CanaryListUpdateAgentHttpHandler());
        AGENT_HTTP_HANDLER_LIST_ON_INIT.add(new PluginPropertyHttpHandler());
        AGENT_HTTP_HANDLER_LIST_ON_INIT.add(new PluginPropertiesHttpHandler());
        return configs;
    }

    static Map<String, String> toConfigMap(Map<String, Object> map) {
        Map<String, String> config = new HashMap<>();
        map.forEach((s, o) -> config.put(s, o.toString()));
        return config;
    }

    public static class CanaryUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler {
        @Override
        public String getPath() {
            return "/config-canary";
        }

        @Override
        public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
            wrappedConfigManager.updateCanary2(config, version);
            return null;
        }
    }

    public static class CanaryListUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler {
        public final static AtomicInteger lastCount = new AtomicInteger(0);

        @Override
        public String getPath() {
            return "/config-global-transmission";
        }

        @Override
        @SuppressWarnings("unchecked")
        public Response processJsonConfig(Map<String, Object> map, Map<String, String> urlParams) {
            LOGGER.info("call /config-global-transmission. configs: {}", map);
            synchronized (lastCount) {
                List<String> headers = (List<String>) map.get("headers");
                Map<String, String> config = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    config.put("easeagent.progress.forwarded.headers.global.transmission." + i, headers.get(i));
                }
                int last = lastCount.get();
                if (headers.size() < last) {
                    for (int i = headers.size(); i < last; i++) {
                        config.put("easeagent.progress.forwarded.headers.global.transmission." + i, "");
                    }
                }
                lastCount.set(headers.size());
                wrappedConfigManager.updateConfigs(config);
            }
            return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
        }

        @Override
        public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
            wrappedConfigManager.updateCanary2(config, version);
            return null;
        }
    }

    public static class ServiceUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler {
        @Override
        public String getPath() {
            return "/config-service";
        }

        @Override
        public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
            wrappedConfigManager.updateService2(CompatibilityConversion.transform(config), version);
            return null;
        }
    }

    public static class PluginPropertyHttpHandler extends AgentHttpHandler {
        public PluginPropertyHttpHandler() {
            methods = Collections.singleton(
                com.megaease.easeagent.httpserver.nanohttpd.protocols.http.request.Method.GET);
        }

        @Override
        public String getPath() {
            return "/plugins/domains/:domain/namespaces/:namespace/:id/properties/:property/:value/:version";
        }

        @SneakyThrows
        @Override
        public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String version = urlParams.get("version");
            if (version == null) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, (String) null);
            }
            try {
                String property = ConfigUtils.buildPluginProperty(
                    ConfigUtils.requireNonEmpty(urlParams.get("domain"), "urlParams.domain must not be null and empty."),
                    ConfigUtils.requireNonEmpty(urlParams.get("namespace"), "urlParams.namespace must not be null and empty."),
                    ConfigUtils.requireNonEmpty(urlParams.get("id"), "urlParams.id must not be null and empty."),
                    ConfigUtils.requireNonEmpty(urlParams.get("property"), "urlParams.property must not be null and empty."));
                String value = Objects.requireNonNull(urlParams.get("value"), "urlParams.value must not be null.");
                wrappedConfigManager.updateService2(Collections.singletonMap(property, value), version);
                return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
            } catch (Exception e) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, e.getMessage());
            }

        }
    }

    public static class PluginPropertiesHttpHandler extends ConfigsUpdateAgentHttpHandler {
        @Override
        public String getPath() {
            return "/plugins/domains/:domain/namespaces/:namespace/:id/properties";
        }

        @Override
        public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
            try {
                String domain = ConfigUtils.requireNonEmpty(urlParams.get("domain"), "urlParams.domain must not be null and empty.");
                String namespace = ConfigUtils.requireNonEmpty(urlParams.get("namespace"), "urlParams.namespace must not be null and empty.");
                String id = ConfigUtils.requireNonEmpty(urlParams.get("id"), "urlParams.id must not be null and empty.");
                Map<String, String> changeConfig = new HashMap<>();
                for (Map.Entry<String, String> propertyEntry : config.entrySet()) {
                    String property = ConfigUtils.buildPluginProperty(domain,
                        namespace,
                        id,
                        ConfigUtils.requireNonEmpty(propertyEntry.getKey(), "body.key must not be null and empty."));
                    String value = Objects.requireNonNull(propertyEntry.getValue(), String.format("body.%s must not be null.", propertyEntry.getKey()));
                    changeConfig.put(property, value);
                }
                wrappedConfigManager.updateService2(changeConfig, version);
                return null;
            } catch (Exception e) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, e.getMessage());
            }
        }
    }

    public static abstract class ConfigsUpdateAgentHttpHandler extends AgentHttpHandler {
        public abstract Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version);

        @SneakyThrows
        @Override
        public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String body = this.buildRequestBody(session);
            if (StringUtils.isEmpty(body)) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, (String) null);
            }
            Map<String, Object> map = JsonUtil.toMap(body);
            if (map == null) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, (String) null);
            }
            return processJsonConfig(map, urlParams);
        }

        public Response processJsonConfig(Map<String, Object> map, Map<String, String> urlParams) {
            String version = (String) map.remove("version");
            if (version == null) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, (String) null);
            }
            Map<String, String> config = toConfigMap(map);
            Response response = processConfig(config, urlParams, version);
            if (response != null) {
                return response;
            }
            return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
        }
    }

    private static final AgentBuilder.Listener LISTENER = new AgentBuilder.Listener() {
        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        }

        @Override
        public void onTransformation(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded, DynamicType dt) {
            LOGGER.debug("onTransformation: {} loaded: {} from classLoader {}", td, loaded, ld);
        }

        @Override
        public void onIgnored(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded) {
        }

        @Override
        public void onError(String name, ClassLoader ld, JavaModule m, boolean loaded, Throwable error) {
            LOGGER.debug("Just for Debug-log, transform ends exceptionally, which is sometimes normal and sometimes there is an error: {} error:{} loaded: {} from classLoader {}", name, error, loaded, ld);
            // LOGGER.debug("Just for Debug-log, transform ends exceptionally, which is sometimes normal and sometimes there is an error,:", error);
        }

        @Override
        public void onComplete(String name, ClassLoader ld, JavaModule m, boolean loaded) {
        }
    };

    private static class ForRegisterAdvice implements AgentBuilder.Transformer {
        private final Register register;
        private final String adviceFactoryClassName;
        private final ForAdvice transformer;
        private final Definition.Transformer agentTransformer;

        ForRegisterAdvice(Register register, Definition.Transformer transformer) {
            this.register = register;
            this.agentTransformer = transformer;
            this.adviceFactoryClassName = transformer.adviceFactoryClassName;
            this.transformer = new ForAdvice().include(getClass().getClassLoader())
                .advice(transformer.matcher, transformer.inlineAdviceClassName);

        }

        @Override
        public Builder<?> transform(Builder<?> b, TypeDescription td, ClassLoader cl, JavaModule m) {
            register.apply(adviceFactoryClassName, cl);
            if (!td.isAssignableTo(DynamicFieldAccessor.class)) {
                if (this.agentTransformer.fieldName != null) {
                    b = b.defineField(this.agentTransformer.fieldName, this.agentTransformer.fieldClass, Opcodes.ACC_PRIVATE)
                        .implement(DynamicFieldAccessor.class).intercept(FieldAccessor.ofField(this.agentTransformer.fieldName));
                }
            }
            return transformer.transform(b, td, cl, m);
        }
    }
}
