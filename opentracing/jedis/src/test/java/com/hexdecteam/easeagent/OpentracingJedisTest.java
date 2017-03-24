package com.hexdecteam.easeagent;

import brave.Tracer;
import brave.internal.Platform;
import brave.opentracing.BraveTracer;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import redis.clients.jedis.Protocol;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.lang.reflect.Method;

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

        final ClassLoader loader = getClass().getClassLoader();
        final Class<?> loaded = Classes.transform("redis.clients.jedis.Connection", loader)
                                     .by(feature).load();

        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(loaded)));

        final Method sendCommand = loaded.getDeclaredMethod("sendCommand", Protocol.Command.class);
        sendCommand.setAccessible(true);

        try { sendCommand.invoke(loaded.newInstance(), Protocol.Command.PING); } catch (Exception ignore) { }

        verify(reporter).report(any(Span.class));

    }
}