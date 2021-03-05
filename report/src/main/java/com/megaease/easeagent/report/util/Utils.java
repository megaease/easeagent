package com.megaease.easeagent.report.util;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.OutputProperties;

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
}
