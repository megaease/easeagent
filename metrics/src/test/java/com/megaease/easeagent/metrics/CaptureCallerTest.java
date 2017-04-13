package com.megaease.easeagent.metrics;

import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CaptureCallerTest {
    @Test
    @SuppressWarnings("unchecked")
    public void should_work() throws Exception {
        final Config conf = ConfigFactory.parseString("include_class_prefix_list = [\"com.\"]");
        final CallTrace trace = new CallTrace();
        final Callable<String> c = (Callable<String>) Classes.transform("com.megaease.easeagent.metrics.CaptureCallerTest$Foo")
                                                             .with(new GenCaptureCaller(conf).define(Definition.Default.EMPTY), trace)
                                                             .load(getClass().getClassLoader()).get(0)
                                                             .getDeclaredConstructor(CallTrace.class)
                                                             .newInstance(trace);

        Context.pushIfRoot(trace, CaptureCaller.class, "should_work");
        assertThat(c.call(), is("Foo#call"));
        trace.pop();
    }

    static class Foo implements Callable<String> {

        final CallTrace trace;

        Foo(CallTrace trace) {this.trace = trace;}

        @Override
        public String call() throws Exception {
            return trace.peek().<Context>context().signature;
        }
    }
}