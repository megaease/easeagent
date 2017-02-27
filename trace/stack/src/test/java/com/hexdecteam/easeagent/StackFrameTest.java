package com.hexdecteam.easeagent;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class StackFrameTest {
    @Test
    public void should_record_stack_a_b_c() throws Exception {
        StackFrame.setRootIfAbsent("a");
        StackFrame.fork("b");
        StackFrame.fork("c");
        final StackFrame c = StackFrame.join();
        final StackFrame b = StackFrame.join();
        final StackFrame a = StackFrame.join();

        assertThat(a.getChildren(),is(Collections.singletonList(b)));
        assertThat(b.getChildren(),is(Collections.singletonList(c)));
        assertThat(c.getChildren(),is(Collections.<StackFrame>emptyList()));
    }

    @Test
    public void should_record_stack_a_bc() throws Exception {
        StackFrame.setRootIfAbsent("a");
        StackFrame.fork("b");
        final StackFrame b = StackFrame.join();
        StackFrame.fork("c");
        final StackFrame c = StackFrame.join();
        final StackFrame a = StackFrame.join();

        assertThat(a.getChildren(),is(Arrays.asList(b,c)));
        assertThat(b.getChildren(),is(Collections.<StackFrame>emptyList()));
        assertThat(c.getChildren(),is(Collections.<StackFrame>emptyList()));
    }

    @Test
    public void should_clean_thread_local() throws Exception {
        StackFrame.setRootIfAbsent("a");
        StackFrame.join();
        assertNull(StackFrame.join());
    }

    @Test
    public void should_fork_with_null_root() throws Exception {
        StackFrame.fork("a");
        assertNull(StackFrame.join());
    }

    @Test
    public void should_not_set_root() throws Exception {
        StackFrame.setRootIfAbsent("a");
        StackFrame.setRootIfAbsent("b");
        assertThat(StackFrame.join().getSignature(), is("a"));
    }
}