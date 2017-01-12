package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;

import java.util.Map;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public interface TracedRequest {
    String id();

    String type();

    String name();

    long executionTime();

    long executionTimeCpu();

    StackFrame callStackJson();

    boolean error();

    // TODO remove stagemonitor's legacy
    String callStack();

    // TODO remove stagemonitor's legacy
    boolean containsCallTree();

    // TODO remove stagemonitor's legacy
    long executionTimeDb();

    // TODO remove stagemonitor's legacy
    long executionCountDb();

    // TODO remove stagemonitor's legacy
    String uniqueVisitorId();

    // TODO remove stagemonitor's legacy
    Map<String, String> parameters();
}
