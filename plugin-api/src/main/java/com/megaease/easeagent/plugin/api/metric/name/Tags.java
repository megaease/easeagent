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

package com.megaease.easeagent.plugin.api.metric.name;

import com.megaease.easeagent.plugin.api.ProgressFields;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A tags describing the metric.
 * It has three predefined tags: category, type, {@code keyFieldName}
 * Its tag is copied as follows:
 * <pre>{@code
 *  output.put("category", tags.category)
 *  output.put("type", tags.type)
 *  output.put(tags.keyFieldName, {@link NameFactory}.key[?])
 *  tags.tags.forEach((k,v)->{
 *      output.put(k,v)
 *  })
 * }</pre>
 */
public class Tags {
    public static final String CATEGORY = "category";
    public static final String TYPE = "type";
    private final String category;
    private final String type;
    private final String keyFieldName;
    private final Map<String, String> tags;

    /**
     * @param category     {@link #getCategory()}
     * @param type         {@link #getType()}
     * @param keyFieldName {@link #getKeyFieldName()}
     */
    public Tags(@Nonnull String category, @Nonnull String type, @Nonnull String keyFieldName) {
        this.category = category;
        this.type = type;
        this.keyFieldName = keyFieldName;
        this.tags = new HashMap<>(ProgressFields.getServiceTags());
    }

    /**
     * put tag for.
     *
     * @param key   tag key
     * @param value tag value
     * @return this methods return {@linkplain Tags} for chaining, but the instance is always the same.
     */
    public Tags put(String key, String value) {
        this.tags.put(key, value);
        return this;
    }

    /**
     * tag category describing of the metrics.
     * for example: "application"
     *
     * @return
     */
    public String getCategory() {
        return category;
    }

    /**
     * tag type describing of the metrics.
     * for example: "http-request"
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * tag {@link NameFactory} keys describing of the metrics.
     * for example: "url"
     *
     * <pre>{@code
     *  keyFieldName="url"
     *  nameFactory.timerName("http://127.0.0.1:8080/", ...);
     *  // it will be tag.put("url", "http://127.0.0.1:8080/")
     * }</pre>
     *
     * @return
     * @see NameFactory
     */
    public String getKeyFieldName() {
        return keyFieldName;
    }

    /**
     * Custom tags describing of the metrics.
     *
     * @return custom tags
     */
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tags tags1 = (Tags) o;
        return Objects.equals(category, tags1.category) &&
            Objects.equals(type, tags1.type) &&
            Objects.equals(keyFieldName, tags1.keyFieldName) &&
            Objects.equals(tags, tags1.tags);
    }

    @Override
    public int hashCode() {

        return Objects.hash(category, type, keyFieldName, tags);
    }
}
