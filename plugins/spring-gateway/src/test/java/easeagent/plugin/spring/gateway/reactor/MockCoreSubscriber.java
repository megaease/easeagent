/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easeagent.plugin.spring.gateway.reactor;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class MockCoreSubscriber implements CoreSubscriber<Void> {
    AtomicBoolean currentContext = new AtomicBoolean(false);
    AtomicBoolean onSubscribe = new AtomicBoolean(false);
    AtomicBoolean onNext = new AtomicBoolean(false);
    AtomicBoolean onError = new AtomicBoolean(false);
    AtomicBoolean onComplete = new AtomicBoolean(false);


    @Override
    public Context currentContext() {
        currentContext.set(true);
        return Context.of(Collections.emptyMap());
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        onSubscribe.set(true);
    }

    @Override
    public void onNext(Void aVoid) {
        onNext.set(true);
    }

    @Override
    public void onError(Throwable throwable) {
        onError.set(true);
    }

    @Override
    public void onComplete() {
        onComplete.set(true);
    }
}
