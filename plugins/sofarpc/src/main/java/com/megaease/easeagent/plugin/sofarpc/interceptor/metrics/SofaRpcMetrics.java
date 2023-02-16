package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics;

import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.MetricValueFetcher;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.tools.metrics.LastMinutesCounterGauge;
import com.megaease.easeagent.plugin.utils.ImmutableMap;

import java.util.concurrent.TimeUnit;

public class SofaRpcMetrics extends ServiceMetric {

	public static final ServiceMetricSupplier<SofaRpcMetrics> SOFARPC_METRICS_SUPPLIER = new ServiceMetricSupplier<SofaRpcMetrics>() {
		@Override
		public NameFactory newNameFactory() {
			return nameFactory();
		}

		@Override
		public SofaRpcMetrics newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
			return new SofaRpcMetrics(metricRegistry, nameFactory);
		}
	};

	public SofaRpcMetrics(MetricRegistry metricRegistry, NameFactory nameFactory) {
		super(metricRegistry, nameFactory);
	}

	private static NameFactory nameFactory() {
		return NameFactory.createBuilder()
				.timerType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.MIN_EXECUTION_TIME, MetricValueFetcher.SnapshotMinValue)
								.put(MetricField.MAX_EXECUTION_TIME, MetricValueFetcher.SnapshotMaxValue)
								.put(MetricField.MEAN_EXECUTION_TIME, MetricValueFetcher.SnapshotMeanValue)
								.put(MetricField.P25_EXECUTION_TIME, MetricValueFetcher.Snapshot25Percentile)
								.put(MetricField.P50_EXECUTION_TIME, MetricValueFetcher.Snapshot50PercentileValue)
								.put(MetricField.P75_EXECUTION_TIME, MetricValueFetcher.Snapshot75PercentileValue)
								.put(MetricField.P95_EXECUTION_TIME, MetricValueFetcher.Snapshot95PercentileValue)
								.put(MetricField.P98_EXECUTION_TIME, MetricValueFetcher.Snapshot98PercentileValue)
								.put(MetricField.P99_EXECUTION_TIME, MetricValueFetcher.Snapshot99PercentileValue)
								.put(MetricField.P999_EXECUTION_TIME, MetricValueFetcher.Snapshot999PercentileValue)
								.build())
				.meterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1Rate)
								.put(MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate)
								.put(MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate)
								.put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
								.build())
				.meterType(MetricSubType.ERROR,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
								.put(MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
								.put(MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
								.build())
				.counterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
								.build())
				.counterType(MetricSubType.ERROR,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount)
								.build())
				.gaugeType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.build())
				.build();
	}

	public void collect(String key, long duration, boolean success) {

		this.timer(key, MetricSubType.DEFAULT).update(duration, TimeUnit.MILLISECONDS);
		final Meter meter = this.meter(key, MetricSubType.DEFAULT);
		final Counter counter = this.counter(key, MetricSubType.DEFAULT);
		meter.mark();
		counter.inc();

		if (!success) {
			final Meter errorMeter = this.meter(key, MetricSubType.ERROR);
			final Counter errorCounter = this.counter(key, MetricSubType.ERROR);
			errorMeter.mark();
			errorCounter.inc();
		}

		this.gauge(key, MetricSubType.DEFAULT, new MetricSupplier<Gauge>() {
			@Override
			public Gauge<LastMinutesCounterGauge> newMetric() {
				return () -> LastMinutesCounterGauge.builder()
						.m1Count((long) (meter.getOneMinuteRate() * 60))
						.m5Count((long) (meter.getFiveMinuteRate() * 60 * 5))
						.m15Count((long) (meter.getFifteenMinuteRate() * 60 * 15))
						.build();
			}
		});
	}
}
