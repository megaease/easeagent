package com.megaease.easeagent.requests;

import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CaptureTraceTest {

    @Test
    public void should_capture_call() throws Exception {
        final Config conf = ConfigFactory.parseString("include_class_prefix_list=[\"com\"]");
        final CallTrace trace = new CallTrace();

        final String name = "com.megaease.easeagent.requests.CaptureTraceTest$Foo";
        final Runnable r = (Runnable) Classes.transform(name)
                                             .with(new GenCaptureTrace(conf).define(Definition.Default.EMPTY), trace)
                                             .load(getClass().getClassLoader())
                                             .get(0).newInstance();


        Context.pushIfRootCall(trace, CaptureTraceTest.class, "should_capture_call");

        r.run();

        final Context context = Context.pop(trace).getChildren().get(0);

        assertThat(context.getShortSignature(), is("Foo#run"));
        assertThat(context.getIoquery(), is(false));

    }

    static class Foo implements Runnable {

        @Override
        public void run() {

        }
    }
}