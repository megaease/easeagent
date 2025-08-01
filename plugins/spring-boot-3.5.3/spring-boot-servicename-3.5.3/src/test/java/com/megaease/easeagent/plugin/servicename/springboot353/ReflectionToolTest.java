/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.servicename.springboot353;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReflectionToolTest {

    @Test
    public void invokeMethod() throws ReflectiveOperationException {
        String name = "testName";
        TestClass testClass = new TestClass(name);
        assertEquals(name, ReflectionTool.invokeMethod(testClass, "get"));
        String p = "ttt";
        assertEquals(name + p, ReflectionTool.invokeMethod(testClass, "get", p));
    }

    @Test
    public void extractField() throws ReflectiveOperationException {
        String name = "testName";
        TestClass testClass = new TestClass(name);
        assertEquals(name, ReflectionTool.extractField(testClass, "name"));
    }

    @Test
    public void hasText() {
        assertFalse(ReflectionTool.hasText(null));
        assertFalse(ReflectionTool.hasText(""));
        assertFalse(ReflectionTool.hasText("  "));
        assertTrue(ReflectionTool.hasText("ab"));
        assertTrue(ReflectionTool.hasText(" ab "));
    }

    class TestClass {
        private final String name;

        public TestClass(String name) {
            this.name = name;
        }

        private String get() {
            return name;
        }

        private String get(String p) {
            return name + p;
        }
    }
}
