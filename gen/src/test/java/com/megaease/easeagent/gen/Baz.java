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

 package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Transformation;
import net.bytebuddy.asm.Advice.Enter;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public abstract class Baz implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return null;
    }

    @AdviceTo(Demo.class)
    abstract Definition.Transformer demo(ElementMatcher<? super MethodDescription> matcher);

    abstract static class Demo {
        @OnMethodEnter
        boolean enter(@This Object self) {
            return false;
        }

        @OnMethodExit(onThrowable = Throwable.class)
        void exit(@Enter boolean flag) { }
    }

    static class Ignore {}
}
