package com.hexdecteam.easeagent;

import brave.Tracer;
import brave.internal.Platform;
import brave.opentracing.BraveTracer;
import com.google.common.io.ByteStreams;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Protocol;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

import static com.hexdecteam.easeagent.TraceContext.init;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class OpentracingJedisTest {

    @Test
    public void should_get_a_span_about_the_command() throws Exception {
        final Transformation.Feature feature = new OpentracingJedis().feature(null);
        final Class<?> loaded = Classes.transform(Connection.class)
                                       .by(feature).load();

        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(loaded)));

        final ServerSocket server = new ServerSocket(0);
        final int port = server.getLocalPort();

        final Reporter<Span> reporter = spy(new Reporter<Span>() {
            @Override
            public void report(Span span) {
                final Endpoint endpoint = Platform.get().localEndpoint();
                assertThat(span.name, is("redis_command"));
                assertThat(span.binaryAnnotations, hasItems(
                        BinaryAnnotation.create("component", "jedis", endpoint),
                        BinaryAnnotation.create("span.kind", "client", endpoint),
                        BinaryAnnotation.create("redis.host", "localhost", endpoint),
                        BinaryAnnotation.create("redis.port", String.valueOf(port), endpoint),
                        BinaryAnnotation.create("redis.result", "true", endpoint)
                ));
            }
        });

        init(BraveTracer.wrap(Tracer.newBuilder().reporter(reporter).build()));

        final Thread thread = new Thread(new Runnable() {
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

        thread.start();

        final Object obj = loaded.getConstructor(String.class, int.class).newInstance("localhost", port);

        final Method sendCommand = loaded.getDeclaredMethod("sendCommand", Protocol.Command.class);
        sendCommand.setAccessible(true);
        sendCommand.invoke(obj, Protocol.Command.PING);

        loaded.getMethod("getStatusCodeReply").invoke(obj);

        thread.join();

        verify(reporter).report(any(Span.class));
    }

    @Test
    public void should_get_a_span_about_the_command_even_connect_failed() throws Exception {
        final Transformation.Feature feature = new OpentracingJedis().feature(null);
        final Class<?> loaded = Classes.transform(Connection.class).by(feature).load();

        final Reporter<Span> reporter = spy(new Reporter<Span>() {
            @Override
            public void report(Span span) {
                final Endpoint endpoint = Platform.get().localEndpoint();
                assertThat(span.name, is("redis_command"));
                assertThat(span.binaryAnnotations, hasItems(
                        BinaryAnnotation.create("component", "jedis", endpoint),
                        BinaryAnnotation.create("span.kind", "client", endpoint),
                        BinaryAnnotation.create("redis.host", "localhost", endpoint),
                        BinaryAnnotation.create("redis.port", "6379", endpoint),
                        BinaryAnnotation.create("redis.result", "false", endpoint)
                ));
            }
        });

        init(BraveTracer.wrap(Tracer.newBuilder().reporter(reporter).build()));

        final Method sendCommand = loaded.getDeclaredMethod("sendCommand", Protocol.Command.class);
        sendCommand.setAccessible(true);

        try { sendCommand.invoke(loaded.newInstance(), Protocol.Command.PING); } catch (Exception ignore) { }

        verify(reporter).report(any(Span.class));

    }
}