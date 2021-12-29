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

package com.megaease.easeagent.context;

import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.trace.Scope;

public class AsyncScope implements Scope {
    private final InitializeContext context;
    private final Scope scope;
    private final boolean clearContext;

    public AsyncScope(InitializeContext context, Scope scope, boolean clearContext) {
        this.context = context;
        this.scope = scope;
        this.clearContext = clearContext;
    }

    @Override
    public void close() {
        this.scope.close();
        if (clearContext) {
            this.context.clear();
        }
    }
}
