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

package io.opentelemetry.sdk.resources;

import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.otlp.common.AgentAttributes;
import com.megaease.easeagent.plugin.api.otlp.common.SemanticKey;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import io.opentelemetry.api.common.Attributes;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAMESPACE;

import javax.annotation.Nullable;
import java.util.List;

public class EaseAgentResource extends Resource implements ConfigChangeListener {
    static volatile EaseAgentResource agentResource = null;

    private EaseAgentResource() {
        super();
        this.system = EaseAgent.getConfig("system", "demo-system");
        this.service = EaseAgent.getConfig("name", "demo-service");
        EaseAgent.getConfig().addChangeListener(this);

        this.resource = Resource.getDefault()
            .merge(Resource.create(
                AgentAttributes.builder()
                    .put(SERVICE_NAME, this.service)
                    .put(SERVICE_NAMESPACE, this.system)
                    .build()));
    }

    private final Resource resource;
    private String service;
    private String system;

    public String getService() {
        return this.service;
    }

    public String getSystem() {
        return this.system;
    }

    public static EaseAgentResource getResource() {
        if (agentResource == null) {
            synchronized (EaseAgentResource.class) {
                if (agentResource == null) {
                    agentResource = new EaseAgentResource();
                }
            }
        }

        return agentResource;
    }

    @Nullable
    @Override
    public String getSchemaUrl() {
        return SemanticKey.SCHEMA_URL;
    }

    @Override
    public Attributes getAttributes() {
        return this.resource.getAttributes();
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        list.forEach(change -> {
            if (change.getFullName().equals("name")) {
                this.service = change.getNewValue();
            } else if (change.getFullName().equals("system")) {
                this.system = change.getNewValue();
            }

            this.resource.merge(Resource.create(
                Attributes.builder()
                    .put(SERVICE_NAME, this.service)
                    .put(SERVICE_NAMESPACE, this.system)
                    .build()));
        });
    }
}
