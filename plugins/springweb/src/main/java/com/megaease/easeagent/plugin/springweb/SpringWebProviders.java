package com.megaease.easeagent.plugin.springweb;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Provider;
import com.megaease.easeagent.plugin.annotation.ProviderBean;
import com.megaease.easeagent.plugin.springweb.advice.FeignClientAdvice;
import com.megaease.easeagent.plugin.springweb.interceptor.FeignClientInterceptor;

import java.util.function.Supplier;

public class SpringWebProviders {

    @ProviderBean
    public static class FeignClientProvider implements Provider {
        @Override
        public Supplier<Interceptor> getInterceptorProvider() {
            return FeignClientInterceptor::new;
        }

        @Override
        public String getAdviceTo() {
            return FeignClientAdvice.class.getCanonicalName()
                + ":default";
        }

        @Override
        public String getPluginClassName() {
            return FeignClientPlugin.class.getCanonicalName();
        }
    }

}
