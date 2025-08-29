/*
 * Copyright (c) 2021, MegaEase
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

package easeagent.plugin.spring353.gateway;

import com.megaease.easeagent.plugin.CodeVersion;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public interface GatewayCons {
    String SPAN_KEY = GatewayCons.class.getName() + ".SPAN";
    String CHILD_SPAN_KEY = GatewayCons.class.getName() + ".CHILD_SPAN";
    String CLIENT_RECEIVE_CALLBACK_KEY = GatewayCons.class.getName() + ".CLIENT_RECEIVE_CALLBACK";

    CodeVersion VERSIONS = CodeVersion.builder()
        .key(ConfigConst.CodeVersion.KEY_SPRING_BOOT)
        .add(ConfigConst.CodeVersion.VERSION_SPRING_BOOT3).build();
}
