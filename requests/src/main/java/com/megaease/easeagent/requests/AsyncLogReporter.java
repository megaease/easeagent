package com.megaease.easeagent.requests;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.megaease.easeagent.common.NamedDaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson.serializer.SerializerFeature.DisableCircularReferenceDetect;

class AsyncLogReporter implements Reporter {
    private static final long START_TIME = ManagementFactory.getRuntimeMXBean().getStartTime();
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLogReporter.class);

    private final BlockingQueue<TracedRequestEvent> queue;
    private final String hostipv4;
    private final String hostname;
    private final String system;
    private final String application;
    private final String type;
    private final Logger logger;

    AsyncLogReporter(Logger logger, int capacity, String hostipv4, String hostname, String system, String application, String type) {
        this.logger = logger;
        this.queue = new ArrayBlockingQueue<TracedRequestEvent>(capacity);
        this.hostipv4 = hostipv4;
        this.hostname = hostname;
        this.system = system;
        this.application = application;
        this.type = type;

        Executors.newSingleThreadExecutor(new NamedDaemonThreadFactory("easeagent-requests-report")).submit(new LoggingDaemon());
    }

    @Override
    public void report(final String url, final String method,
                       final int status, final Map<String, String> headers, final Map<String, String> queries, final Context context) {
        final List<Long> ios = collectIo(context, ImmutableList.<Long>builder()).build();
        final int countIo = ios.size();
        final long timeIo = sum(ios);
        final long timestamp = System.currentTimeMillis();

        final TracedRequestEvent event = new TracedRequestEvent() {
            @Override
            public Date getTimestamp() {
                return new Date(timestamp);
            }

            @Override
            public String getId() {
                return UUID.randomUUID().toString();
            }

            @Override
            public String getUniqueVisitorId() {
                return "@Deprecated";
            }

            @Override
            public String getName() {
                return context.getShortSignature();
            }

            @Override
            public String getType() {
                return type;
            }

            @Override
            public String getStatus() {
                return getError() ? "Error" : "OK";
            }

            @Override
            public String getMethod() {
                return method;
            }

            @Override
            public String getUrl() {
                return url;
            }

            @Override
            public String getCallStack() {
                return "";
            }

            @Override
            public Context getCallStackJson() {
                return context;
            }

            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }

            @Override
            public Map<String, String> getParameters() {
                return queries;
            }

            @Override
            public Map<String, String> getUserAgent() {
                return Collections.singletonMap("@Deprecated", "@Deprecated");
            }

            @Override
            public boolean getContainsCallTree() {
                return context.getChildren().size() > 0;
            }

            @Override
            public boolean getError() {
                return status >= 400;
            }

            @Override
            public int getStatusCode() {
                return status;
            }

            @Override
            public long getBytesWritten() {
                return -1;
            }

            @Override
            public long getExecutionCountDb() {
                return countIo;
            }

            @Override
            public long getExecutionTimeDb() {
                return timeIo;
            }

            @Override
            public long getExecutionTime() {
                return context.getExecutionTime();
            }

            @Override
            public long getExecutionTimeCpu() {
                return context.getExecutionCpuTime();
            }

            @Override
            public String getHostipv4() {
                return hostipv4;
            }

            @Override
            public String getHostname() {
                return hostname;
            }

            @Override
            public String getSystem() {
                return system;
            }

            @Override
            public String getApplication() {
                return application;
            }

            @Override
            public long getStartTime() {
                return START_TIME;
            }
        };

        queue.offer(event);
    }

    static long sum(Iterable<Long> ios) {
        long sum = 0L;
        for (Long io : ios) {
            sum += io;
        }
        return sum;
    }

    static ImmutableList.Builder<Long> collectIo(Context context, ImmutableList.Builder<Long> builder) {
        final Iterable<Context> children = context.getChildren();
        if (Iterables.isEmpty(children) && context.getIoquery()) {
            builder.add(context.getExecutionTime());
        }

        for (Context child : children) {
            builder = collectIo(child, builder);
        }
        return builder;
    }

    private class LoggingDaemon implements Runnable {

        @Override
        public void run() {
            for (; ; ) {
                try {
                    final TracedRequestEvent event = queue.poll(1, TimeUnit.SECONDS);
                    if (event == null) continue;
                    logger.info("{}\n", JSON.toJSONString(event, DisableCircularReferenceDetect));
                } catch (InterruptedException e) {
                    LOGGER.info("Interrupted to quit", e);
                    break;
                }
            }
        }
    }
}

