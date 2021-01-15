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

 package com.megaease.easeagent.zipkin;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runnable;
import org.junit.Test;
import zipkin.reporter.Callback;

import java.util.Collections;

import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.Runner.running;

public class GatewaySenderTest {
    @Test
    public void should_work() throws Exception {
        final HttpServer server = httpServer(log());
        server.request(eq(header("User-Agent"),"easeagent/0.1.0")).response(status(200));

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                new GatewaySender(1024, "http://localhost:" + server.port(), 1000, 1000, false, "easeagent/0.1.0")
                .sendSpans(Collections.singletonList(new byte[0]), new Callback() {
                    @Override
                    public void onComplete() { }

                    @Override
                    public void onError(Throwable t) {
                        throw new IllegalStateException(t);
                    }
                });
            }
        });
    }
}