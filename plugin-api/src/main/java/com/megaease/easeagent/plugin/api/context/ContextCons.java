/*
 * Copyright (c) 2021, MegaEase
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

public interface ContextCons {

    String CACHE_CMD = ContextCons.class.getName() + ".cache_cmd";
    String CACHE_URI = ContextCons.class.getName() + ".cache_uri";
    String MQ_URI = ContextCons.class.getName() + ".mq_uri";
    String ASYNC_FLAG = ContextCons.class.getName() + ".async";
    String SPAN = ContextCons.class.getName() + ".Span";
    String PROCESSED_BEFORE = ContextCons.class.getName() + ".Processed-Before";
    String PROCESSED_AFTER = ContextCons.class.getName() + ".Processed-After";
}
