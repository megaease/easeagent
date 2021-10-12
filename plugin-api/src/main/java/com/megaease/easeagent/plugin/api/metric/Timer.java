package com.megaease.easeagent.plugin.api.metric;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface Timer {

    interface Context {

        long stop();

        void close();
    }

    void update(long duration, TimeUnit unit);

    void update(Duration duration);

    <T> T time(Callable<T> event) throws Exception;

    <T> T timeSupplier(Supplier<T> event);

    void time(Runnable event);

    Timer.Context time();

    long getCount();

    double getFifteenMinuteRate();

    double getFiveMinuteRate();

    double getMeanRate();

    double getOneMinuteRate();

    Snapshot getSnapshot();
}
