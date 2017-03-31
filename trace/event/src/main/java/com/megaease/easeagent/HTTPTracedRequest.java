package com.megaease.easeagent;

import com.google.auto.service.AutoService;

import java.util.*;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class HTTPTracedRequest implements TracedRequest {

    private final String     id;
    private final String     name;
    private final long       timeElapse;
    private final long       cpuTimeElapse;
    private final StackFrame rootFrame;
    private final boolean    error;
    private final String     url;
    private final String     method;
    private final int        statusCode;
    private final long       timeDb;
    private final int        countDb;

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

        final List<Long> ioqs = new ArrayList<Long>();
        collectIoQueris(rootFrame, ioqs);

        this.timeDb = sumOf(ioqs);
        this.countDb = ioqs.size();
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
        return Collections.singletonMap("TODO", "@zhongl");
    }

    // TODO remove stagemonitor's legacy
    public Map<String, String> userAgent() {
        return Collections.singletonMap("TODO", "@zhongl");
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
        return timeDb;
    }

    @Override
    public long executionCountDb() {
        return countDb;
    }

    @Override
    public String uniqueVisitorId() {
        return "TODO @zhongl";  // TODO
    }

    @Override
    public Map<String, String> parameters() {
        return Collections.singletonMap("TODO", "@zhongl");
    }

    private long sumOf(List<Long> ioqs) {
        long sum = 0L;
        for (Long v : ioqs) {
            sum += v;
        }
        return sum;
    }

    private void collectIoQueris(StackFrame frame, List<Long> ioqs) {
        final List<StackFrame> children = frame.getChildren();
        if(children.isEmpty() && frame.getIoquery()) {
            ioqs.add(frame.getExecutionTime());
        }

        for (StackFrame child : children) {
            collectIoQueris(child, ioqs);
        }
    }
}
