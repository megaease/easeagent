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

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.SpanContext;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.Map;

/**
 * An asynchronous thread snapshot context
 * code example:
 * <pre>{@code
 *  AsyncContext asyncContext = context.exportAsync();
 *  class Run implements Runnable{
 *      void run(){
 *          try (Scope scope = asyncContext.importToCurrent()) {
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
     * get Span Context for Tracing
     *
     * @return SpanContext
     */
    SpanContext getSpanContext();

    /**
     * @return current {@link Context} for session
     */
    Context getContext();

    /**
     * Import this AsyncContext to current {@link Context} and return a {@link com.megaease.easeagent.plugin.api.Cleaner}
     * <p>
     * The Cleaner must be close after business:
     * <p>
     * example:
     * <pre>{@code
     *    void callback(AsyncContext ac){
     *       try (Cleaner cleaner = ac.importAsync()) {
     *          //do business
     *       }
     *    }
     * }</pre>
     *
     * @return {@link com.megaease.easeagent.plugin.api.Cleaner}
     */
    Cleaner importToCurrent();

    /**
     * @return all async snapshot context key:value
     */
    Map<Object, Object> getAll();

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this context contains no mapping for the key.
     *
     * <p>More formally, if this context contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>If this context permits null values, then a return value of
     * {@code null} does not <i>necessarily</i> indicate that the context
     * contains no mapping for the key; it's also possible that the context
     * explicitly maps the key to {@code null}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     * {@code null} if this context contains no mapping for the key
     * @throws ClassCastException if the key is of an inappropriate type for
     *                            this context
     *                            (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    <T> T get(Object key);

    /**
     * Associates the specified value with the specified key in this context
     * (optional operation).  If the context previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A context
     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt>
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the context
     * previously associated <tt>null</tt> with <tt>key</tt>,
     * if the implementation supports <tt>null</tt> values.)
     * @throws ClassCastException if the class of the specified key or value
     *                            prevents it from being stored in this context
     */
    <V> V put(Object key, V value);

}
