package com.megaease.easeagent.zipkin.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class RequestInfo {

    @JsonProperty("span_id")
    protected String spanId;

    @JsonProperty("trace_id")
    protected String traceId;

    @JsonProperty("pspan_id")
    protected String parentSpanId;

    private String type = "access-log";

    private String system;

    private String service;

    @JsonProperty("client_ip")
    private String clientIP = "-";

    private String user = "-";

    @JsonProperty("response_size")
    private int responseSize;

    private long beginTime;

    private long beginCpuTime;

    @JsonProperty("request_time")
    private long requestTime;

    private long cpuElapsedTime;

    private String url;

    private String method;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("host_name")
    private String hostName;

    @JsonProperty("host_ipv4")
    private String hostIpv4;

    private String category;

    @JsonProperty("match_url")
    private String matchUrl;

    private Map<String, String> headers;

    private Map<String, List<String>> queries;
}
