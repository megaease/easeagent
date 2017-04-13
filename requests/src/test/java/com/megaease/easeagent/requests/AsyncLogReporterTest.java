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
    final AsyncLogReporter reporter = new AsyncLogReporter(logger, 1, "ip", "hostname", "system", "application", "type");
    final Map<String, String> map = Collections.singletonMap("k", "v");

    @Test
    public void should_not_contain_call_tree() throws Exception {
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