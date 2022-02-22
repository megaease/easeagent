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

package com.megaease.easeagent.plugin.mongodb;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.mongodb.interceptor.MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor;
import com.megaease.easeagent.plugin.mongodb.interceptor.MongoReactiveInitMetricInterceptor;
import com.megaease.easeagent.plugin.mongodb.interceptor.listener.MongoMetricCommandListener;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandSucceededEvent;
import com.mongodb.internal.async.SingleResultCallback;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MongoReactiveMetricTest extends MongoBaseTest {
    MongoMetricCommandListener listener;
    MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor interceptor;
    MongoReactiveInitMetricInterceptor initMetricInterceptor;

    @Before
    public void before() {
        super.before();
        interceptor = new MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor();
        initMetricInterceptor = new MongoReactiveInitMetricInterceptor();
        initMetricInterceptor.init(config, "", "", "");
        listener = new MongoMetricCommandListener(this.config, initMetricInterceptor.getMongoMetric());
    }

    @Test
    public void performSuccess() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{null, null, null, mock(SingleResultCallback.class)}).build();
        interceptor.before(methodInfo, context);

        BsonDocument bsonDocument = new BsonDocument();
        bsonDocument.put("collection", new BsonString(collection));
        CommandSucceededEvent succeededEvent = new CommandSucceededEvent(this.requestId, this.connectionDescription, this.cmdName, bsonDocument, 10);
        this.listener.commandStarted(startedEvent);
        this.listener.commandSucceeded(succeededEvent);
        MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor.SingleResultCallbackProxy<?> proxy =
            (MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor.SingleResultCallbackProxy<?>) methodInfo.getArgs()[3];
        proxy.onResult(null, null);
        this.assertMetric(initMetricInterceptor.getMongoMetric().getNameFactory(), initMetricInterceptor.getMongoMetric().getMetricRegistry(), true);
        context.exit(interceptor.getEnterKey(methodInfo, context));
    }

    @Test
    public void performOpFail() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{null, null, null, mock(SingleResultCallback.class)}).build();
        interceptor.before(methodInfo, context);

        BsonDocument errDoc = new BsonDocument();
        errDoc.put("errmsg", new BsonString(errMsg));
        List<BsonValue> list = new ArrayList<>();
        list.add(errDoc);
        BsonArray bsonValues = new BsonArray(list);
        BsonDocument bsonDocument = new BsonDocument();
        bsonDocument.put("collection", new BsonString(collection));
        bsonDocument.put("writeErrors", bsonValues);
        CommandSucceededEvent succeededEvent = new CommandSucceededEvent(this.requestId, this.connectionDescription, this.cmdName, bsonDocument, 10);
        this.listener.commandStarted(startedEvent);
        this.listener.commandSucceeded(succeededEvent);
        MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor.SingleResultCallbackProxy<?> proxy =
            (MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor.SingleResultCallbackProxy<?>) methodInfo.getArgs()[3];
        proxy.onResult(null, null);
        this.assertMetric(initMetricInterceptor.getMongoMetric().getNameFactory(), initMetricInterceptor.getMongoMetric().getMetricRegistry(), false);
        context.exit(interceptor.getEnterKey(methodInfo, context));
    }

    @Test
    public void performOpFail2() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{null, null, null, mock(SingleResultCallback.class)}).build();
        interceptor.before(methodInfo, context);

        CommandFailedEvent failedEvent = new CommandFailedEvent(this.requestId, this.connectionDescription, this.cmdName, 10, new RuntimeException(this.errMsg));
        this.listener.commandStarted(startedEvent);
        this.listener.commandFailed(failedEvent);
        MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor.SingleResultCallbackProxy<?> proxy =
            (MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor.SingleResultCallbackProxy<?>) methodInfo.getArgs()[3];
        proxy.onResult(null, null);
        this.assertMetric(initMetricInterceptor.getMongoMetric().getNameFactory(), initMetricInterceptor.getMongoMetric().getMetricRegistry(), false);
        context.exit(interceptor.getEnterKey(methodInfo, context));
    }
}
