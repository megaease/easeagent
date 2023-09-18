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
 */

package com.megaease.easeagent.config;

import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

public class ConfigPropertiesUtilsTest {

    @Rule
    public final EnvironmentVariables environmentVariables
        = new EnvironmentVariables();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties
        = new RestoreSystemProperties();

    @BeforeClass
    public static void beforeClass() {
        // EnvironmentVariables and restoreSystemProperties does not work with Java 16
        Assume.assumeTrue(
            System.getProperty("java.version").startsWith("1.8")
                || System.getProperty("java.version").startsWith("11")
        );
    }

    @Test
    public void getString_systemProperty() {
        environmentVariables.set("TEST_PROPERTY_STRING", "env");
        System.setProperty("test.property.string", "sys");
        Assert.assertEquals("sys", ConfigPropertiesUtils.getString("test.property.string"));
    }

    @Test
    public void getString_environmentVariable() {
        environmentVariables.set("TEST_PROPERTY_STRING", "env");
        Assert.assertEquals("env", ConfigPropertiesUtils.getString("test.property.string"));
    }

    @Test
    public void getString_none() {
        Assert.assertNull(ConfigPropertiesUtils.getString("test.property.string.none"));
    }

    @Test
    public void getInt_systemProperty() {
        environmentVariables.set("TEST_PROPERTY_INT", "12");
        System.setProperty("test.property.int", "42");
        Assert.assertEquals(42, ConfigPropertiesUtils.getInt("test.property.int", -1));
    }

    @Test
    public void getInt_environmentVariable() {
        environmentVariables.set("TEST_PROPERTY_INT", "12");
        Assert.assertEquals(12, ConfigPropertiesUtils.getInt("test.property.int", -1));
    }

    @Test
    public void getInt_none() {
        Assert.assertEquals(-1, ConfigPropertiesUtils.getInt("test.property.int", -1));
    }

    @Test
    public void getInt_invalidNumber() {
        System.setProperty("test.property.int", "not a number");
        Assert.assertEquals(-1, ConfigPropertiesUtils.getInt("test.property.int", -1));
    }

    @Test
    public void getBoolean_systemProperty() {
        environmentVariables.set("TEST_PROPERTY_BOOLEAN", "false");
        System.setProperty("test.property.boolean", "true");
        Assert.assertTrue(ConfigPropertiesUtils.getBoolean("test.property.boolean", false));
    }

    @Test
    public void getBoolean_environmentVariable() {
        environmentVariables.set("TEST_PROPERTY_BOOLEAN", "true");
        Assert.assertTrue(ConfigPropertiesUtils.getBoolean("test.property.boolean", false));
    }

    @Test
    public void getBoolean_none() {
        Assert.assertFalse(ConfigPropertiesUtils.getBoolean("test.property.boolean", false));
    }
}
