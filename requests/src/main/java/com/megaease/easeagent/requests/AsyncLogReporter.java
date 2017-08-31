package com.megaease.easeagent.requests;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.megaease.easeagent.common.NamedDaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson.serializer.SerializerFeature.DisableCircularReferenceDetect;

class AsyncLogReporter implements Reporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLogReporter.class);

    private final BlockingQueue<TracedRequestEvent> queue;
    private final String hostipv4;
    private final String hostname;
    private final String system;
    private final String application;
    private final String type;
    private final boolean callstack;
    private final Logger logger;

    AsyncLogReporter(Logger logger, int capacity, String hostipv4, String hostname, String system, String application, String type, boolean callstack) {
        this.logger = logger;
        this.queue = new ArrayBlockingQueue<TracedRequestEvent>(capacity);
        this.hostipv4 = hostipv4;
        this.hostname = hostname;
        this.system = system;
        this.application = application;
        this.type = type;
        this.callstack = callstack;

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
            public String getName() {
                return context.getShortSignature();
            }

            @Override
            public String getType() {
                return type;
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
            public Context getCallStackJson() {
                return callstack ? context : Context.empty();
            }

            @Override
            public boolean getContainsCallTree() {
                return !getCallStackJson().getChildren().isEmpty();
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

        };

        queue.offer(event);
    }

    private static long sum(Iterable<Long> ios) {
        long sum = 0L;
        for (Long io : ios) {
            sum += io;
        }
        return sum;
    }

    private static ImmutableList.Builder<Long> collectIo(Context context, ImmutableList.Builder<Long> builder) {
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

