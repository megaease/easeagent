package com.megaease.easeagent.config;

import java.util.List;

public interface ConfigChangeListener {
    public void onChange(List<ChangeItem> list);
}
