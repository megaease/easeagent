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

import com.megaease.easeagent.plugin.api.Context;

/**
 * Interface request type used for parsing and sampling.
 */
public interface Request extends Setter, Getter {
    /**
     * The remote {@link Span.Kind} describing the direction and type of the request.
     * {@code span.kind(request.kind())}
     */
    Span.Kind kind();

    /**
     * The header get of the request for Span and EaseAgent Context.
     * <pre>{@code
     *  String traceId = request.header("X-B3-TraceId");
     *  String spanId = request.header("X-B3-SpanId");
     *  String parentSpanId = request.header("X-B3-ParentSpanId");
     *  String rootSource = request.header("root-source");
     *  ......
     * }</pre>
     * <p>
     * It is usually called on the server side when collaboration between multiple processes is required.
     * {@code client --> <spanId,root-source...>server }
     * <p>
     * The class that implements this method needs to provide the name: value passed by the previous process,
     * It can be passed by using http or tcp.
     *
     * <pre>{@code
     *  class IRequest implements Request{
     *      HttpRequest httpRequest;
     *      String header(String name){
     *          return httpRequest.getHeaders(name);
     *      }
     *  }
     * }</pre>
     *
     * @see Context#importProgress(Request)
     */
    String header(String name);

    /**
     * The remote name describing the direction of the request.
     * {@code span.name(request.name())}
     */
    String name();

    /**
     * When true, cache the scope in span.
     * <pre>{@code
     *  span.cacheScope();
     * }</pre>
     *
     * @return boolean
     * @see {@link Span#cacheScope()}
     */
    boolean cacheScope();

    /**
     * The header set of the span and EaseAgent for request.
     * It is usually called on the client when collaboration between multiple processes is required.
     * {@code client<spanId,root-source...> --> server }
     * <p>
     * The class that implements this method needs to pass the name:value of the method to the next process,
     * It can be passed by using http or tcp.
     *
     * <p>
     * <pre>{@code
     *  request.setHeader("X-B3-TraceId", span.traceIdString());
     *  request.setHeader("X-B3-SpanId", span.spanIdString());
     *  request.setHeader("root-source", context.get("root-source"));
     *  ......
     * }</pre>
     * <p>
     * <pre>{@code
     *  class IRequest implements Request{
     *      HttpRequest httpRequest;
     *      void setHeader(String name, String value){
     *          httpRequest.setHeader(name, value);
     *      }
     *  }
     * }</pre>
     *
     * @see Context#nextProgress(Request)
     */
    void setHeader(String name, String value);
}
