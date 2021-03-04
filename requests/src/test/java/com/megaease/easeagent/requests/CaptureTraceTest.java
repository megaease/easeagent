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

import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CaptureTraceTest {

    @Test
    public void should_capture_call() throws Exception {
        final Config conf = new Configs(Collections.singletonMap("include_class_prefix_list", "com"));
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