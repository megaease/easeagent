package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class HTTPTracedRequest implements TracedRequest {

    private final String              id;
    private final String              name;
    private final long                timeElapse;
    private final long                cpuTimeElapse;
    private final StackFrame          rootFrame;
    private final boolean             error;
    private final String              url;
    private final String              method;
    private final int                 statusCode;

    public HTTPTracedRequest(String id, String name, long timeElapse, long cpuTimeElapse, StackFrame rootFrame,
                             boolean error, String url, String method, int statusCode) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.name = name;
        this.timeElapse = timeElapse;
        this.cpuTimeElapse = cpuTimeElapse;
        this.rootFrame = rootFrame;
        this.error = error;
        this.url = url;
        this.method = method;
        this.statusCode = statusCode;
    }

    public String url() {
        return url;
    }

    public int statusCode() {
        return statusCode;
    }

    public String method() {
        return method;
    }

    // TODO remove stagemonitor's legacy
    public String status() {
        return error() ? "Error" : "OK";
    }

    // TODO remove stagemonitor's legacy
    public int bytesWritten() {
        return -1;
    }

    // TODO remove stagemonitor's legacy
    public Map<String, String> headers() {
        return Collections.singletonMap("TODO","@zhongl");
    }

    // TODO remove stagemonitor's legacy
    public Map<String, String> userAgent() {
        return Collections.singletonMap("TODO","@zhongl");
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String type() {
        return "http_request";
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long executionTime() {
        return timeElapse;
    }

    @Override
    public long executionTimeCpu() {
        return cpuTimeElapse;
    }

    @Override
    public StackFrame callStackJson() {
        return rootFrame;
    }

    @Override
    public boolean error() {
        return error;
    }

    @Override
    public String callStack() {
        return "TODO @zhongl";
    }

    @Override
    public boolean containsCallTree() {
        return callStackJson() != null;
    }

    @Override
    public long executionTimeDb() {
        return -1;
    }

    @Override
    public long executionCountDb() {
        return -1;
    }

    @Override
    public String uniqueVisitorId() {
        return "TODO @zhongl";  // TODO
    }

    @Override
    public Map<String, String> parameters() {
        return Collections.singletonMap("TODO","@zhongl");
    }
}
