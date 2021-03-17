package com.megaease.easeagent.sniffer.jedis.v3;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class JedisAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("redis.clients.jedis.BinaryJedis")))
                .transform(doCommand(any()
                        .and(isOverriddenFrom(named("redis.clients.jedis.commands.JedisCommands")
                                .or(named("redis.clients.jedis.commands.AdvancedJedisCommands"))
                                .or(named("redis.clients.jedis.commands.BasicCommands"))
                                .or(named("redis.clients.jedis.commands.ClusterCommands"))
                                .or(named("redis.clients.jedis.commands.ModuleCommands"))
                                .or(named("redis.clients.jedis.commands.MultiKeyCommands"))
                                .or(named("redis.clients.jedis.commands.ScriptingCommands"))
                                .or(named("redis.clients.jedis.commands.SentinelCommands"))
                                .or(named("redis.clients.jedis.commands.BinaryJedisCommands"))
                                .or(named("redis.clients.jedis.commands.MultiKeyBinaryCommands"))
                                .or(named("redis.clients.jedis.commands.AdvancedBinaryJedisCommands"))
                                .or(named("redis.clients.jedis.commands.BinaryScriptingCommands"))
                        ))
                )).end();
    }

    @AdviceTo(DoCommand.class)
    public abstract Definition.Transformer doCommand(ElementMatcher<? super MethodDescription> matcher);


    static class DoCommand extends AbstractAdvice {

        @Injection.Autowire
        public DoCommand(@Injection.Qualifier("builder4Jedis") AgentInterceptorChain.Builder builder,
                         AgentInterceptorChainInvoker chainInvoker) {
            super(builder, chainInvoker);
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(@Advice.This Object invoker,
                                                       @Advice.Origin("#m") String method,
                                                       @Advice.AllArguments Object[] args) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.This Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Throwable throwable) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }
}
