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

package com.megaease.easeagent.plugin.jdbc;

import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;

import java.sql.Statement;

public abstract class MockJDBCStatement implements Statement, DynamicFieldAccessor {
    Object data;

    @Override
    public void setEaseAgent$$DynamicField$$Data(Object data) {
        this.data = data;
    }

    @Override
    public Object getEaseAgent$$DynamicField$$Data() {
        return this.data;
    }
}
