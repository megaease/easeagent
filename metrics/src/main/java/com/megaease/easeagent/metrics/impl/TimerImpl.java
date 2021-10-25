package com.megaease.easeagent.metrics.impl;

import com.codahale.metrics.Timer;
import com.megaease.easeagent.plugin.api.metric.Snapshot;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimerImpl implements com.megaease.easeagent.plugin.api.metric.Timer {
    private final Timer timer;

    private TimerImpl(@Nonnull Timer timer) {
        this.timer = timer;
    }

    public static com.megaease.easeagent.plugin.api.metric.Timer build(Timer timer) {
        return timer == null ? NoOpMetrics.NO_OP_TIMER : new TimerImpl(timer);
    }

    @Override
    public void update(long duration, TimeUnit unit) {
        timer.update(duration, unit);
    }

    @Override
    public void update(Duration duration) {
        timer.update(duration);
    }

    @Override
    public <T> T time(Callable<T> event) throws Exception {
        return timer.time(event);
    }

    @Override
    public <T> T timeSupplier(Supplier<T> event) {
        return timer.timeSupplier(event);
    }

    @Override
    public void time(Runnable event) {
        timer.time(event);
    }

    @Override
    public Context time() {
        return new ContextImpl(timer.time());
    }

    @Override
    public long getCount() {
        return timer.getCount();
    }

    @Override
    public double getFifteenMinuteRate() {
        return timer.getFifteenMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return timer.getFiveMinuteRate();
    }

    @Override
    public double getMeanRate() {
        return timer.getMeanRate();
    }

    @Override
    public double getOneMinuteRate() {
        return timer.getOneMinuteRate();
    }

    @Override
    public Snapshot getSnapshot() {
        return SnapshotImpl.build(timer.getSnapshot());
    }


    public static class ContextImpl implements Context {
        private final Timer.Context context;

        ContextImpl(Timer.Context context) {
            this.context = context;
        }

        @Override
        public long stop() {
            return context.stop();
        }

        @Override
        public void close() {
            context.close();
        }
    }

}
