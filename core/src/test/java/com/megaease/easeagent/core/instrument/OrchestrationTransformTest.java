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
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.scaffold.TypeWriter;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unused")
public class OrchestrationTransformTest extends TransformTestBase {
    private static ClassLoader classLoader;
    private static String dumpFolder;
    private static final AtomicInteger globalIndex = new AtomicInteger(1000);

    @Rule
    public MethodRule agentAttachmentRule = new AgentAttachmentRule();

    @BeforeClass
    public static void setUp() {
        EaseAgent.initializeContextSupplier = TestContext::new;
        classLoader = new ByteArrayClassLoader.ChildFirst(
            OrchestrationTransformTest.class.getClassLoader(),
            ClassFileLocator.ForClassLoader.readToNames(Foo.class, CommonInlineAdvice.class),
            ByteArrayClassLoader.PersistenceHandler.MANIFEST);

        String path = "target/test-classes";
        File file = new File(path);
        dumpFolder = file.getAbsolutePath();
        System.out.println(dumpFolder);
        assertTrue(dumpFolder.endsWith("target" + File.separator + "test-classes"));
    }

    @Test
    @AgentAttachmentRule.Enforce
    public void testOrchestration() {
        System.setProperty(TypeWriter.DUMP_PROPERTY, dumpFolder);
        assertEquals(System.getProperty(TypeWriter.DUMP_PROPERTY), dumpFolder);

        assertThat(ByteBuddyAgent.install(), instanceOf(Instrumentation.class));
        AgentBuilder builder = Bootstrap.getAgentBuilder(null, true);

        Set<MethodTransformation> transformations
            = getMethodTransformations(globalIndex.incrementAndGet(), FOO, new FooProvider());
        Set<MethodTransformation> secTransformations
            = getMethodTransformations(globalIndex.incrementAndGet(), FOO, new FooSecProvider());

        ClassFileTransformer classFileTransformer = builder
            .type(hasSuperType(named(FooBase.class.getName())), ElementMatchers.is(classLoader))
            .transform(PluginLoader.compound(true, transformations))
            .transform(PluginLoader.compound(true, secTransformations))
            .installOnByteBuddyAgent();
        try {
            Class<?> type = classLoader.loadClass(Foo.class.getName());
            // check
            Object instance = type.getDeclaredConstructor().newInstance();
            AgentDynamicFieldAccessor.setDynamicFieldValue(instance, BAR);
            assertEquals(BAR, AgentDynamicFieldAccessor.getDynamicFieldValue(instance));
            assertThat(type.getDeclaredMethod(FOO, String.class)
                    .invoke(instance, BAR),
                is(BAR + QUX + BAR));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            assertThat(ByteBuddyAgent.getInstrumentation().removeTransformer(classFileTransformer), is(true));
        }
    }

    @SuppressWarnings("unused")
    public static class Foo extends FooBase {
        static String clazzInitString = FOO;

        public static String fooStatic(String a) {
            return a;
        }

        public String foo(String a) {
            return a;
        }

        public int baz() {
            return (int) System.currentTimeMillis();
        }
    }

    public interface FooInterface {
        String foo(String a);
    }

    public static class FooBase implements FooInterface {
        public String foo(String a) {
            return a + "-base";
        }
    }
}
