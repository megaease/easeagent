/*
 * Copyright (c) 2021 MegaEase
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

package com.megaease.easeagent.plugin.jdbc.common;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

public class SQLCompressionWrapper implements SQLCompression {

    public static final SQLCompressionWrapper INSTANCE = new SQLCompressionWrapper();

    private static final String SQL_COMPRESS_ENABLED = "plugin.observability.jdbc.sql.compress.enabled";

    @Override
    public String compress(String origin) {
        Config config = EaseAgent.getConfig();
        Boolean enabled = config.getBoolean(SQL_COMPRESS_ENABLED);
        if (enabled) {
            return MD5SQLCompression.getInstance().compress(origin);
        }
        return SQLCompression.DEFAULT.compress(origin);
    }
}
