package com.megaease.easeagent.plugin.httpfilter;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Provider;
import com.megaease.easeagent.plugin.annotation.ProviderBean;
import com.megaease.easeagent.plugin.httpfilter.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.httpfilter.interceptor.DoFilterTraceInterceptor;

import java.util.function.Supplier;

public class HttpFilterProviders {
    @ProviderBean
    public static class DoFilterProvider implements Provider {
        @Override
        public Supplier<Interceptor> getInterceptorProvider() {
            return DoFilterTraceInterceptor::new;
        }

        @Override
        public String getAdviceTo() {
            return DoFilterAdvice.class.getCanonicalName()
                + ":default";
        }

        @Override
        public String getPluginClassName() {
            return HttpFilterPlugin.class.getCanonicalName();
        }
    }

}
