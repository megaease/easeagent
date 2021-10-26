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

package com.megaease.easeagent.core.instrument;

import com.megaease.easeagent.core.Bootstrap;
import com.megaease.easeagent.core.plugin.CommonInlineAdvice;
import com.megaease.easeagent.core.plugin.PluginLoader;
import com.megaease.easeagent.core.plugin.interceptor.SupplierChain;
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import com.megaease.easeagent.log4j2.supplier.AllUrlsSupplier;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.scaffold.TypeWriter;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransformerTest {
    static {
        AllUrlsSupplier.ENABLED = true;
    }
    private static final String FOO = "foo", BAR = "bar", QUX = "qux";

    @SuppressWarnings("unused")
    public static class Foo {
        public String foo(String a) {
            return a;
        }
    }

    public static class FooInterceptor implements Interceptor {
        @Override
        public void before(MethodInfo methodInfo, Object context) {
            Object [] args = methodInfo.getArgs();
            args[0] = QUX;
        }

        @Override
        public Object after(MethodInfo methodInfo, Object context) {
            methodInfo.setRetValue(methodInfo.getRetValue() + BAR);
            return null;
        }
    }

    private ClassLoader classLoader;
    private String dumpFolder;

    @Before
    public void setUp() {
        classLoader = new ByteArrayClassLoader.ChildFirst(
            getClass().getClassLoader(),
            ClassFileLocator.ForClassLoader.readToNames(Foo.class, CommonInlineAdvice.class),
            ByteArrayClassLoader.PersistenceHandler.MANIFEST);

        String path = "target/test-classes";
        File file = new File(path);
        dumpFolder = file.getAbsolutePath();
        System.out.println(dumpFolder);
        assertTrue(dumpFolder.endsWith("target/test-classes"));
    }

    @Test
    public void testAdviceTransformer() throws Exception {
        System.setProperty(TypeWriter.DUMP_PROPERTY, dumpFolder);
        assertEquals(System.getProperty(TypeWriter.DUMP_PROPERTY), dumpFolder);
        assertThat(ByteBuddyAgent.install(), instanceOf(Instrumentation.class));
        AgentBuilder builder = Bootstrap.getAgentBuilder(null);

        Supplier<Interceptor> supplier = FooInterceptor::new;
        SupplierChain.Builder<Interceptor> chainBuilder = SupplierChain.builder();
        chainBuilder.addSupplier(supplier);

        MethodTransformation methodTransformation = new MethodTransformation(1,
            ElementMatchers.named(FOO),
            chainBuilder);

        Set<MethodTransformation> transformations = new HashSet<>();
        transformations.add(methodTransformation);

        ClassFileTransformer classFileTransformer = builder
            .type(ElementMatchers.is(Foo.class), ElementMatchers.is(classLoader))
            .transform(PluginLoader.compound(true, transformations))
            .installOnByteBuddyAgent();
        try {
            Class<?> type = classLoader.loadClass(Foo.class.getName());
            // check
            Object instance = type.getDeclaredConstructor().newInstance();
            AgentDynamicFieldAccessor.setDynamicFieldValue(instance, BAR);
            assertEquals(AgentDynamicFieldAccessor.getDynamicFieldValue(instance), BAR);
            assertThat(type.getDeclaredMethod(FOO, String.class)
                    .invoke(instance, "kkk"),
                is(QUX + BAR));
        } finally {
            assertThat(ByteBuddyAgent.getInstrumentation().removeTransformer(classFileTransformer), is(true));
        }
    }
}
