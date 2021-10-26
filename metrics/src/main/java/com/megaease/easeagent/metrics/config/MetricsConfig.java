package com.megaease.easeagent.metrics.config;

public interface MetricsConfig {
    boolean isEnabled();

    int getInterval();

    void setIntervalChangeCallback(Runnable runnable);
}
