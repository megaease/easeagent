package com.megaease.easeagent.metrics;

import com.codahale.metrics.*;
import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.MocoConfig;
import com.github.dreamhead.moco.ResponseHandler;
import com.github.dreamhead.moco.internal.SessionContext;
import com.megaease.easeagent.metrics.jvm.gc.JVMGCMetric;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Runner.running;

public class JVMGCMetricTest {

    @Test
    public void success() throws Exception {
        MetricRegistry metricRegistry = new MetricRegistry();
        JVMGCMetric jvmgcMetric = new JVMGCMetric(metricRegistry);
        jvmgcMetric.collect();
        int serverPort = 12999;
        HttpServer httpServer = httpServer(serverPort);
        httpServer.response(new ResponseHandler() {
            @SneakyThrows
            @Override
            public void writeToResponse(SessionContext context) {
//                Runtime runtime = Runtime.getRuntime();
//                System.out.println("total memory: " + runtime.totalMemory() / (1024 * 1024));
//                System.out.println("begin response freeMemory:" + runtime.freeMemory() / (1024 * 1024));
                List<byte[]> list = new ArrayList<>();
                for (int i = 0; i < 40; i++) {
                    list.add(new byte[10 * 1024 * 1024]);
                }
                Assert.assertNotNull(list);
                System.gc();
                TimeUnit.SECONDS.sleep(1);
//                System.out.println("end response freeMemory:" + runtime.freeMemory() / (1024 * 1024));
            }

            @Override
            public ResponseHandler apply(MocoConfig config) {
                return this;
            }
        });

        running(httpServer, () -> {
            OkHttpClient client = new OkHttpClient.Builder().readTimeout(100, TimeUnit.SECONDS).build();
            for (int i = 0; i < 1; i++) {
                Request request = new Request.Builder()
                        .url("http://localhost:" + serverPort)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    Objects.requireNonNull(response.body()).string();
                }
                TimeUnit.SECONDS.sleep(1);
            }
            TimeUnit.SECONDS.sleep(5);
            Map<String, Metric> metrics = metricRegistry.getMetrics();
            Assert.assertFalse(metrics.isEmpty());
            for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
                Metric value = entry.getValue();
                if (value instanceof Meter) {
                    Meter meter = (Meter) value;
                    Assert.assertTrue(meter.getCount() > 1);
                    continue;
                }
                if (value instanceof Gauge) {
                    Counter counter = (Counter) value;
                    Assert.assertTrue(counter.getCount() > 1);
                }
            }
        });
    }
}
