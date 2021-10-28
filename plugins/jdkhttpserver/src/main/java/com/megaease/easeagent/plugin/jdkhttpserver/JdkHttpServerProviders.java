package com.megaease.easeagent.plugin.jdkhttpserver;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Provider;
import com.megaease.easeagent.plugin.annotation.ProviderBean;
import com.megaease.easeagent.plugin.jdkhttpserver.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.jdkhttpserver.interceptor.DoFilterInterceptor;

import java.util.function.Supplier;

public class JdkHttpServerProviders {
    @ProviderBean
    public static class DoFilterProvider implements Provider {
        @Override
        public Supplier<Interceptor> getInterceptorProvider() {
            return DoFilterInterceptor::new;
        }

        @Override
        public String getAdviceTo() {
            return DoFilterAdvice.class.getCanonicalName()
                + ":default";
        }

        @Override
        public String getPluginClassName() {
            return JdkHttpServerPlugin.class.getCanonicalName();
        }
    }

}
