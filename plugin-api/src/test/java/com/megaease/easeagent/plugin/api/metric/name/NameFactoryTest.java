/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.api.metric.name;

import com.megaease.easeagent.plugin.utils.ImmutableMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class NameFactoryTest {
    Map<Key, ? extends A> keyTMap = new HashMap<>();

    @Test
    public void createBuilder() {
        NameFactory nameFactory = NameFactory.createBuilder().counterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
            .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
            .build()).build();
        String key = nameFactory.counterName("test_key", MetricSubType.DEFAULT);
        System.out.println(key);

        Key key1 = new Key();
        B a = (B) keyTMap.get(key1);
    }


    class Key {

    }

    interface A {
    }

    class B implements A {

    }

}
