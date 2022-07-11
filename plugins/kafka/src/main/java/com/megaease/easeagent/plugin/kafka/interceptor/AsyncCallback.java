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

package com.megaease.easeagent.plugin.kafka.interceptor;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.kafka.clients.producer.Callback;

public abstract class AsyncCallback implements Callback {
    protected final Callback delegate;
    private final boolean async;

    public AsyncCallback(Callback delegate) {
        this.delegate = delegate;
        this.async = isAsync(delegate);
    }

    public boolean isAsync() {
        return async;
    }

    public static Callback callback(MethodInfo methodInfo) {
        Object arg1 = methodInfo.getArgs()[1];
        if (arg1 == null) {
            return null;
        }
        return (Callback) arg1;
    }


    public static boolean isAsync(Callback callback) {
        if (callback == null) {
            return false;
        }
        if (callback instanceof AsyncCallback) {
            return ((AsyncCallback) callback).isAsync();
        }
        return true;
    }
}
