package com.hexdecteam.log4j2.appender;


import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.HttpsCertificate;
import com.github.dreamhead.moco.HttpsServer;
import com.github.dreamhead.moco.Runnable;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.github.dreamhead.moco.HttpsCertificate.certificate;
import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.Runner.running;

public class PostAppenderTest {
    static {
        System.setProperty("javax.net.ssl.trustStore", "./src/test/resources/client.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "666666");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void should_post_by_http() throws Exception {
        final HttpServer server = httpServer(8080, log());
        server.post(and(
                by(uri("/requests")),
                eq(header("Content-Type"), "text/plain"),
                by(text("message"))
        )).response(status(200));

        running(server, new Runnable() {
            public void run() throws Exception {
                LogManager.getLogger("http").info("message");
            }
        });
    }

    @Test()
    public void should_complaint_error() throws Exception {
        thrown.expect(AppenderLoggingException.class);
        thrown.expectCause(IsInstanceOf.<Throwable>instanceOf(HttpHostConnectException.class));
        LogManager.getLogger("http").info("message");
    }

    @Test
    public void should_post_by_https() throws Exception {
        final HttpsCertificate certificate = certificate(pathResource("server.jks"), "666666", "666666");

        final HttpsServer server = httpsServer(8443, certificate, log());

        server.post(and(
                by(uri("/requests")),
                eq(header("Content-Type"), "text/plain"),
                by(text("message"))
        )).response(status(200));


        running(server, new Runnable() {
            public void run() throws Exception {
                LogManager.getLogger("https").info("message");
            }
        });
    }

}