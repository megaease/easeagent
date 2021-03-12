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
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigFactory;
import com.megaease.easeagent.config.ConfigManagerMXBean;
import com.megaease.easeagent.config.Configs;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Default;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import org.jolokia.jvmagent.JvmAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.FluentIterable.from;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static void start(String args, Instrumentation inst, Iterable<Class<?>> providers,
                             Iterable<Class<? extends Transformation>> transformations) throws Exception {
        final long begin = System.nanoTime();

        LOGGER.debug("Injected class: {}", AppendBootstrapClassLoaderSearch.by(inst, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP));

        final Configs conf = load(args);

        if (LOGGER.isDebugEnabled()) {
            final String repr = conf.toPrettyDisplay();
            LOGGER.debug("Loaded conf:\n{}", repr);
        }

        define(
                conf, transformations, scoped(providers, conf),
                new Default()
                        .with(LISTENER)
                        .ignore(any(), protectedLoaders())
                        .or(isSynthetic())
                        .or(nameStartsWith("sun.reflect."))
                        .or(nameStartsWith("net.bytebuddy."))
                        .or(nameStartsWith("com\\.sun\\.proxy\\.\\$Proxy.+"))
                        .or(nameStartsWith("java\\.lang\\.invoke\\.BoundMethodHandle\\$Species_L.+"))
                        .or(nameStartsWith("org.junit."))
                        .or(nameStartsWith("junit."))
                        .or(nameStartsWith("com.intellij."))
        ).installOn(inst);
        registerMBeans(conf, inst);
        LOGGER.info("Initialization has took {}ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
    }

    static void registerMBeans(ConfigManagerMXBean conf, Instrumentation inst) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mxbeanName = new ObjectName("com.megaease.easeagent:type=ConfigManager");
        mbs.registerMBean(conf, mxbeanName);
        LOGGER.debug("Register {} as MBean {}", conf.getClass().getName(), mxbeanName.toString());
        JvmAgent.premain("", inst);
    }

    private static Map<Class<?>, Iterable<QualifiedBean>> scoped(Iterable<Class<?>> providers, final Configs conf) {
        return ImmutableMap.copyOf(Maps.transformValues(
                from(providers).uniqueIndex(new Function<Class<?>, Class<?>>() {
                    @Override
                    public Class<?> apply(Class<?> input) {
                        return input.getSuperclass();
                    }
                }), new Function<Class<?>, Iterable<QualifiedBean>>() {
                    @Override
                    public Iterable<QualifiedBean> apply(Class<?> input) {
                        return beans(conf, input);
                    }
                }));
    }

    private static ElementMatcher<ClassLoader> protectedLoaders() {
        return isBootstrapClassLoader().or(is(Bootstrap.class.getClassLoader()));
    }

    private static AgentBuilder define(Configs conf, Iterable<Class<? extends Transformation>> transformations,
                                       Map<Class<?>, Iterable<QualifiedBean>> scopedBeans, AgentBuilder ab) {

        for (Class<? extends Transformation> tc : transformations) {
            final Injection.Provider ann = tc.getAnnotation(Injection.Provider.class);
            final Iterable<QualifiedBean> beans = ann == null ? Collections.<QualifiedBean>emptySet() : scopedBeans.get(ann.value());
            final Register register = new Register(beans);

            for (Map.Entry<ElementMatcher<? super TypeDescription>, Iterable<Definition.Transformer>> entry :
                    newInstance(conf, tc).define(Definition.Default.EMPTY).asMap().entrySet()) {
                ab = ab.type(entry.getKey()).transform(compound(entry.getValue(), register));
            }

            LOGGER.debug("Defined {}", tc);
        }
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

    private static Iterable<QualifiedBean> beans(Configs conf, Class<?> provider) {
        final ImmutableList.Builder<QualifiedBean> builder = ImmutableList.builder();
        final Object instance = newInstance(conf, provider);
        for (Method method : provider.getMethods()) {
            final Injection.Bean bean = method.getAnnotation(Injection.Bean.class);
            if (bean == null) continue;
            try {
                final QualifiedBean qb = new QualifiedBean(bean.value(), method.invoke(instance));
                builder.add(qb);
                LOGGER.debug("Provided {} ", qb);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return builder.build();
    }

    private static <T> T newInstance(Configs conf, Class<T> aClass) {
        final Configurable configurable = aClass.getAnnotation(Configurable.class);
        try {
            return configurable == null ? aClass.newInstance()
                    : aClass.getConstructor(Config.class).newInstance(conf.getConfig(configurable.bind()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Configs load(String pathname) throws IOException {
        return Strings.isNullOrEmpty(pathname)
                ? ConfigFactory.loadFromClasspath(Bootstrap.class.getClassLoader())
                : ConfigFactory.loadFromFile(new File(pathname));
    }

    private static final AgentBuilder.Listener LISTENER = new AgentBuilder.Listener() {

        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
//            LOGGER.debug("onDiscovery {} from classLoader {}", typeName, classLoader);
        }

        @Override
        public void onTransformation(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded, DynamicType dt) {
            LOGGER.info("onTransformation: {} loaded: {} from classLoader {}", td, loaded, ld);
        }

        @Override
        public void onIgnored(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded) {
            LOGGER.debug("onIgnored: {} loaded: {} from classLoader {}", td, loaded, ld);
        }

        @Override
        public void onError(String name, ClassLoader ld, JavaModule m, boolean loaded, Throwable error) {
            LOGGER.error("onError: {} error:{} loaded: {} from classLoader {}", name, error, loaded, ld);
            error.printStackTrace();
        }

        @Override
        public void onComplete(String name, ClassLoader ld, JavaModule m, boolean loaded) {
            LOGGER.debug("onComplete: {} loaded: {} from classLoader {}", name, loaded, ld);
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
            if (this.agentTransformer.fieldName != null) {
                b = b.defineField(this.agentTransformer.fieldName, this.agentTransformer.fieldClass, Opcodes.ACC_PROTECTED);
            }
            return transformer.transform(b, td, cl, m);
        }
    }
}
