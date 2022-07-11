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
package com.megaease.easeagent.plugin.report.tracing;

/**
 * form zipkin2.Annotation
 */
public class Annotation implements Comparable<Annotation> {
    long timestamp;
    String value;

    public Annotation(long timestamp, String v) {
        this.timestamp = timestamp;
        this.value = v;
    }

    public long timestamp() {
        return timestamp;
    }

    public String value() {
        return value;
    }

    @Override
    public int compareTo(Annotation that) {
        if (this == that) return 0;
        int byTimestamp = Long.compare(timestamp(), that.timestamp());
        if (byTimestamp != 0) {
            return byTimestamp;
        }
        return value().compareTo(that.value());
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= (int) ((timestamp >>> 32) ^ timestamp);
        h *= 1000003;
        h ^= value.hashCode();
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Annotation)) {
            return false;
        }
        Annotation that = (Annotation) o;
        return timestamp == that.timestamp() && value.equals(that.value());
    }
}
