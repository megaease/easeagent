package com.megaease.easeagent.report;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.metric.MetricReporter;
import com.megaease.easeagent.report.metric.MetricReporterImpl;
import com.megaease.easeagent.report.trace.TraceReport;
import zipkin2.Span;

public class DefaultAgentReport implements AgentReport {
    private TraceReport traceReport;
    private MetricReporter metricReporter;

    public DefaultAgentReport(TraceReport traceReport, MetricReporter metricReporter) {
        this.traceReport = traceReport;
        this.metricReporter = metricReporter;
    }

    public static AgentReport create(Configs config) {
        return new DefaultAgentReport(new TraceReport(config), MetricReporterImpl.create(config));
    }

    @Override
    public void report(Span span) {
        this.traceReport.report(span);
    }

    @Override
    public MetricReporter metricReporter() {
        return this.metricReporter;
    }
}
