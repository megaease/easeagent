package com.megaease.easeagent.plugin.springweb;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Provider;
import com.megaease.easeagent.plugin.annotation.ProviderBean;
import com.megaease.easeagent.plugin.springweb.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.springweb.interceptor.DoFilterInterceptor;

import java.util.function.Supplier;

public class SpringWebFilterProviders {
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
            return SpringWebFilterPlugin.class.getCanonicalName();
        }
    }

}
