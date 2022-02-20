/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.bean;

import com.megaease.easeagent.plugin.Ordered;

public interface BeanProvider extends Ordered {
    enum BeanOrder {
        INIT(0, "init"),
        HIGH(20, "high"),
        METRIC_REGISTRY(200, "metric"),
        LOW(210, "low");

        private final int order;
        private final String name;

        BeanOrder(int s, String name) {
            this.order = s;
            this.name = name;
        }

        public int getOrder() {
            return this.order;
        }

        public String getName() {
            return this.name;
        }
    }
}
