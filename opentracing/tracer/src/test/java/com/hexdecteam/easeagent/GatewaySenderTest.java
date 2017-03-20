package com.hexdecteam.easeagent;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runnable;
import org.junit.Test;

import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.Runner.running;

public class GatewaySenderTest {
    @Test
    public void should_be_ok() throws Exception {
        final HttpServer server = httpServer(log());
        server.request(eq(header("User-Agent"),"easeagent/0.1.0")).response(status(200));

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {


                new TracerInitializer().hook(new TracerInitializer.Configuration() {
                    @Override
                    public String send_endpoint() {
                        return "http://localhost:" + server.port();
                    }

                    @Override
                    public String service_name() {
                        return "service";
                    }

                    @Override
                    public boolean send_compression() {
                        return true;
                    }
                }, null, null);

                TraceContext.tracer().buildSpan("test").start().finish();
                Thread.sleep(2000);

            }
        });

    }
}