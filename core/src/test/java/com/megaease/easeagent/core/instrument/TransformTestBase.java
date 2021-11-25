/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.core.instrument;

import com.megaease.easeagent.core.plugin.interceptor.ProviderChain;
import com.megaease.easeagent.core.plugin.interceptor.ProviderPluginDecorator;
import com.megaease.easeagent.core.plugin.matcher.MethodMatcherConvert;
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import com.megaease.easeagent.core.plugin.registry.QualifierRegistry;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.Provider;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class TransformTestBase {
    protected static final String FOO = "foo",
        BAR = "bar",
        QUX = "qux",
        CLASS_INIT = "<clinit>",
        FOO_STATIC = "fooStatic";

    @SuppressWarnings("all")
    protected Set<MethodTransformation> getMethodTransformations(int index,
                                                               String methodName,
                                                               Provider provider) {
        IMethodMatcher m = MethodMatcher.builder().named(methodName).build();
        return getMethodTransformations(index, m, provider);
    }

    @SuppressWarnings("all")
    protected Set<MethodTransformation> getMethodTransformations(int index,
                                                                 IMethodMatcher m,
                                                                 Provider provider) {
        ProviderChain.Builder providerBuilder = ProviderChain.builder();
        providerBuilder.addProvider(new ProviderPluginDecorator(new TestPlugin(), provider));

        MethodTransformation methodTransformation = new MethodTransformation(index,
            MethodMatcherConvert.INSTANCE.convert(m),
            providerBuilder);
        QualifierRegistry.addMethodTransformation(index, methodTransformation);

        Set<MethodTransformation> transformations = new HashSet<>();
        transformations.add(methodTransformation);

        return transformations;
    }

    public static class FooInstInterceptor implements Interceptor {
        @Override
        public void before(MethodInfo methodInfo, Context context) {
            Object [] args = methodInfo.getArgs();
            args[0] = QUX;
            methodInfo.markChanged();
        }

        @Override
        public void after(MethodInfo methodInfo, Context context) {
        }

        @Override
        public int order() {
            return Order.HIGHEST.getOrder();
        }
    }

    public static class FooInterceptor implements Interceptor {
        @Override
        public void before(MethodInfo methodInfo, Context context) {
            Object [] args = methodInfo.getArgs();
            args[0] = QUX;
            methodInfo.markChanged();
        }

        @Override
        public void after(MethodInfo methodInfo, Context context) {
            methodInfo.setRetValue(methodInfo.getRetValue() + BAR);
        }

        @Override
        public int order() {
            return Order.HIGHEST.getOrder();
        }
    }

    public static class FooSecondInterceptor implements Interceptor {
        @Override
        public void before(MethodInfo methodInfo, Context context) {
            Object [] args = methodInfo.getArgs();
            args[0] = BAR;
            methodInfo.markChanged();
        }

        @Override
        public void after(MethodInfo methodInfo, Context context) {
            methodInfo.setRetValue(methodInfo.getRetValue() + QUX);
        }

        @Override
        public int order() {
            return Order.LOW.getOrder();
        }
    }

    static class FooProvider implements Provider {
        @Override
        public Supplier<Interceptor> getInterceptorProvider() {
            return FooInterceptor::new;
        }

        @Override
        public String getAdviceTo() {
            return "";
        }

        @Override
        public String getPluginClassName() {
            return TestPlugin.class.getCanonicalName();
        }
    }

    static class FooInstProvider implements Provider {
        @Override
        public Supplier<Interceptor> getInterceptorProvider() {
            return FooInstInterceptor::new;
        }

        @Override
        public String getAdviceTo() {
            return "";
        }

        @Override
        public String getPluginClassName() {
            return TestPlugin.class.getCanonicalName();
        }
    }

    static class FooSecProvider implements Provider {
        @Override
        public Supplier<Interceptor> getInterceptorProvider() {
            return FooSecondInterceptor::new;
        }

        @Override
        public String getAdviceTo() {
            return "";
        }

        @Override
        public String getPluginClassName() {
            return TestPlugin.class.getCanonicalName();
        }
    }
}
