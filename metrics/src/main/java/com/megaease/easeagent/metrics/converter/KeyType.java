package com.megaease.easeagent.metrics.converter;

/**
 * KeyType indicated how to fetch key value from which type metric of MetricRegistry, first we need to recognize what
 * is key of data?</p>
 * The key is a value which attached many metrics' value with it, for example:</p>
 * In <b>http-request</b>, we think the url is key, other metrics' value describe a special url properties, in
 * <b>jvm-memory</b> resource is key.
 */
public enum KeyType {
    Timer,
    Gauge,
    Counter,
    Histogram,
    Meter;
}
