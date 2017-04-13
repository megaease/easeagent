package com.megaease.easeagent.requests;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.Map;

interface TracedRequestEvent {
    @JSONField(name = "@timestamp", format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    Date getTimestamp();

    String getId();

    // TODO remove stagemonitor's legacy
    String getUniqueVisitorId();

    String getName();

    String getType();

    // TODO remove stagemonitor's legacy
    String getStatus();

    String getMethod();

    String getUrl();

    // TODO remove stagemonitor's legacy
    String getCallStack();

    @JSONField(serializeUsing = ContextSerializer.class)
    Context getCallStackJson();

    Map<String, String> getHeaders();

    Map<String, String> getParameters();

    // TODO remove stagemonitor's legacy
    Map<String, String> getUserAgent();

    boolean getContainsCallTree();

    boolean getError();

    int getStatusCode();

    long getBytesWritten();

    long getExecutionCountDb();

    long getExecutionTimeDb();

    long getExecutionTime();

    long getExecutionTimeCpu();

    String getHostipv4();

    String getHostname();

    String getSystem();

    String getApplication();

    // TODO remove stagemonitor's legacy
    @JSONField(name = "measurement_start")
    long getStartTime();
}