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

/**
 * Used to send the trace context downstream. For example, as http headers.
 *
 * <p>For example, to put the context on an {@link java.net.HttpURLConnection}, you can do this:
 * <pre>{@code
 * // in your constructor
 * injector = tracing.messagingTracing().injector();
 *
 * // later in your code, reuse the function you created above to add trace headers
 * HttpURLConnection connection = (HttpURLConnection) new URL("http://myserver").openConnection();
 * injector.inject(span, new MessagingRequest(){ public void setHeader(k,v){connection.setRequestProperty(k,v);}} );
 * }</pre>
 */
public interface Injector<R extends MessagingRequest> {
    /**
     * Usually calls a {@link Request#setHeader(String, String)} for each propagation field to send downstream.
     *
     * @param span    possibly unsampled.
     * @param request holds propagation fields. For example, an outgoing message or http request.
     */
    void inject(Span span, R request);
}
