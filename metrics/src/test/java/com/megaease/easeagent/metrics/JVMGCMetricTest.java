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

public class JVMGCMetricTest extends BaseMetricsTest {
//
//    @Test
//    public void success() throws Exception {
//        Config config = this.createConfig(JVMGCMetric.ENABLE_KEY, "true");
//        MetricRegistry metricRegistry = new MetricRegistry();
//        JVMGCMetric jvmgcMetric = new JVMGCMetric(metricRegistry, config);
//        jvmgcMetric.collect();
//        int serverPort = 12999;
//        HttpServer httpServer = httpServer(serverPort);
//        httpServer.response(new ResponseHandler() {
//            @SneakyThrows
//            @Override
//            public void writeToResponse(SessionContext context) {
//                List<byte[]> list = new ArrayList<>();
//                for (int i = 0; i < 40; i++) {
//                    list.add(new byte[10 * 1024 * 1024]);
//                }
//                Assert.assertNotNull(list);
//                System.gc();
//                TimeUnit.SECONDS.sleep(1);
//            }
//
//            @Override
//            public ResponseHandler apply(MocoConfig config) {
//                return this;
//            }
//        });
//
//        running(httpServer, () -> {
//            OkHttpClient client = new OkHttpClient.Builder().readTimeout(100, TimeUnit.SECONDS).build();
//            for (int i = 0; i < 1; i++) {
//                Request request = new Request.Builder()
//                        .url("http://localhost:" + serverPort)
//                        .build();
//                try (Response response = client.newCall(request).execute()) {
//                    Objects.requireNonNull(response.body()).string();
//                }
//                TimeUnit.SECONDS.sleep(1);
//            }
//            TimeUnit.SECONDS.sleep(5);
//            Map<String, Metric> metrics = metricRegistry.getMetrics();
//            Assert.assertFalse(metrics.isEmpty());
//            for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
//                Metric value = entry.getValue();
//                if (value instanceof Meter) {
//                    Meter meter = (Meter) value;
//                    Assert.assertTrue(meter.getCount() > 1);
//                    continue;
//                }
//                if (value instanceof Gauge) {
//                    Counter counter = (Counter) value;
//                    Assert.assertTrue(counter.getCount() > 1);
//                }
//            }
//        });
//    }
//
//    @Test
//    public void disableCollect() throws Exception {
//        Config config = this.createConfig(JVMGCMetric.ENABLE_KEY, "false");
//        MetricRegistry metricRegistry = new MetricRegistry();
//        JVMGCMetric jvmgcMetric = new JVMGCMetric(metricRegistry, config);
//        jvmgcMetric.collect();
//        int serverPort = 12999;
//        HttpServer httpServer = httpServer(serverPort);
//        httpServer.response(new ResponseHandler() {
//            @SneakyThrows
//            @Override
//            public void writeToResponse(SessionContext context) {
//                List<byte[]> list = new ArrayList<>();
//                for (int i = 0; i < 40; i++) {
//                    list.add(new byte[10 * 1024 * 1024]);
//                }
//                Assert.assertNotNull(list);
//                System.gc();
//                TimeUnit.SECONDS.sleep(1);
//            }
//
//            @Override
//            public ResponseHandler apply(MocoConfig config) {
//                return this;
//            }
//        });
//
//        running(httpServer, () -> {
//            OkHttpClient client = new OkHttpClient.Builder().readTimeout(100, TimeUnit.SECONDS).build();
//            for (int i = 0; i < 1; i++) {
//                Request request = new Request.Builder()
//                        .url("http://localhost:" + serverPort)
//                        .build();
//                try (Response response = client.newCall(request).execute()) {
//                    Objects.requireNonNull(response.body()).string();
//                }
//                TimeUnit.SECONDS.sleep(1);
//            }
//            TimeUnit.SECONDS.sleep(5);
//            Map<String, Metric> metrics = metricRegistry.getMetrics();
//            Assert.assertTrue(metrics.isEmpty());
//        });
//    }
}
