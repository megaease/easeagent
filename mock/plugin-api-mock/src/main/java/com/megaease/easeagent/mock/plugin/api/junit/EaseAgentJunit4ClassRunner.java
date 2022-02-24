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

import com.megaease.easeagent.context.ContextManager;
import com.megaease.easeagent.mock.context.MockContextManager;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public final class EaseAgentJunit4ClassRunner extends BlockJUnit4ClassRunner {
    public static final ContextManager ignored = MockContextManager.getContextManagerMock();

    public EaseAgentJunit4ClassRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        return new ResetStatement(super.withBefores(method, target, statement));
    }
}
