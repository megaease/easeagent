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

package com.megaease.easeagent.core.plugin.registry;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.InterceptorProvider;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PluginRegistryTest {

    @Test
    public void isVersion() {
        AgentPlugin plugin = new TestAgentPlugin();
        PluginRegistry.register(plugin);
        InterceptorProvider provider = new InterceptorProvider() {
            @Override
            public Supplier<Interceptor> getInterceptorProvider() {
                return null;
            }

            @Override
            public String getAdviceTo() {
                return null;
            }

            @Override
            public String getPluginClassName() {
                return plugin.getClass().getCanonicalName();
            }
        };
        TestPoints points = new TestPoints();
        assertTrue(PluginRegistry.isCodeVersion(points, provider, new Configs(Collections.emptyMap())));
        Configs configs = new Configs(Collections.singletonMap("plugin.test_domain.test_namespace.code.version", "spring_boot_2_x"));
        assertTrue(PluginRegistry.isCodeVersion(points, provider, configs));
        points.versions = new HashSet<>(Arrays.asList(Points.DEFAULT_VERSION, "spring_boot_2_x"));
        assertTrue(PluginRegistry.isCodeVersion(points, provider, configs));
        points.versions = new HashSet<>(Collections.singletonList("spring_boot_2_x"));
        assertTrue(PluginRegistry.isCodeVersion(points, provider, configs));
        points.versions = new HashSet<>(Collections.singletonList("spring_boot_3_x"));
        assertFalse(PluginRegistry.isCodeVersion(points, provider, configs));
        configs = new Configs(Collections.singletonMap("plugin.test_domain.test_namespace.code.version", "spring_boot_3_x"));
        points.versions = new HashSet<>(Arrays.asList(Points.DEFAULT_VERSION, "spring_boot_2_x"));
        assertFalse(PluginRegistry.isCodeVersion(points, provider, configs));
        configs = new Configs(Collections.emptyMap());
        assertTrue(PluginRegistry.isCodeVersion(points, provider, configs));

    }

    class TestAgentPlugin implements AgentPlugin{
        @Override
        public String getNamespace() {
            return "test_namespace";
        }

        @Override
        public String getDomain() {
            return "test_domain";
        }
    }

    class TestPoints implements Points {
        Set<String> versions;

        @Override
        public Set<String> codeVersions() {
            if (versions == null) {
                return Points.super.codeVersions();
            } else {
                return versions;
            }
        }

        @Override
        public IClassMatcher getClassMatcher() {
            return null;
        }

        @Override
        public Set<IMethodMatcher> getMethodMatcher() {
            return null;
        }
    }
}
