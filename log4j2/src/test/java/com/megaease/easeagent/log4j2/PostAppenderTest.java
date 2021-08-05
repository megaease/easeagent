/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.log4j2;

public class PostAppenderTest {
//    static {
//        System.setProperty("javax.net.ssl.trustStore", "./src/test/resources/client.jks");
//        System.setProperty("javax.net.ssl.trustStorePassword", "666666");
//    }
//
//    @Rule
//    public ExpectedException thrown = ExpectedException.none();
//
//    @Test
//    public void should_post_by_http() throws Exception {
//        final HttpServer server = httpServer(8080, log());
//        server.post(and(
//                by(uri("/requests")),
//                eq(header("Content-Type"), "text/plain; charset=utf-8"),
//                by(text("message"))
//        )).response(status(200));
//
//        running(server, () -> LogManager.getLogger("http").info("message"));
//    }
//
//    @Test()
//    public void should_complaint_error() {
//        thrown.expect(AppenderLoggingException.class);
//        thrown.expectCause(IsInstanceOf.<Throwable>instanceOf(ConnectException.class));
//        try {
//            LogManager.getLogger("https").info("message");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        LogManager.getLogger("https").info("message");
//    }
//
//    @Test
//    public void should_post_by_https() throws Exception {
//        final HttpsCertificate certificate = certificate(pathResource("server.jks"), "666666", "666666");
//
//        final HttpsServer server = httpsServer(8443, certificate, log());
//
//        server.post(and(
//                by(uri("/requests")),
//                eq(header("Content-Type"), "text/plain; charset=utf-8"),
//                by(text("message"))
//        )).response(status(200));
//
//
//        running(server, () -> LogManager.getLogger("https").info("message"));
//    }
//
//    @Test
//    public void should_failover_exception() throws Exception {
//        final HttpServer server = httpServer(8080, log());
//        server.post(and(
//                by(uri("/requests")),
//                eq(header("Content-Type"), "text/plain; charset=utf-8"),
//                by(text("message"))
//        )).response(seq(status(503), status(200)));
//
//        running(server, () -> {
//            LogManager.getLogger("http").info("message");
//            Thread.sleep(1100);
//            LogManager.getLogger("http").info("message");
//        });
//
//    }
}