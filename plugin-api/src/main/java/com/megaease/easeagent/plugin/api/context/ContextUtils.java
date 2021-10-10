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

import java.util.HashMap;
import java.util.Map;

public class ContextUtils {
    private static final String BEGIN_TIME = ContextUtils.class.getName() + ".beginTime";
    private static final String END_TIME = ContextUtils.class.getName() + ".endTime";

    private static void setBeginTime(Map<Object, Object> context) {
        context.put(BEGIN_TIME, System.currentTimeMillis());
    }

    public static Long getBeginTime(Map<Object, Object> context) {
        return (Long) context.get(BEGIN_TIME);
    }

    public static void setEndTime(Map<Object, Object> context) {
        context.put(END_TIME, System.currentTimeMillis());
    }

    public static Long getEndTime(Map<Object, Object> context) {
        Long endTime = (Long) context.get(END_TIME);
        if (endTime == null) {
            setEndTime(context);
            endTime = (Long) context.get(END_TIME);
        }
        return endTime;
    }

    public static long getDuration(Map<Object, Object> context) {
        return getEndTime(context) - getBeginTime(context);
    }

    public static Map<Object, Object> createContext() {
        HashMap<Object, Object> map = new HashMap<>();
        setBeginTime(map);
        return map;
    }

    /**
     * Get data from context
     *
     * @param context Store data
     * @param key     key is the type of data. Like {@code value.getClass()}
     * @param <T>     The type of data
     * @return data
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFromContext(Map<Object, Object> context, Object key) {
        return (T) context.get(key);
    }

}
