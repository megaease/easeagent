package com.megaease.easeagent.report;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.metric.MetricProcessor;
import com.megaease.easeagent.report.trace.TraceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

class ReportImpl implements ReportFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportImpl.class);
    private final List<Processor> processors;

    ReportImpl(Configs configs) {
        this.processors = Arrays.asList(new MetricProcessor(configs), new TraceProcessor(configs));
    }

    @Override
    public void report(DataItem item) {
        for (Processor one : processors) {
            if (one.support(item)) {
                try {
                    one.process(item);
                } catch (Exception e) {
                    LOGGER.warn("Process report data error: {}", item, e);
                }
            }
        }

    }
}
