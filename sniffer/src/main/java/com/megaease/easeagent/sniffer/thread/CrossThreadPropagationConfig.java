package com.megaease.easeagent.sniffer.thread;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigChangeListener;
import com.megaease.easeagent.config.ConfigConst;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CrossThreadPropagationConfig {
    private volatile String[] canaryHeaders = new String[0];
    private final Predicate<String> prefixPredicate = e -> e.startsWith(ConfigConst.GlobalCanaryLabels.SERVICE_HEADERS + ConfigConst.DELIMITER);

    public CrossThreadPropagationConfig(Config config) {
        this.resetData(config);
        config.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(List<ChangeItem> list) {
                boolean anyMatch = list.stream()
                        .map(ChangeItem::getFullName)
                        .anyMatch(prefixPredicate);
                if (anyMatch) {
                    resetData(config);
                }
            }
        });
    }

    private void resetData(Config config) {
        this.canaryHeaders = config.keySet().stream()
                .filter(prefixPredicate)
                .map(config::getString)
                .filter(Objects::nonNull).distinct().toArray(String[]::new);
    }

    public String[] getCanaryHeaders() {
        return canaryHeaders;
    }
}
