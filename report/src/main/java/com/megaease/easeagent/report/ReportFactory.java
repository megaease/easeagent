package com.megaease.easeagent.report;

import com.megaease.easeagent.config.Configs;

public class ReportFactory {
    public static ReportFacade getReportFacade(Configs configs) {
        return new ReportImpl(configs);
    }
}
