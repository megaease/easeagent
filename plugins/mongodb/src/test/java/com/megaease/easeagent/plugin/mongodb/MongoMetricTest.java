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
import com.megaease.easeagent.plugin.mongodb.interceptor.MongoClientConstruct4MetricInterceptor;
import com.megaease.easeagent.plugin.mongodb.interceptor.listener.MongoMetricCommandListener;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandSucceededEvent;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MongoMetricTest extends MongoBaseTest {
    MongoMetricCommandListener listener;
    MongoClientConstruct4MetricInterceptor interceptor;

    @Before
    public void before() {
        super.before();
        interceptor = new MongoClientConstruct4MetricInterceptor();
        interceptor.init(config, "", "", "");
        listener = new MongoMetricCommandListener(this.config, interceptor.getMongoMetric());
    }

    @Test
    public void performSuccess() {
        BsonDocument bsonDocument = new BsonDocument();
        bsonDocument.put("collection", new BsonString(collection));
        CommandSucceededEvent succeededEvent = new CommandSucceededEvent(this.requestId, this.connectionDescription, this.cmdName, bsonDocument, 10);
        this.listener.commandStarted(startedEvent);
        this.listener.commandSucceeded(succeededEvent);
        this.assertMetric(interceptor.getMongoMetric().getNameFactory(), interceptor.getMongoMetric().getMetricRegistry(), true);
    }

    @Test
    public void performOpFail() {
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
        this.assertMetric(interceptor.getMongoMetric().getNameFactory(), interceptor.getMongoMetric().getMetricRegistry(), false);
    }

    @Test
    public void performOpFail2() {
        CommandFailedEvent failedEvent = new CommandFailedEvent(this.requestId, this.connectionDescription, this.cmdName, 10, new RuntimeException(this.errMsg));
        this.listener.commandStarted(startedEvent);
        this.listener.commandFailed(failedEvent);
        this.assertMetric(interceptor.getMongoMetric().getNameFactory(), interceptor.getMongoMetric().getMetricRegistry(), false);
    }
}
