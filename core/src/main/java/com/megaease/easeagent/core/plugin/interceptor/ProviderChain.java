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

package com.megaease.easeagent.core.plugin.interceptor;

import com.megaease.easeagent.plugin.interceptor.InterceptorProvider;
import com.megaease.easeagent.plugin.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProviderChain {
    private final List<InterceptorProvider> providers;

    ProviderChain(List<InterceptorProvider> providers) {
        this.providers = providers;
    }

    public List<Supplier<Interceptor>> getSupplierChain() {
        return this.providers.stream()
            .map(InterceptorProvider::getInterceptorProvider)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("all")
    public static class Builder {
        private List<InterceptorProvider> providers = new ArrayList<>();

        Builder() {
        }

        public Builder providers(List<InterceptorProvider> providers) {
            this.providers = providers;
            return this;
        }

        public Builder addProvider(InterceptorProvider supplier) {
            this.providers.add(supplier);
            return this;
        }

        public ProviderChain build() {
            return new ProviderChain(providers);
        }

        @Override
        public String toString() {
            return "ProviderChain.Builder(providers=" + this.providers + ")";
        }
    }
}
