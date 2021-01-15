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

 package com.megaease.easeagent.requests;

import com.alibaba.fastjson.JSON;
import com.megaease.easeagent.common.CallTrace;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import javax.servlet.http.HttpServlet;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
public class AsyncLogReporterTest {
    final Logger logger = mock(Logger.class);
    final CallTrace trace = new CallTrace();
    final Map<String, String> map = Collections.singletonMap("k", "v");

    @Test
    public void should_not_contain_call_tree() throws Exception {
        final AsyncLogReporter reporter = new AsyncLogReporter(logger, 1, "ip", "hostname", "system", "application", "type", false);
        Context.pushIfRootCall(trace, HttpServlet.class, "service");

        final Context root = Context.pop(trace);

        reporter.report("/", "GET", 200, map, map, root);
        Thread.sleep(100);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).info(eq("{}\n"), captor.capture());

        final Map<String, Object> map = JSON.parseObject(captor.getValue(), Map.class);
        assertTrue(map.get("callStackJson").toString().isEmpty());
        assertFalse((Boolean) map.get("containsCallTree"));
    }

    @Test
    public void should_contain_call_tree() throws Exception {
        final AsyncLogReporter reporter = new AsyncLogReporter(logger, 1, "ip", "hostname", "system", "application", "type", true);
        Context.pushIfRootCall(trace, HttpServlet.class, "service");

        Context.forkCall(trace, String.class, "toString");
        Context.join(trace);

        final Context root = Context.pop(trace);

        reporter.report("/", "GET", 200, map, map, root);
        Thread.sleep(100);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(logger).info(eq("{}\n"), captor.capture());

        final Map<String, Object> map = JSON.parseObject(captor.getValue(), Map.class);
        assertFalse(map.get("callStackJson").toString().isEmpty());
        assertTrue((Boolean) map.get("containsCallTree"));
    }
}