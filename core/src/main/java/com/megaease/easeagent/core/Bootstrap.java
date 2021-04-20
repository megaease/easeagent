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
import com.megaease.easeagent.core.utils.JsonUtil;
import com.megaease.easeagent.core.utils.WrappedConfigManager;
import com.megaease.easeagent.httpserver.AgentHttpHandler;
import com.megaease.easeagent.httpserver.AgentHttpHandlerProvider;
import com.megaease.easeagent.httpserver.AgentHttpServer;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.FluentIterable.from;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private static final List<AgentHttpHandler> AGENT_HTTP_HANDLER_LIST_ON_INIT = new ArrayList<>();

    private static final List<AgentHttpHandler> AGENT_HTTP_HANDLER_LIST_AFTER_PROVIDER = new ArrayList<>();

    private static final String AGENT_SERVER_PORT_KEY = "easeagent.server.port";

    private static final int DEF_AGENT_SERVER_PORT = 9900;

    private static WrappedConfigManager wrappedConfigManager;

    public static void start(String args, Instrumentation inst, Iterable<Class<?>> providers,
                             Iterable<Class<? extends Transformation>> transformations) throws Exception {
        long begin = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Injected class: {}", AppendBootstrapClassLoaderSearch.by(inst, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP));
        }
        final Configs conf = load(args);
        if (LOGGER.isDebugEnabled()) {
            final String display = conf.toPrettyDisplay();
            LOGGER.info("Loaded conf:\n{}", display);
        }
        registerMBeans(conf);

        Integer port = conf.getInt(AGENT_SERVER_PORT_KEY);
        if (port == null) {
            port = DEF_AGENT_SERVER_PORT;
        }
        String portStr = System.getProperty(AGENT_SERVER_PORT_KEY, String.valueOf(port));
        port = Integer.parseInt(portStr);
        AgentHttpServer agentHttpServer = new AgentHttpServer(port);
        agentHttpServer.addHttpRoutes(AGENT_HTTP_HANDLER_LIST_ON_INIT);
        agentHttpServer.startServer();
        LOGGER.info("start agent http server on port:{}", port);

        long buildBegin = System.currentTimeMillis();
        AgentBuilder builder = new AgentBuilder.Default()
