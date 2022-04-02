package com.megaease.easeagent.plugin.api.otlp.common;

import com.megaease.easeagent.plugin.report.EncodedData;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.EaseAgentResource;

import java.util.Map;

public interface AgentLogData extends LogData {
    /**
     * get logger thread name
     * @return thread name
     */
    String getThreadName();

    /**
     * get logger name
     * @return logger name
     */
    String getLocation();

    /**
     * get unix timestamp in milliseconds
     * @return
     */
    long getEpochMillis();

    /**
     * get agent resource - system/service
     * @return agent resource
     */
    EaseAgentResource getAgentResource();

    /**
     * return pattern map
     * @return pattern map
     */
    Map<String, String> getPatternMap();

    /**
     * return encoded data
     * @return encoded data
     */
    EncodedData getEncodedData();

    void setEncodedData(EncodedData data);
}
