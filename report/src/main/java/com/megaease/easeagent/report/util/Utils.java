package com.megaease.easeagent.report.util;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.trace.TraceProps;

import java.util.List;

public class Utils {
    public static OutputProperties extractOutputProperties(Configs configs) {
        //todo extract output properties from configurations.
        return new OutputProperties() {
            @Override
            public String getServers() {
                return null;
            }

            @Override
            public String getTimeout() {
                return null;
            }
        };
    }

    public static boolean isOutputPropertiesChange(List<ChangeItem> list) {
        //todo
        return false;
    }

    public static TraceProps extractTraceProps(Configs configs) {
        return null;
    }

    public static String extractServiceName(Configs configs) {
        return null;
    }

    public static String extractSystemName(Configs configs) {
        return null;
    }

    public static boolean isTraceOutputPropertiesChange(List<ChangeItem> list) {
        return false;
    }
}
