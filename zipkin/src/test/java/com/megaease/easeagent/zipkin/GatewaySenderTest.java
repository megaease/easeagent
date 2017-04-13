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