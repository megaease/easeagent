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

package com.megaease.easeagent.core;

import com.google.common.collect.Sets;
import net.bytebuddy.dynamic.loading.ClassInjector;
import org.junit.Assert;
import org.junit.Test;

import java.lang.instrument.Instrumentation;
import java.util.Set;

import static org.mockito.Mockito.mock;

public class AppendBootstrapClassLoaderSearchTest {
    @Test
    public void should_inject_classes() throws Exception {
        final Set<String> strings = Sets.newHashSet(
                "com.megaease.easeagent.core.utils.AgentArray",
                "com.megaease.easeagent.core.utils.ContextUtils",
                "com.megaease.easeagent.core.plugin.Dispatcher"
            );
        Assert.assertEquals(strings, AppendBootstrapClassLoaderSearch.by(mock(Instrumentation.class), ClassInjector.UsingInstrumentation.Target.SYSTEM));
    }
}
