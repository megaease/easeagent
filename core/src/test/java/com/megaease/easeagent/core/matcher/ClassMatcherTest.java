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

package com.megaease.easeagent.core.matcher;

import com.megaease.easeagent.core.plugin.annotation.Index;
import com.megaease.easeagent.core.plugin.matcher.ClassMatcherConvert;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.Assert;
import org.junit.Test;

public class ClassMatcherTest {
    public static class TestBaseClass {
    }

    public interface TestInterface {
    }

    public interface TestInterface2 extends TestInterface {
    }

    @Index
    public static class TestClass extends TestBaseClass implements TestInterface {
    }

    @Test
    public void testMatch() {
        // super class matcher
        IClassMatcher matcher = ClassMatcher.builder()
            .hasSuperClass(TestBaseClass.class.getName()).build();

        ElementMatcher<TypeDescription> eMatcher = ClassMatcherConvert.INSTANCE.convert(matcher);
        TypeDescription type = TypeDescription.ForLoadedType.of(TestClass.class);
        Assert.assertTrue(eMatcher.matches(type));

        // test annotation match
        matcher = ClassMatcher.builder()
            .hasInterface(TestInterface.class.getName())
            .hasAnnotation(Index.class.getName())
            .build();
        eMatcher = ClassMatcherConvert.INSTANCE.convert(matcher);

        Assert.assertTrue(eMatcher.matches(type));

        // no interface
        matcher = ClassMatcher.builder()
            .hasInterface(TestInterface.class.getName())
            .notInterface()
            .build();
        type = TypeDescription.ForLoadedType.of(TestInterface2.class);
        eMatcher = ClassMatcherConvert.INSTANCE.convert(matcher);
        Assert.assertFalse(eMatcher.matches(type));
        type = TypeDescription.ForLoadedType.of(TestClass.class);
        Assert.assertTrue(eMatcher.matches(type));
    }
}
