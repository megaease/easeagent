package com.megaease.easeagent.report.trace;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.ConfigChangeListener;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.Category;
import com.megaease.easeagent.report.DataItem;
import com.megaease.easeagent.report.Processor;

import java.util.List;

public class TraceProcessor implements Processor, ConfigChangeListener {
    public TraceProcessor(Configs configs) {

    }

    @Override
    public boolean support(DataItem item) {
        return Category.Trace == item.getCategory();
    }

    @Override
    public void process(DataItem item) {

    }

    @Override
    public void onChange(List<ChangeItem> list) {

    }
}
