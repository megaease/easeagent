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

package com.megaease.easeagent.log4j2.api;

public class ILevel extends java.util.logging.Level {
    public static final int OFF_VALUE = 1;
    public static final int FATAL_VALUE = 2;
    public static final int ERROR_VALUE = 3;
    public static final int WARN_VALUE = 4;
    public static final int INFO_VALUE = 5;
    public static final int DEBUG_VALUE = 6;
    public static final int TRACE_VALUE = 7;
    public static final int ALL_VALUE = 8;


    public static final ILevel OFF = new ILevel("OFF", OFF_VALUE);
    public static final ILevel FATAL = new ILevel("FATAL", FATAL_VALUE);
    public static final ILevel ERROR = new ILevel("ERROR", ERROR_VALUE);
    public static final ILevel WARN = new ILevel("WARN", WARN_VALUE);
    public static final ILevel INFO = new ILevel("INFO", INFO_VALUE);
    public static final ILevel DEBUG = new ILevel("DEBUG", DEBUG_VALUE);
    public static final ILevel TRACE = new ILevel("TRACE", TRACE_VALUE);
    public static final ILevel ALL = new ILevel("ALL", ALL_VALUE);


    protected ILevel(String name, int value) {
        super(name, value);
    }
}
