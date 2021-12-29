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

package com.megaease.easeagent.plugin.api.trace;

import java.io.Closeable;

/**
 * A span remains in the scope it was bound to until close is called.
 *
 * <p>This type can be extended so that the object graph can be built differently or overridden,
 * for example via zipkin or when mocking.
 * <p>
 * The Scope must be close after plugin:
 * <p>
 * example 1:
 * <pre>{@code
 *    void after(...){
 *       RequestContext pCtx = context.get(...)
 *       try{
 *          //do business
 *       }finally{
 *           pCtx.scope().close();
 *       }
 *    }
 * }</pre>
 * <p>
 * example 2:
 * <pre>{@code
 *    void after(...){
 *       RequestContext pCtx = context.get(...)
 *       try (Scope scope = pCtx.scope()) {
 *          //do business
 *       }
 *    }
 * }</pre>
 * <p>
 * example 3:
 * <pre>{@code
 *    void callback(AsyncContext ac){
 *       try (Scope scope = ac.importToCurrent()) {
 *          //do business
 *       }
 *    }
 * }</pre>
 */
public interface Scope extends Closeable {
    /**
     * No exceptions are thrown when unbinding a span scope.
     * It must be call after your business.
     * <pre>{@code
     *  try{
     *      ......
     *  }finally{
     *      scope.close();
     *  }
     * }</pre>
     */
    @Override
    void close();
}
