/*
 * Copyright (c) 2022, MegaEase
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
 *
 */
package com.megaease.easeagent.core.matcher;

import com.megaease.easeagent.core.Bootstrap;
import com.megaease.easeagent.core.plugin.matcher.ClassLoaderMatcherConvert;
import com.megaease.easeagent.plugin.matcher.loader.ClassLoaderMatcher;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderMatcherTest {
    @Test
    public void test_convert() {
        ElementMatcher<ClassLoader> matcher;
        // bootstrap
        matcher = ClassLoaderMatcherConvert.INSTANCE.convert(ClassLoaderMatcher.BOOTSTRAP);
        Assert.assertTrue(matcher.matches(null));
        Assert.assertFalse(matcher.matches(ClassLoader.getSystemClassLoader()));

        // external
        matcher = ClassLoaderMatcherConvert.INSTANCE.convert(ClassLoaderMatcher.EXTERNAL);
        Assert.assertFalse(matcher.matches(null));
        Assert.assertFalse(matcher.matches(ClassLoader.getSystemClassLoader()));
        Assert.assertTrue(matcher.matches(ClassLoader.getSystemClassLoader().getParent()));

        // system
        matcher = ClassLoaderMatcherConvert.INSTANCE.convert(ClassLoaderMatcher.SYSTEM);
        Assert.assertFalse(matcher.matches(ClassLoader.getSystemClassLoader().getParent()));
        Assert.assertTrue(matcher.matches(ClassLoader.getSystemClassLoader()));

        // agent
        matcher = ClassLoaderMatcherConvert.INSTANCE.convert(ClassLoaderMatcher.AGENT);
        Assert.assertTrue(matcher.matches(Bootstrap.class.getClassLoader()));
        Assert.assertFalse(matcher.matches(ClassLoader.getSystemClassLoader().getParent()));

        // name
        matcher = ClassLoaderMatcherConvert.INSTANCE
            .convert(new ClassLoaderMatcher("com.megaease.easeagent.core.matcher.ClassLoaderMatcherTest.TestClassLoader"));
        URL[] urls = new URL[1];
        urls[0] = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        Assert.assertFalse(matcher.matches(Bootstrap.class.getClassLoader()));
        Assert.assertTrue(matcher.matches(new TestClassLoader(urls)));
    }

    static class TestClassLoader extends URLClassLoader {
        public TestClassLoader(URL[] urls) {
            super(urls);
        }
    }
}
