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

package easeagent.plugin.spring353.gateway.interceptor;

import com.megaease.easeagent.plugin.api.Context;

public class TimeUtils {
    public static long startTime(Context context, Object key) {
        Long start = context.get(key);
        if (start == null) {
            start = System.currentTimeMillis();
            context.put(key, start);
        }
        return start;
    }

    public static Long removeStartTime(Context context, Object key) {
        return context.remove(key);
    }
}
