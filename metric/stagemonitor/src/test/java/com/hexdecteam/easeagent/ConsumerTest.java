package com.hexdecteam.easeagent;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.stagemonitor.core.metrics.metrics2.MeterExt;
import org.stagemonitor.core.metrics.metrics2.TimerExt;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class ConsumerTest {

    final ObjectMapper                 mapper   = new ObjectMapper();
    final Map<String, String>          hostInfo = ImmutableMap.of("gid", "gid");
    final ImmutableMap<String, String> tags     = ImmutableMap.of("signature", "C.m");

    @Before
    public void setUp() throws Exception {
        mapper.registerModule(new MetricsXModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void should_serialize_meter() throws Exception {
        final Meter meterExt = new MeterExt();
        meterExt.mark();
        final Map map = consume(meterExt);

        assertTrue(map.containsKey("@timestamp"));
        assertTrue(map.containsKey("name"));
        assertTrue(map.containsKey("type"));
        assertTrue(map.containsKey("signature"));
        assertTrue(map.containsKey("gid"));
        assertTrue(map.containsKey("count"));
        assertTrue(map.containsKey("m1_rate"));
        assertTrue(map.containsKey("m5_rate"));
        assertTrue(map.containsKey("m15_rate"));
        assertTrue(map.containsKey("mean_rate"));
        assertTrue(map.containsKey("m1_count"));
        assertTrue(map.containsKey("m5_count"));
        assertTrue(map.containsKey("m15_count"));
    }

    @Test
    public void should_serialize_timer() throws Exception {
        final TimerExt metric = new TimerExt();
        metric.update(10, TimeUnit.MILLISECONDS);

        final Map map = consume(metric);

        assertTrue(map.containsKey("@timestamp"));
        assertTrue(map.containsKey("name"));
        assertTrue(map.containsKey("type"));
        assertTrue(map.containsKey("signature"));
        assertTrue(map.containsKey("gid"));
        assertTrue(map.containsKey("count"));
        assertTrue(map.containsKey("m1_rate"));
        assertTrue(map.containsKey("m5_rate"));
        assertTrue(map.containsKey("m15_rate"));
        assertTrue(map.containsKey("mean_rate"));
        assertTrue(map.containsKey("m1_count"));
        assertTrue(map.containsKey("m5_count"));
        assertTrue(map.containsKey("m15_count"));
        assertTrue(map.containsKey("max"));
        assertTrue(map.containsKey("mean"));
        assertTrue(map.containsKey("min"));
        assertTrue(map.containsKey("std"));
        assertTrue(map.containsKey("median"));
        assertTrue(map.containsKey("p25"));
        assertTrue(map.containsKey("p75"));
        assertTrue(map.containsKey("p95"));
        assertTrue(map.containsKey("p98"));
        assertTrue(map.containsKey("p99"));
        assertTrue(map.containsKey("p999"));
    }

    private Map consume(Metric metric) throws IOException {
        return mapper.readValue(consume(metric, ""), Map.class);
    }

    private String consume(Metric metric, String bulk) throws IOException {
        final JsonFactory factory = new JsonFactory(mapper);
        final long timestamp = System.currentTimeMillis();
        return new MetricsXReport.Consumer(factory, timestamp, hostInfo, bulk).asJson("meter", tags, metric);
    }

}