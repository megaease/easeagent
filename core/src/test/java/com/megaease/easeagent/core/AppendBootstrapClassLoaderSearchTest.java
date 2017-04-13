package com.megaease.easeagent.core;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.lang.instrument.Instrumentation;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class AppendBootstrapClassLoaderSearchTest {
    @Test
    public void should_inject_classes() throws Exception {
        final Set<String> strings = Sets.newHashSet(Dispatcher.class.getName(), Dispatcher.Advice.class.getName());
        assertThat(AppendBootstrapClassLoaderSearch.by(mock(Instrumentation.class)), is(strings));
    }
}