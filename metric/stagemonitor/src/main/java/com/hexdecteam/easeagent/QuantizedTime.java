package com.hexdecteam.easeagent;

/**
 * Makes sure that values are always submitted at the same time on each server no matter when they where started (aka. quantization)
 * <p>
 *
 * @see <a href="https://blog.raintank.io/how-to-effectively-use-the-elasticsearch-data-source-and-solutions-to-common-pitfalls/#incomplete">how-to-effectively-use-the-elasticsearch-data-source-and-solutions-to-common-pitfalls</a>
 * @see <a href="https://blog.raintank.io/25-graphite-grafana-and-statsd-gotchas/#graphite.quantization">25-graphite-grafana-and-statsd-gotchas</a>
 *
 */
class QuantizedTime {
    private final long period;

    QuantizedTime(long period) {
        this.period = period;
    }

    long timestamp() {
        long now = System.currentTimeMillis();
        return now - (now % period);
    }

    long offset() {
        return period - (System.currentTimeMillis() % period);
    }

}
