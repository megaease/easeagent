/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.rabbitmq.v5.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class RabbitMqChannelAdvice implements Points {
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder().hasInterface("com.rabbitmq.client.Channel")
                .notAbstract()
                .notInterface()
                .build();
    }

    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
                .match(MethodMatcher.builder().named("basicPublish")
                        .isPublic()
                        .argsLength(6)
                        .returnType("void")
                        .qualifier("basicPublish")
                        .build())
                .match(MethodMatcher.builder().named("basicConsume")
                        .isPublic()
                        .argsLength(7)
                        .qualifier("basicConsume")
                        .build())
                .build();
    }

    public boolean isAddDynamicField() {
        return false;
    }

    /*
    @AdviceTo(value = RabbitMqChannelAdvice.class, qualifier = "basicPublish")
    public static class PublishInterceptor implements Interceptor {
        public void before(MethodInfo methodInfo, Map<Object, Object> context) {
            return;
        }

        public Object after(MethodInfo methodInfo, Map<Object, Object> context) {
            return null;
        }
    }

    @AdviceTo(value = RabbitMqChannelAdvice.class, qualifier = "basicConsume")
    public static class ConsumeInterceptor implements Interceptor {
        public void before(MethodInfo methodInfo, Map<Object, Object> context) {
            return;
        }

        public Object after(MethodInfo methodInfo, Map<Object, Object> context) {
            return null;
        }
    }
    */

    /**
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def
                .type(hasSuperType(named("com.rabbitmq.client.Channel")).and(not(isInterface().or(isAbstract()))))
                .transform(objConstruct(none(), AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
                .transform(doBasicPublish(named("basicPublish")
                        .and(takesArguments(6))
                        .and(isPublic()).and(returns(TypeDescription.VOID))))
                .transform(doBasicConsume(named("basicConsume")
                        .and(takesArguments(7))
                        .and(isPublic())))
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

    @AdviceTo(DoBasicPublish.class)
    public abstract Definition.Transformer doBasicPublish(ElementMatcher<? super MethodDescription> matcher);

    static class DoBasicPublish extends AbstractAdvice {
        @Injection.Autowire
        public DoBasicPublish(@Injection.Qualifier("supplier4RabbitMqBasicPublish") Supplier<AgentInterceptorChain.Builder> supplier,
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
                         @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
                         @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }

    @AdviceTo(DoBasicConsume.class)
    public abstract Definition.Transformer doBasicConsume(ElementMatcher<? super MethodDescription> matcher);

    static class DoBasicConsume extends AbstractAdvice {

        @Injection.Autowire
        public DoBasicConsume(@Injection.Qualifier("supplier4RabbitMqBasicConsume") Supplier<AgentInterceptorChain.Builder> supplier,
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
                         @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
                         @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }
    */
}
