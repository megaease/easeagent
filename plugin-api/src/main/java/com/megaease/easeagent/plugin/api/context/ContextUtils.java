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

package com.megaease.easeagent.plugin.api.context;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.utils.SystemClock;

public class ContextUtils {
    private static final String BEGIN_TIME = ContextUtils.class.getSimpleName() + ".beginTime";
    private static final String END_TIME = ContextUtils.class.getSimpleName() + ".endTime";

    public static void setBeginTime(Context context) {
        context.put(BEGIN_TIME, SystemClock.now());
    }

    public static Long getBeginTime(Context context) {
        return context.get(BEGIN_TIME);
    }

    public static Long getEndTime(Context context) {
        Long endTime = context.remove(END_TIME);
        if (endTime == null) {
            return SystemClock.now();
        }
        return endTime;
    }

    public static Long getDuration(Context context) {
        return getEndTime(context) - getBeginTime(context);
    }

    public static Long getDuration(Context context, Object startKey) {
        Long now = SystemClock.now();
        return now - (Long)context.remove(startKey);
    }

    /**
     * Get data from context
     *
     * @param context Store data
     * @param key     key is the type of data. Like {@code value.getClass()}
     * @param <T>     The type of data
     * @return data
     */
    public static <T> T getFromContext(Context context, Object key) {
        return context.get(key);
    }

    /**
     * Remove data from context
     * @param context data store
     * @param key key is the type of data.
     * @return
     * @param <T> the type of data
     */
    public static <T> T removeFromContext(Context context, Object key) {
        return context.remove(key);
    }
}
