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

package com.megaease.easeagent.plugin.httpservlet.utils;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import java.util.function.Consumer;

public class InternalAsyncListener implements AsyncListener {

    private final Consumer<AsyncEvent> consumer;

    public InternalAsyncListener(Consumer<AsyncEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onComplete(AsyncEvent event) {
        this.consumer.accept(event);
    }

    @Override
    public void onTimeout(AsyncEvent event) {
    }

    @Override
    public void onError(AsyncEvent event) {
    }

    @Override
    public void onStartAsync(AsyncEvent event) {
        AsyncContext eventAsyncContext = event.getAsyncContext();
        if (eventAsyncContext != null) {
            eventAsyncContext.addListener(this, event.getSuppliedRequest(), event.getSuppliedResponse());
        }
    }
}
