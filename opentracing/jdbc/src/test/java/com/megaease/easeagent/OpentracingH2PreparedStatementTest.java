package com.megaease.easeagent;

import brave.Tracer;
import brave.internal.Platform;
import brave.opentracing.BraveTracer;
import net.bytebuddy.description.type.TypeDescription;
import org.h2.jdbc.JdbcConnection;
import org.junit.Test;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.lang.reflect.Constructor;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;

import static com.megaease.easeagent.TraceContext.init;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class OpentracingH2PreparedStatementTest {
    public static final String JDBC_URL = "jdbc:h2:mem:test";
    public static final String SQL = "select 1";

    final Transformation.Feature feature = new OpentracingH2PreparedStatement().feature(null);

    @Test
    public void should_get_a_span_about_the_statement() throws Exception {

        final Reporter<Span> reporter = spy(new Reporter<Span>() {
            @Override
            public void report(Span span) {
                final Endpoint endpoint = Platform.get().localEndpoint();
                assertThat(span.name, is("jdbc_statement"));
                assertThat(span.binaryAnnotations, hasItems(
                        BinaryAnnotation.create("component", "jdbc", endpoint),
                        BinaryAnnotation.create("span.kind", "client", endpoint),
                        BinaryAnnotation.create("jdbc.url", JDBC_URL, endpoint),
                        BinaryAnnotation.create("jdbc.sql", SQL, endpoint),
                        BinaryAnnotation.create("jdbc.result", "true", endpoint),
                        BinaryAnnotation.create("has.error", "false", endpoint),
                        BinaryAnnotation.create("remote.address", "null", endpoint)
                ));
            }
        });

        init(BraveTracer.wrap(Tracer.newBuilder().reporter(reporter).build()));


        final ClassLoader loader = getClass().getClassLoader();
        final Class<PreparedStatement> load = Classes.<PreparedStatement>transform("org.h2.jdbc.JdbcPreparedStatement", loader)
                .by(feature).load();

        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(load)));

        final Constructor<PreparedStatement> constructor = load.getDeclaredConstructor(JdbcConnection.class, String.class, int.class, int.class, int.class, boolean.class);
        constructor.setAccessible(true);

        final JdbcConnection connection = mock(JdbcConnection.class, RETURNS_DEEP_STUBS);
        final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getURL()).thenReturn(JDBC_URL);
        when(connection.getMetaData()).thenReturn(metaData);

        final PreparedStatement stmt = constructor.newInstance(connection, SQL, 1, 1, 1, false);

        try { stmt.execute(); } catch (Exception ignore) { }

        verify(reporter).report(any(Span.class));
    }

}