//                .with(LISTENER)
//                .with(new AgentBuilder.Listener.Filtering(
//                        new StringMatcher("java.lang.Thread", StringMatcher.Mode.EQUALS_FULLY),
//                        AgentBuilder.Listener.StreamWriting.toSystemOut()))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .with(AgentBuilder.LocationStrategy.ForClassLoader.STRONG.withFallbackTo(ClassFileLocator.ForClassLoader.ofBootLoader()))
//                .ignore(any(), protectedLoaders()) // we need to redefine java.lang and java.util
                .ignore(isSynthetic())
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
        LOGGER.info("AgentBuilder use time: {}", (System.currentTimeMillis() - buildBegin));

        final AgentReport agentReport = AgentReport.create(conf);
        builder = define(transformations, scoped(providers, conf, agentReport), builder, conf, agentReport);
        long installBegin = System.currentTimeMillis();
        builder.installOn(inst);
        LOGGER.info("installBegin use time: {}", (System.currentTimeMillis() - installBegin));
        agentHttpServer.addHttpRoutes(AGENT_HTTP_HANDLER_LIST_AFTER_PROVIDER);
        LOGGER.info("Initialization has took {}ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
    }

    static void registerMBeans(ConfigManagerMXBean conf) throws Exception {
        long begin = System.currentTimeMillis();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mxbeanName = new ObjectName("com.megaease.easeagent:type=ConfigManager");
        ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
        wrappedConfigManager = new WrappedConfigManager(customClassLoader, conf);
        mbs.registerMBean(wrappedConfigManager, mxbeanName);
        LOGGER.info("Register {} as MBean {}, use time: {}", conf.getClass().getName(), mxbeanName, (System.currentTimeMillis() - begin));
    }

    private static Map<Class<?>, Iterable<QualifiedBean>> scoped(Iterable<Class<?>> providers, final Configs conf, final AgentReport agentReport) {
        return ImmutableMap.copyOf(Maps.transformValues(
                from(providers).uniqueIndex(new Function<Class<?>, Class<?>>() {
                    @Override
                    public Class<?> apply(Class<?> input) {
                        return input.getSuperclass();
                    }
                }), new Function<Class<?>, Iterable<QualifiedBean>>() {
                    @Override
                    public Iterable<QualifiedBean> apply(Class<?> input) {
                        return beans(input, conf, agentReport);
                    }
                }));
    }

    private static ElementMatcher<ClassLoader> protectedLoaders() {
        return isBootstrapClassLoader().or(is(Bootstrap.class.getClassLoader()));
    }

    private static AgentBuilder define(Iterable<Class<? extends Transformation>> transformations,
                                       Map<Class<?>, Iterable<QualifiedBean>> scopedBeans, AgentBuilder ab, Configs conf, AgentReport report) {
        long begin = System.currentTimeMillis();
        for (Class<? extends Transformation> tc : transformations) {
            final Injection.Provider ann = tc.getAnnotation(Injection.Provider.class);
            final Iterable<QualifiedBean> beans = ann == null ? Collections.<QualifiedBean>emptySet() : scopedBeans.get(ann.value());
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

    private static AgentBuilder.Transformer compound(Iterable<Definition.Transformer> transformers, final Register register) {
        return new CompoundTransformer(from(transformers).transform(
                new Function<Definition.Transformer, AgentBuilder.Transformer>() {
                    @Override
                    public AgentBuilder.Transformer apply(final Definition.Transformer input) {
                        return new ForRegisterAdvice(register, input);
                    }
                }).toList());
    }

    private static Iterable<QualifiedBean> beans(Class<?> provider, Configs conf, AgentReport agentReport) {
        final ImmutableList.Builder<QualifiedBean> builder = ImmutableList.builder();
        final Object instance = newInstance(provider, conf, agentReport);
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
        if (instance instanceof IProvider) {
            ((IProvider) instance).afterPropertiesSet();
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
        public void processConfig(Map<String, String> config, String version) {
            wrappedConfigManager.updateCanary2(config, version);
        }
    }

    public static class ServiceUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler {
        @Override
        public String getPath() {
            return "/config-service";
        }

        @Override
        public void processConfig(Map<String, String> config, String version) {
            wrappedConfigManager.updateService2(config, version);
        }
    }

    public static abstract class ConfigsUpdateAgentHttpHandler extends AgentHttpHandler {

        public abstract void processConfig(Map<String, String> config, String version);

        @SneakyThrows
        @Override
        public NanoHTTPD.Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            HashMap<String, String> dataMap = new HashMap<>();
            session.parseBody(dataMap);
            String str = dataMap.get("postData");
            if (StringUtils.isEmpty(str)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, null);
            }
            Map<String, Object> map = JsonUtil.toMap(str);
            if (map == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, null);
            }
            String version = (String) map.remove("version");
            if (version == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, null);
            }
            Map<String, String> config = toConfigMap(map);
            processConfig(config, version);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, AgentHttpServer.JSON_TYPE, null);
        }
    }

    private static final AgentBuilder.Listener LISTENER = new AgentBuilder.Listener() {

        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            LOGGER.info("onDiscovery {} from classLoader {}", typeName, classLoader);
        }

        @Override
        public void onTransformation(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded, DynamicType dt) {
//            LOGGER.info("onTransformation: {} loaded: {} from classLoader {}", td, loaded, ld);
        }

        @Override
        public void onIgnored(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded) {
//            LOGGER.info("onIgnored: {} loaded: {} from classLoader {}", td, loaded, ld);
        }

        @Override
        public void onError(String name, ClassLoader ld, JavaModule m, boolean loaded, Throwable error) {
//            LOGGER.warn("onError: {} error:{} loaded: {} from classLoader {}", name, error, loaded, ld);
        }

        @Override
        public void onComplete(String name, ClassLoader ld, JavaModule m, boolean loaded) {
//            LOGGER.info("onComplete: {} loaded: {} from classLoader {}", name, loaded, ld);
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
