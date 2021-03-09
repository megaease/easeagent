package com.megaease.easeagent.report.util;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.ConfigConst;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.metric.MetricProps;
import com.megaease.easeagent.report.trace.TraceProps;

import java.util.Arrays;
import java.util.List;

public class Utils {
    public static boolean isOutputPropertiesChange(List<ChangeItem> list) {
        List<String> relatedNames = Arrays.asList(ConfigConst.OUTPUT_ENABLED, ConfigConst.OUTPUT_SERVERS, ConfigConst.OUTPUT_TIMEOUT);
        boolean hasChange = list.stream().map(ChangeItem::getFullName)
                .anyMatch(relatedNames::contains);
        return hasChange;
    }

    public static boolean isTraceOutputPropertiesChange(List<ChangeItem> list) {
        boolean hasChange = list.stream().map(ChangeItem::getFullName)
                .anyMatch(name -> name.startsWith(ConfigConst.TRACE_OUTPUT + ConfigConst.DELIMITER));
        return hasChange;
    }

    public static OutputProperties extractOutputProperties(Configs configs) {
        return OutputProperties.newDefault(configs);
    }

    public static TraceProps extractTraceProps(Configs configs) {
        return TraceProps.newDefault(configs);
    }

    public static MetricProps extractMetricProps(Configs configs, String key) {
        return MetricProps.newDefault(configs, key);
    }


}
