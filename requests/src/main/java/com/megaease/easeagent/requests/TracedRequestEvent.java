package com.megaease.easeagent.requests;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

interface TracedRequestEvent {
    @JSONField(name = "@timestamp", format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    Date getTimestamp();

    String getId();

    String getName();

    String getType();

    String getMethod();

    String getUrl();

    @JSONField(serializeUsing = ContextSerializer.class)
    Context getCallStackJson();

    boolean getContainsCallTree();

    boolean getError();

    int getStatusCode();

    long getExecutionCountDb();

    long getExecutionTimeDb();

    long getExecutionTime();

    long getExecutionTimeCpu();

    String getHostipv4();

    String getHostname();

    String getSystem();

    String getApplication();

}