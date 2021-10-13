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

package com.megaease.easeagent.sniffer.rabbitmq.v5.advice;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.gen.Generate;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Generate.Advice
@Injection.Provider(Provider.class)
public abstract class RabbitMqConsumerAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def
                .type(hasSuperType(named("com.rabbitmq.client.Consumer")).and(not(isInterface().or(isAbstract()))))
                .transform(objConstruct(none(), AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
                .transform(doHandleDelivery(named("handleDelivery")))
                .end()
                ;
    }

    @AdviceTo(ObjConstruct.class)
    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fieldName);

    static class ObjConstruct extends AbstractAdvice {

        public ObjConstruct() {
            super(null, null);
        }

        @Advice.OnMethodExit
        public void exit() {

        }
    }


    @AdviceTo(DoHandleDelivery.class)
    public abstract Definition.Transformer doHandleDelivery(ElementMatcher<? super MethodDescription> matcher);

    static class DoHandleDelivery extends AbstractAdvice {
        @Injection.Autowire
        public DoHandleDelivery(@Injection.Qualifier("supplier4RabbitMqHandleDelivery") Supplier<AgentInterceptorChain.Builder> supplier,
                                AgentInterceptorChainInvoker chainInvoker) {
            super(supplier, chainInvoker);
        }

        @Advice.OnMethodEnter
        public ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                         @Advice.This Object invoker,
                         @Advice.Origin("#m") String method,
                         @Advice.AllArguments Object[] args,
                         @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }

}
