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

package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.utils.ContextUtils;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class JdbcDataSourceAdvice implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(isSubTypeOf(DataSource.class))
                .transform(getConnection(named("getConnection").and(returns(isSubTypeOf(Connection.class)))))
                .end();
    }

    @AdviceTo(GetConnection.class)
    abstract Definition.Transformer getConnection(ElementMatcher<? super MethodDescription> getConnection);

    static class GetConnection {

        private final ForwardLock lock;
        private final Logger logger;
        private final AgentInterceptor agentInterceptor;

        @Injection.Autowire
        GetConnection(@Injection.Qualifier("agentInterceptor4Con") AgentInterceptor agentInterceptor4Con) {
            this.lock = new ForwardLock();
            logger = LoggerFactory.getLogger(getClass());
            this.agentInterceptor = agentInterceptor4Con;
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(@Advice.This DataSource dataSource,
                                                       @Advice.Origin("#m") String method,
                                                       @Advice.AllArguments Object[] args) {
            return lock.acquire(() -> {
                Map<Object, Object> map = ContextUtils.createContext();
                agentInterceptor.before(dataSource, method, args, map);
                return map;
            });
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.This DataSource dataSource,
                  @Advice.AllArguments Object[] args,
                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object retValue,
                  @Advice.Origin("#m") String method,
                  @Advice.Thrown Exception exception) {
            release.apply(map -> {
                try {
                    ContextUtils.setEndTime(map);
                    this.agentInterceptor.after(dataSource, method, args, retValue, exception, map);
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            });
        }
    }

}
