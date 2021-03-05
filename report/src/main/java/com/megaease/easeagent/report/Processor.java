package com.megaease.easeagent.report;

public interface Processor {
    boolean support(DataItem item);

    void process(DataItem item);
}
