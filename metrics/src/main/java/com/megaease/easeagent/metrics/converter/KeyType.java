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

package com.megaease.easeagent.metrics.converter;

/**
 * KeyType indicated how to fetch key value from which type metric of MetricRegistry, first we need to recognize what
 * is key of data?</p>
 * The key is a value which attached many metrics' value with it, for example:</p>
 * In <b>http-request</b>, we think the url is key, other metrics' value describe a special url properties, in
 * <b>jvm-memory</b> resource is key.
 */
@SuppressWarnings("all")
public enum KeyType {
    Timer,
    Gauge,
    Counter,
    Histogram,
    Meter;
}
