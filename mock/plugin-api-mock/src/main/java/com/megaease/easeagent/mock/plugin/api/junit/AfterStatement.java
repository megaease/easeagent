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

package com.megaease.easeagent.mock.plugin.api.junit;

import com.megaease.easeagent.mock.zipkin.MockTracingProvider;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class AfterStatement extends Statement {
    private final FrameworkMethod method;
    private final Object target;
    private final Statement previous;

    public AfterStatement(FrameworkMethod method, Object target, Statement previous) {
        this.method = method;
        this.target = target;
        this.previous = previous;
    }

    @Override
    public void evaluate() throws Throwable {
        this.previous.evaluate();
        checkSpan();
    }

    private void checkSpan() throws ScopeMustBeCloseException {
        if (MockTracingProvider.hashCurrentConetxt()) {
            throw new ScopeMustBeCloseException(String.format("The Scope must be close after plugin. Also make sure the scop is closed if you don't test after. \n\tat %s.%s(%s.java)",
                target.getClass().getName(),
                method.getName(),
                target.getClass().getSimpleName()
            ));
        }
        if (MockTracingProvider.hasPendingSpans()) {
            MockTracingProvider.cleanPendingSpans();

            throw new RuntimeException(String.format("The Span must be finish or abandon. \n\tat %s.%s(%s.java)",
                target.getClass().getName(),
                method.getName(),
                target.getClass().getSimpleName()
            ));
        }

    }
}
