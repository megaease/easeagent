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
