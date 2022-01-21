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

package com.megaease.easeagent.plugin.mongodb.interceptor;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.mongodb.MongoClientSettings;

public class InterceptorHelper {
    public static final String CONFIG = InterceptorHelper.class.getName() + ".Config";
    public static final String METRIC = InterceptorHelper.class.getName() + ".Metric";
    public static final String EVENT_KEY = InterceptorHelper.class.getName() + "-Event";


    public static MongoClientSettings mongoClientSettings(MethodInfo methodInfo) {
        if (methodInfo.getArgs() != null) {
            for (Object arg : methodInfo.getArgs()) {
                if (arg instanceof MongoClientSettings) {
                    return (MongoClientSettings) arg;
                }
            }
        }
        return null;
    }
}
