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
import com.megaease.easeagent.plugin.Ordered;

import java.util.ArrayList;
import java.util.function.Supplier;

public class SupplierChain<T extends Ordered> {
    private final ArrayList<Supplier<T>> suppliers;
    public SupplierChain(ArrayList<Supplier<T>> suppliers) {
        this.suppliers = suppliers;
    }

    public ArrayList<Supplier<T>> getSuppliers() {
        return this.suppliers;
    }

    public static <T extends Ordered> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T extends Ordered> {
        private ArrayList<Supplier<T>> suppliers = new ArrayList<>();

        Builder() {
        }

        public Builder<T> suppliers(ArrayList<Supplier<T>> suppliers) {
            this.suppliers = suppliers;
            return this;
        }

        public Builder<T> addSupplier(Supplier<T> supplier) {
            this.suppliers.add(supplier);
            return this;
        }

        public SupplierChain<T> build() {
            return new SupplierChain<>(suppliers);
        }

        public String toString() {
            return "SupplierChain.SupplierChainBuilder(suppliers=" + this.suppliers + ")";
        }
    }
}
