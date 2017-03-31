package com.megaease.easeagent;

import brave.Tracer;
import brave.internal.Platform;
import brave.opentracing.BraveTracer;
import com.mysql.jdbc.MySQLConnection;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;

import static com.megaease.easeagent.TraceContext.init;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class OpentracingMySQLPreparedStatementTest {
    public static final String JDBC_URL = "jdbc:mysql://localhost/test";

    final Transformation.Feature feature = new OpentracingMySQLPreparedStatement().feature(null);

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
                        BinaryAnnotation.create("jdbc.sql", "sql", endpoint),
                        BinaryAnnotation.create("jdbc.result", "false", endpoint),
                        BinaryAnnotation.create("has.error", "true", endpoint),
                        BinaryAnnotation.create("remote.address", "localhost:3306", endpoint)
                ));
            }
        });

        init(BraveTracer.wrap(Tracer.newBuilder().reporter(reporter).build()));


        final ClassLoader loader = getClass().getClassLoader();
        final Class<PreparedStatement> load = Classes.<PreparedStatement>transform("com.mysql.jdbc.PreparedStatement", loader)
                .by(feature).load();

        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(load)));

        final MySQLConnection connection = mock(MySQLConnection.class, RETURNS_DEEP_STUBS);
        final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getURL()).thenReturn(JDBC_URL);
        when(connection.getMetaData()).thenReturn(metaData);

        final PreparedStatement stmt = load.getConstructor(MySQLConnection.class, String.class, String.class)
                                           .newInstance(connection, "sql", "cat");

        try { stmt.execute(); } catch (Exception ignore) { }

        verify(reporter).report(any(Span.class));
    }

}