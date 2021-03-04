package com.megaease.easeagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ConfigNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigNotifier.class);
    private final CopyOnWriteArrayList<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final String prefix;

    public ConfigNotifier(String prefix) {
        this.prefix = prefix;
    }

    public Runnable addChangeListener(ConfigChangeListener listener) {
        final boolean add = listeners.add(listener);
        return () -> {
            if (add) {
                listeners.remove(listener);
            }
        };
    }

    public void handleChanges(List<ChangeItem> list) {
        final List<ChangeItem> changes = this.prefix.isEmpty() ? list : filterChanges(list);
        if (changes.isEmpty()) {
            return;
        }
        listeners.forEach(one -> {
            try {
                one.onChange(changes);
            } catch (Exception e) {
                LOGGER.warn("Notify config changes to listener failure: {}", one.toString());
            }
        });
    }

    private List<ChangeItem> filterChanges(List<ChangeItem> list) {
        return list.stream().filter(one -> one.getFullName().startsWith(prefix))
                .map(e -> new ChangeItem(e.getFullName().substring(prefix.length()), e.getFullName(), e.getOldValue(), e.getNewValue()))
                .collect(Collectors.toList());
    }
}
