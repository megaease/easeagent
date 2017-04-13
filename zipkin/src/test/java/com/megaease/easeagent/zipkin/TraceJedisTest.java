package com.megaease.easeagent.zipkin;

import brave.Tracer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import redis.clients.jedis.Protocol;
import zipkin.BinaryAnnotation;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
public class TraceJedisTest {


    @Test
    public void should_work() throws Exception {
        final CallTrace trace = new CallTrace();
        final Reporter<Span> reporter = mock(Reporter.class);
        final Tracer tracer = tracer(reporter);

        trace.push(tracer.newTrace().start());

        final Definition.Default def = new GenTraceJedis().define(Definition.Default.EMPTY);
        final Class<?> aClass = Classes.transform("redis.clients.jedis.Connection")
                                       .with(def, trace, new ForwardLock(), tracer)
                                       .load(getClass().getClassLoader()).get(0);

        final ServerSocket server = new ServerSocket(0);
        final int port = server.getLocalPort();
        final Thread thread = async(server);

        thread.start();


        final Object obj = aClass.getConstructor(String.class, int.class).newInstance("localhost", port);

        final Method sendCommand = aClass.getDeclaredMethod("sendCommand", Protocol.Command.class);
        sendCommand.setAccessible(true);
        sendCommand.invoke(obj, Protocol.Command.PING);

        aClass.getMethod("getStatusCodeReply").invoke(obj);

        thread.join();

        final ArgumentCaptor<Span> captor = ArgumentCaptor.forClass(Span.class);
        verify(reporter).report(captor.capture());
        final Span span = captor.getValue();
        assertThat(span.name, is("redis_command"));
        assertThat(span.annotations.get(0).value, is("cs"));
        assertThat(span.annotations.get(1).value, is("cr"));

        final Iterable<Map.Entry<String, String>> entries = ImmutableMap.<String, String>builder()
                .put("component", "jedis")
                .put("has.error", "false")
                .put("redis.cmd", "PING")
                .put("redis.host", "localhost")
                .put("redis.port", String.valueOf(port))
                .put("redis.result", "true")
                .put("remote.address", "localhost:" + String.valueOf(port))
                .put("span.kind", "client")
                .build().entrySet();
        assertThat(asEntries(span.binaryAnnotations), is(entries));
        trace.pop();
    }

    private Thread async(final ServerSocket server) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Socket socket = server.accept();
                    ByteStreams.readFully(socket.getInputStream(), new byte[10]);
                    socket.getOutputStream().write("+OK\r\n".getBytes());
                    socket.close();
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "testcase-runner");
    }

    private Iterable<Map.Entry<String, String>> asEntries(List<BinaryAnnotation> bas) {
        return from(bas).transform(new Function<BinaryAnnotation, Map.Entry<String, String>>() {
            @Override
            public Map.Entry apply(BinaryAnnotation input) {
                return new AbstractMap.SimpleEntry(input.key, new String(input.value));
            }
        }).toSet();
    }

    private Tracer tracer(Reporter<Span> reporter) {
        return Tracer.newBuilder().reporter(reporter).build();
    }

    private Map<String, Object> beans(Object... objects) {
        return from(objects).uniqueIndex(new Function<Object, String>() {
            @Override
            public String apply(Object input) {
                return input.getClass().getName();
            }
        });
    }

}