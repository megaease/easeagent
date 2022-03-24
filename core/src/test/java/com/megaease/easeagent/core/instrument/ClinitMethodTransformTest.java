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
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import com.megaease.easeagent.core.utils.AgentAttachmentRule;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.InterceptorProvider;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.scaffold.TypeWriter;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.MethodRule;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("all")
public class ClinitMethodTransformTest extends TransformTestBase {
    private static ClassLoader classLoader;
    private static String dumpFolder;
    private static final AtomicInteger globalIndex = new AtomicInteger(0);

    private static String testString = "kkk";

    @Rule
    public MethodRule agentAttachmentRule = new AgentAttachmentRule();

    @BeforeClass
    public static void setUp() {
        EaseAgent.initializeContextSupplier = TestContext::new;
        classLoader = new ByteArrayClassLoader.ChildFirst(
            NonStaticMethodTransformTest.class.getClassLoader(),
            ClassFileLocator.ForClassLoader.readToNames(Foo.class,
                FooClassInitInterceptor.class,
                FooClsInitProvider.class,
                CommonInlineAdvice.class),
            ByteArrayClassLoader.PersistenceHandler.MANIFEST);

        String path = "target/test-classes";
        File file = new File(path);
        dumpFolder = file.getAbsolutePath();
        System.out.println(dumpFolder);
        assertTrue(dumpFolder.endsWith("target" + File.separator + "test-classes"));
    }

    // @Test
    // @AgentAttachmentRule.Enforce
    public void testTypeInitialAdviceTransformer() throws Exception {
        System.setProperty(TypeWriter.DUMP_PROPERTY, dumpFolder);
        assertEquals(System.getProperty(TypeWriter.DUMP_PROPERTY), dumpFolder);

        assertThat(ByteBuddyAgent.install(), instanceOf(Instrumentation.class));
        AgentBuilder builder = Bootstrap.getAgentBuilder(null, true);

        Set<MethodTransformation> transformations = getMethodTransformations(globalIndex.incrementAndGet(),
            CLASS_INIT, new FooClsInitProvider());

        ClassFileTransformer classFileTransformer = builder
            .type(ElementMatchers.is(Foo.class), ElementMatchers.is(classLoader))
            .transform(PluginLoader.compound(false, transformations))
            .installOnByteBuddyAgent();

        try {
            Class<?> type = classLoader.loadClass(Foo.class.getName());
            AgentFieldReflectAccessor.setStaticFieldValue(type, "clazzInitString", BAR+QUX);
            testString = AgentFieldReflectAccessor.getStaticFieldValue(type, "clazzInitString");
            assertEquals(BAR + QUX, testString);
            // wait to finish
        } finally {
            assertThat(ByteBuddyAgent.getInstrumentation().removeTransformer(classFileTransformer), is(true));
        }
    }

    @SuppressWarnings("unused")
    public static class Foo {
        static String clazzInitString = FOO;

        public static String fooStatic(String a) {
            return a;
        }

        public String foo(String a) {
            return a;
        }
    }

    public static class FooClassInitInterceptor implements Interceptor {
        @Override
        public void before(MethodInfo methodInfo, Context context) {
            System.out.println("aaa");
        }

        @Override
        public void after(MethodInfo methodInfo, Context context) {
            testString = BAR + QUX;
        }

        @Override
        public int order() {
            return Order.HIGH.getOrder();
        }
    }

    static class FooClsInitProvider implements InterceptorProvider {
        @Override
        public Supplier<Interceptor> getInterceptorProvider() {
            return FooClassInitInterceptor::new;
        }

        @Override
        public String getAdviceTo() {
            return "";
        }

        @Override
        public String getPluginClassName() {
            return TestPlugin.class.getCanonicalName();
        }
    }
}
