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

 package com.megaease.easeagent.metrics;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class MeasureJdbcGetConnection implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(isSubTypeOf(DataSource.class))
                  .transform(getConnection(named("getConnection").and(returns(isSubTypeOf(Connection.class)))))
                  .end();
    }

    @AdviceTo(GetConnection.class)
    abstract Definition.Transformer getConnection(ElementMatcher<? super MethodDescription> getConnection);

    static class GetConnection {
        static final String GET_JDBC_CONNECTION = "get_jdbc_connection";
        static final String URL = "url";
        static final String ALL = "All";

        private final ForwardLock lock;
        private final Metrics metrics;
        private final Logger logger;

        @Injection.Autowire
        GetConnection(Metrics metrics) {
            this.lock = new ForwardLock();
            this.metrics = metrics;
            logger = LoggerFactory.getLogger(getClass());
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Long> enter() {
            return lock.acquire(new ForwardLock.Supplier<Long>() {
                @Override
                public Long get() {
                    return System.nanoTime();
                }
            });
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Long> release, @Advice.Return final Connection conn) {
            release.apply(new ForwardLock.Consumer<Long>() {
                @Override
                public void accept(Long begin) {
                    if (conn == null) return;
                    try {
                        final DatabaseMetaData meta = conn.getMetaData();
                        final long duration = System.nanoTime() - begin;
                        final String url = meta.getURL() + "-" + meta.getUserName();
                        metrics.timer(GET_JDBC_CONNECTION).tag(URL, url).get().update(duration, NANOSECONDS);
                        // TODO lazy calculation in streaming
                        metrics.timer(GET_JDBC_CONNECTION).tag(URL, ALL).get().update(duration, NANOSECONDS);
                    } catch (SQLException e) {
                        logger.error("Unexpected", e);
                    }
                }
            });
        }
    }

}
