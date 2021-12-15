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

package com.megaease.easeagent.core.utils;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

/**
 * This rules assures that the running JVM is a JDK JVM with an available
 * <a href="https://blogs.oracle.com/CoreJavaTechTips/entry/the_attach_api">attach API</a>.
 */
public class AgentAttachmentRule implements MethodRule {
    private final boolean available;

    public AgentAttachmentRule() {
        available = ByteBuddyAgent.AttachmentProvider.DEFAULT.attempt().isAvailable();
    }

    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        Enforce enforce = method.getAnnotation(Enforce.class);
        if (enforce != null) {
            if (!available) {
                return new NoOpStatement("The executing JVM does not support runtime attachment");
            }
            Instrumentation instrumentation = ByteBuddyAgent.install(ByteBuddyAgent.AttachmentProvider.DEFAULT);
            if (enforce.redefinesClasses() && !instrumentation.isRedefineClassesSupported()) {
                return new NoOpStatement("The executing JVM does not support class redefinition");
            } else if (enforce.retransformsClasses() && !instrumentation.isRetransformClassesSupported()) {
                return new NoOpStatement("The executing JVM does not support class retransformation");
            } else if (enforce.nativeMethodPrefix() && !instrumentation.isNativeMethodPrefixSupported()) {
                return new NoOpStatement("The executing JVM does not support class native method prefixes");
            }
        }
        return base;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Enforce {

        boolean redefinesClasses() default false;

        boolean retransformsClasses() default false;

        boolean nativeMethodPrefix() default false;
    }

    private static class NoOpStatement extends Statement {

        private final String reason;

        private NoOpStatement(String reason) {
            this.reason = reason;
        }

        public void evaluate() {
            Logger.getLogger("net.bytebuddy").info("Omitting test case: " + reason);
        }
    }
}
