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

package com.megaease.easeagent.gen;

import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Dispatcher;
import com.megaease.easeagent.core.Injection;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
@Assembly({Bar.class, Baz.class})
public class AssemblyProcessorTest {
    private final String packageName = getClass().getPackage().getName();
    private final ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers.any();

    @Test
    public void should_generate_bar() throws Exception {
        final Class<Bar> genBar = (Class<Bar>) Class.forName(packageName + ".GenBar");
        assertNotNull(genBar.getAnnotation(Configurable.class));

        final Bar bar = genBar.getConstructor(Config.class).newInstance(new Configs(Collections.singletonMap("bool", "yes")));
        assertTrue(bar.bool());
        assertThat(bar.i(), is(10));

        assertNotNull(bar.demo(matcher));

        final Class<?> inline = Class.forName(packageName + ".GenBar$Demo_inline");

        final Method inline_run = inline.getDeclaredMethod("run", Object.class);
        assertTrue(Modifier.isStatic(inline_run.getModifiers()));
        assertNotNull(inline_run.getAnnotation(Advice.OnMethodEnter.class));
        assertNotNull(inline_run.getAnnotation(Advice.OnMethodExit.class));

        final Annotation[][] parameterAnnotations = inline_run.getParameterAnnotations();
        assertThat(Arrays.asList(parameterAnnotations[0]), hasItem(any(Advice.This.class)));

        final Class<?> factory = Class.forName(packageName + ".GenBar$Demo_factory");
        final Constructor<?> constructor = factory.getDeclaredConstructor(String.class);
        assertThat(constructor.getDeclaredAnnotations()[0], instanceOf(Injection.Autowire.class));
        assertThat(constructor.getParameterAnnotations()[0][0], instanceOf(Injection.Qualifier.class));

        final Method advice_run = factory.getMethod("advice_run");
        assertTrue(Dispatcher.Advice.class.isAssignableFrom(advice_run.getReturnType()));
    }

    @Test
    public void should_generate_baz() throws Exception {
        final Class<Baz> genBaz = (Class<Baz>) Class.forName(packageName + ".GenBaz");
        assertNull(genBaz.getAnnotation(Configurable.class));
        assertNotNull(genBaz.newInstance().demo(matcher));

        final Class<?> inline = Class.forName(packageName + ".GenBaz$Demo_inline");
        final Method run = inline.getDeclaredMethod("enter", Object.class);
        assertTrue(Modifier.isStatic(run.getModifiers()));
        assertNotNull(run.getAnnotation(Advice.OnMethodEnter.class));
        assertEquals(run.getReturnType(), boolean.class);

        final Method exit = inline.getDeclaredMethod("exit", boolean.class);
        assertTrue(Modifier.isStatic(exit.getModifiers()));
        assertNotNull(exit.getAnnotation(Advice.OnMethodExit.class));
        assertEquals(exit.getReturnType(), void.class);

        final Class<?> factory = Class.forName(packageName + ".GenBaz$Demo_factory");
        assertNotNull(factory.getMethod("advice_enter"));
        assertNotNull(factory.getMethod("advice_exit"));
    }

    @Test
    public void should_generate_qux() throws Exception {
        final Class<?> aClass = Class.forName(packageName + ".GenQux");
        assertTrue(Modifier.isPublic(aClass.getModifiers()));
    }

    @Test
    public void should_generate_start_bootstrap() throws Exception {
        final Class<?> aClass = Class.forName(packageName + ".StartBootstrap");
        assertTrue(Modifier.isPublic(aClass.getModifiers()));
        assertNotNull(aClass.getMethod("premain", String.class, Instrumentation.class));

    }
}