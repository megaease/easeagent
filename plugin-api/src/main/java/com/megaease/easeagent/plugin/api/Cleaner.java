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

package com.megaease.easeagent.plugin.api;

import java.io.Closeable;

/**
 * A Cleaner for Context
 * It must be call after your business.
 * <p>
 * example 1:
 * <pre>${@code
 *    Cleaner cleaner = context.importAsync(snapshot);
 *    try{
 *       //do business
 *    }finally{
 *        cleaner.close();
 *    }
 * }</pre>
 * <p>
 * example 2:
 * <pre>${@code
 *    void before(...){
 *       Cleaner cleaner = context.importForwardedHeaders(getter);
 *    }
 *    void after(...){
 *      try{
 *         //do business
 *      }finally{
 *          cleaner.close();
 *      }
 *    }
 * }</pre>
 * <p>
 * example 3:
 * <pre>${@code
 *    void callback(AsyncContext ac){
 *       try (Cleaner cleaner = ac.importToCurrent()) {
 *          //do business
 *       }
 *    }
 * }</pre>
 */
public interface Cleaner extends Closeable {
    /**
     * No exceptions are thrown when unbinding a Context.
     * It must be call after your business.
     * <pre>{@code
     *  try{
     *      ......
     *  }finally{
     *      cleaner.close();
     *  }
     * }</pre>
     */
    @Override
    void close();
}
