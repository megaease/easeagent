package com.megaease.easeagent.report;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.metric.MetricItem;
import com.megaease.easeagent.report.metric.MetricReport;
import com.megaease.easeagent.report.trace.TraceReport;
import zipkin2.Span;

public interface AgentReport {
    void report(MetricItem item);

    void report(Span span);

    static AgentReport create(Configs config) {
        return new Default(new MetricReport(config), new TraceReport(config));
    }

    class Default implements AgentReport {
        private MetricReport metricReport;
        private TraceReport traceReport;

        public Default(MetricReport metricReport, TraceReport traceReport) {
            this.metricReport = metricReport;
            this.traceReport = traceReport;
        }

        @Override
        public void report(MetricItem item) {
            this.metricReport.report(item);
        }

        @Override
        public void report(Span span) {
            this.traceReport.report(span);
        }
    }
}
