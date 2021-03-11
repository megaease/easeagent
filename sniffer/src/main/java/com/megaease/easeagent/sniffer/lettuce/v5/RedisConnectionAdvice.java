package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class RedisConnectionAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("io.lettuce.core.api.StatefulRedisConnection"))
                .and(not(isInterface()))
                .and(not(isAbstract()))
        )
//                .type(named("io.lettuce.core.StatefulRedisConnectionImpl")
//                .or(named("io.lettuce.core.cluster.StatefulRedisClusterPubSubConnectionImpl"))
//                .or(named("io.lettuce.core.masterslave.StatefulRedisMasterSlaveConnectionImpl"))
//                .or(named("io.lettuce.core.pubsub.StatefulRedisPubSubConnectionImpl"))
//        )
                .transform(objConstruct(none(), AgentFieldAccessor.FIELD_MAP_NAME))
                .end()
                ;
    }

    @AdviceTo(ObjConstruct.class)
    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fieldName);


    static class ObjConstruct extends AbstractAdvice {

        ObjConstruct() {
            super(null, null);
        }

        //        @Advice.OnMethodEnter
        public ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return innerEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public void exit(
//                @Advice.This Object invoker,
//                @Advice.Origin("#m") String method,
//                @Advice.AllArguments Object[] args,
                @Advice.Thrown Exception exception
        ) {
            System.out.println("");
        }
    }

}
