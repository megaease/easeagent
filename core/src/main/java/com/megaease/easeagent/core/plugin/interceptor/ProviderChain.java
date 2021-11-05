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

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Provider;

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProviderChain {
    private final ArrayList<Provider> providers;

    ProviderChain(ArrayList<Provider> providers) {
        this.providers = providers;
    }

    public ArrayList<Provider> getProviders() {
        return this.providers;
    }

    public ArrayList<Supplier<Interceptor>> getSupplierChain() {
        ArrayList<Supplier<Interceptor>> ss = this.providers.stream()
            .map(Provider::getInterceptorProvider)
            .collect(Collectors.toCollection(ArrayList::new));

        return ss;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ArrayList<Provider> providers = new ArrayList<>();

        Builder() {
        }

        public Builder providers(ArrayList<Provider> providers) {
            this.providers = providers;
            return this;
        }

        public Builder addProvider(Provider supplier) {
            this.providers.add(supplier);
            return this;
        }

        public ProviderChain build() {
            return new ProviderChain(providers);
        }

        public String toString() {
            return "ProviderChain.Builder(providers=" + this.providers + ")";
        }
    }
}
