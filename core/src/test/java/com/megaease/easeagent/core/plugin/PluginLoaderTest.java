package com.megaease.easeagent.core.plugin;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.core.plugin.registry.PluginRegistry;
import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.CodeVersion;
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

public class PluginLoaderTest {

    @Test
    public void isVersion() {
        AgentPlugin plugin = new TestAgentPlugin();
        PluginRegistry.register(plugin);
        TestPoints points = new TestPoints();
        assertTrue(PluginLoader.isCodeVersion(points, new Configs(Collections.emptyMap())));
        Configs configs = new Configs(Collections.singletonMap("runtime.code.version.points.sprint-boot", "spring_boot_2_x"));
        //empty codeVersion always true
        assertTrue(PluginLoader.isCodeVersion(points, configs));

        points.versions = CodeVersion.builder().key("sprint-boot").add(Points.DEFAULT_VERSION).add("spring_boot_2_x").build();
        assertTrue(PluginLoader.isCodeVersion(points, configs));

        points.versions = CodeVersion.builder().key("sprint-boot").add("spring_boot_2_x").build();
        assertTrue(PluginLoader.isCodeVersion(points, configs));

        points.versions = CodeVersion.builder().key("sprint-boot").add("spring_boot_3_x").build();
        assertFalse(PluginLoader.isCodeVersion(points, configs));

        configs = new Configs(Collections.singletonMap("runtime.code.version.points.sprint-boot", "spring_boot_3_x"));
        points.versions = CodeVersion.builder().key("sprint-boot").add(Points.DEFAULT_VERSION).add("spring_boot_2_x").build();
        assertFalse(PluginLoader.isCodeVersion(points, configs));

        configs = new Configs(Collections.emptyMap());
        assertTrue(PluginLoader.isCodeVersion(points, configs));

    }

    class TestAgentPlugin implements AgentPlugin {
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
        CodeVersion versions;

        @Override
        public CodeVersion codeVersions() {
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
