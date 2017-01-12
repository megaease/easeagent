package com.hexdecteam.easeagent;

import org.junit.Test;

import java.lang.instrument.Instrumentation;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AppendBootstrapClassLoaderSearchTest {
    @Test
    public void should_inject_class() throws Exception {
        final Set<String> names = AppendBootstrapClassLoaderSearch.by(mock(Instrumentation.class));
        assertTrue(names.contains(EventBus.class.getCanonicalName()));
    }
}