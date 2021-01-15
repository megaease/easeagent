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