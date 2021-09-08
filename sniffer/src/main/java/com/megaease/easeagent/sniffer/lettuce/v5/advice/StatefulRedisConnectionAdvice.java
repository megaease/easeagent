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

package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import com.megaease.easeagent.gen.Generate;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Generate.Advice
@Injection.Provider(Provider.class)
public abstract class StatefulRedisConnectionAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def
                .type((hasSuperType(named("io.lettuce.core.api.StatefulConnection")
                        .or(named("io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection"))
                ).and(not(isInterface().or(isAbstract())))))
                .transform(objConstruct(none(), AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
                .end()
                ;
    }

    @AdviceTo(ObjConstruct.class)
    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fileName);


    static class ObjConstruct extends AbstractAdvice {

        ObjConstruct() {
            super(null, null);
        }

        @Advice.OnMethodExit
        public void exit() {

        }
    }

}
