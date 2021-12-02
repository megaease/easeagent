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

package com.megaease.easeagent.plugin.api.context;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.Map;

/**
 * An asynchronous thread snapshot context
 * code example:
 * <pre>{@code
 *  AsyncContext asyncContext = context.exportAsync();
 *  class Run implements Runnable{
 *      void run(){
 *          try (Scope scope = asyncContext.importToCurr()) {
 *               //do something
 *               //or asyncContext.getTracer().nextSpan();
 *          }
 *      }
 *  }
 *  }</pre>
 */
public interface AsyncContext {
    /**
     * When true, do nothing and nothing is reported . However, this AsyncContext should
     * still be injected into outgoing requests. Use this flag to avoid performing expensive
     * computation.
     */
    boolean isNoop();


    /**
     * @return {@link Tracing}
     */
    Tracing getTracer();

    /**
     * @return current {@link Context} for session
     */
    Context getContext();

    /**
     * Import this AsyncContext to current {@link Context} and return a {@link Scope}
     *
     * @return {@link Scope}
     */
    Scope importToCurr();

    /**
     * @return all async snapshot context key:value
     */
    Map<Object, Object> getAll();


    /**
     * put all key:value to context
     *
     * @param context key:value
     */
    void putAll(Map<Object, Object> context);

}
