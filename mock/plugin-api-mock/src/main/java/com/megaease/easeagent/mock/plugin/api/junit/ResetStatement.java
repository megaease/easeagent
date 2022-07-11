/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.mock.plugin.api.junit;

import com.megaease.easeagent.mock.plugin.api.utils.ContextUtils;
import com.megaease.easeagent.mock.report.MockReport;
import org.junit.runners.model.Statement;

public class ResetStatement extends Statement {
    private final Statement after;

    public ResetStatement(Statement after) {
        this.after = after;
    }

    @Override
    public void evaluate() throws Throwable {
        cleanAll();
        after.evaluate();
    }

    private void cleanAll() {
        ContextUtils.resetAll();
        MockReport.cleanReporter();
    }

}
