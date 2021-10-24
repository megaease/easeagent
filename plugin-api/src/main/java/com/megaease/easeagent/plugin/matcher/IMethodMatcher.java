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

package com.megaease.easeagent.plugin.matcher;

import com.megaease.easeagent.plugin.matcher.operator.AndMethodMatcher;
import com.megaease.easeagent.plugin.matcher.operator.NotMethodMatcher;
import com.megaease.easeagent.plugin.matcher.operator.Operator;
import com.megaease.easeagent.plugin.matcher.operator.OrMethodMatcher;

import java.util.HashSet;
import java.util.Set;

public interface IMethodMatcher extends Operator<IMethodMatcher>, Matcher {
    String DEFAULT_QUALIFIER = "default";

    default IMethodMatcher and(IMethodMatcher other) {
        return new AndMethodMatcher(this, other);
    }

    default IMethodMatcher or(IMethodMatcher other) {
        return new OrMethodMatcher(this, other);
    }

    default IMethodMatcher not() {
        return new NotMethodMatcher(this);
    }

    String getQualifier();

    default Set<IMethodMatcher> toSet() {
        Set<IMethodMatcher> set = new HashSet<>();
        set.add(this);
        return set;
    }
}
