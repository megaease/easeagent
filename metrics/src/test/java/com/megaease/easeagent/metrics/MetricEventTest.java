package com.megaease.easeagent.metrics;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MetricEventTest {
    @Test
    public void should_work() throws Exception {
        final MetricRegistry registry = new MetricRegistry();

        final String name = "jdbc_statement";
        final Timer timer = registry.timer(name);

        timer.update(1, MILLISECONDS);

        final MetricEvent event = new MetricEvent(timer, name, ImmutableMap.<String, String>builder()
                .put("signature", "NotifyTaskExecutorServiceImpl#sendNotifyMail")
                .build(), TimeUnit.SECONDS, TimeUnit.MILLISECONDS);

        final JSONObject jsono = JSON.parseObject(JSON.toJSONString(event));

        assertThat(jsono.getString("name"), is(jsono.getString("type")));
        assertThat(jsono.containsKey("@timestamp"), is(true));
        assertThat(jsono.containsKey("signature"), is(true));

        assertThat(jsono.containsKey("count"), is(true));
        assertThat(jsono.containsKey("m1_rate"), is(true));
        assertThat(jsono.containsKey("m5_rate"), is(true));
        assertThat(jsono.containsKey("m15_rate"), is(true));
        assertThat(jsono.containsKey("mean_rate"), is(true));
        assertThat(jsono.containsKey("min"), is(true));
        assertThat(jsono.containsKey("max"), is(true));
        assertThat(jsono.containsKey("mean"), is(true));
        assertThat(jsono.containsKey("median"), is(true));
        assertThat(jsono.containsKey("std"), is(true));
        assertThat(jsono.containsKey("p25"), is(true));
        assertThat(jsono.containsKey("p75"), is(true));
        assertThat(jsono.containsKey("p95"), is(true));
        assertThat(jsono.containsKey("p98"), is(true));
        assertThat(jsono.containsKey("p99"), is(true));
        assertThat(jsono.containsKey("p999"), is(true));
    }
}