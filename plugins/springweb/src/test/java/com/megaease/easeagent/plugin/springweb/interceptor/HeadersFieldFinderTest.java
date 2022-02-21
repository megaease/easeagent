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

package com.megaease.easeagent.plugin.springweb.interceptor;

import feign.Request;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;

public class HeadersFieldFinderTest {

    @Test
    public void getHashMapHeaders() {
        Request request = RequestUtils.buildFeignClient();
        HashMap<String, Collection<String>> hashMap = HeadersFieldFinder.getHashMapHeaders(request);
        assertTrue(hashMap.isEmpty());
        String key = "testKey";
        String value = "testValue";
        hashMap.put(key, Collections.singletonList(value));
        assertEquals(value, request.headers().get(key).iterator().next());

        hashMap = HeadersFieldFinder.getHashMapHeaders(request);
        assertEquals(1, hashMap.size());

        Request request2 = RequestUtils.buildFeignClient();
        HashMap<String, Collection<String>> hashMap2 = HeadersFieldFinder.getHashMapHeaders(request2);
        assertTrue(hashMap2.isEmpty());
        hashMap2.put(key, Collections.singletonList(value));
        assertEquals(value, request2.headers().get(key).iterator().next());

    }
}
