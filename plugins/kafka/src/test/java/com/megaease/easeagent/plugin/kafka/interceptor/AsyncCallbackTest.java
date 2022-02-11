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

package com.megaease.easeagent.plugin.kafka.interceptor;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;

import static org.junit.Assert.*;

public class AsyncCallbackTest {

    @Test
    public void isAsync() {
        AsyncCallback asyncCallback = new MockAsyncCallback((metadata, exception) -> {
        });
        assertTrue(asyncCallback.isAsync());
        asyncCallback = new MockAsyncCallback(null);
        assertFalse(asyncCallback.isAsync());
    }

    @Test
    public void callback() {
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{null, null}).build();
        assertNull(AsyncCallback.callback(methodInfo));
        Callback callback = (metadata, exception) -> {
        };
        methodInfo = MethodInfo.builder().args(new Object[]{null, callback}).build();
        assertSame(callback, AsyncCallback.callback(methodInfo));
    }

    @Test
    public void isAsync1() {
        assertFalse(AsyncCallback.isAsync(null));
        Callback callback = (metadata, exception) -> {
        };
        assertTrue(AsyncCallback.isAsync(callback));
        AsyncCallback asyncCallback = new MockAsyncCallback(callback);
        assertTrue(AsyncCallback.isAsync(asyncCallback));
        asyncCallback = new MockAsyncCallback(null);
        assertFalse(AsyncCallback.isAsync(asyncCallback));
    }

    class MockAsyncCallback extends AsyncCallback {

        public MockAsyncCallback(Callback delegate) {
            super(delegate);
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {

        }
    }
}
