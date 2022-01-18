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

package com.megaease.easeagent.plugin.mongodb.interceptor;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.mongodb.MongoSocketException;
import com.mongodb.connection.ConnectionDescription;
import com.mongodb.connection.ConnectionId;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * copy from https://github.com/openzipkin/brave/blob/master/instrumentation/mongodb/src/main/java/brave/mongodb/TraceMongoCommandListener.java
 */
public class TraceMongoDBCommandListener implements CommandListener {

    private static final String SPAN_KEY = TraceMongoDBCommandListener.class.getName() + "-Span";

    // See https://docs.mongodb.com/manual/reference/command for the command reference
    static final Set<String> COMMANDS_WITH_COLLECTION_NAME = new LinkedHashSet<>(Arrays.asList(
        "aggregate", "count", "distinct", "mapReduce", "geoSearch", "delete", "find", "findAndModify",
        "insert", "update", "collMod", "compact", "convertToCapped", "create", "createIndexes", "drop",
        "dropIndexes", "killCursors", "listIndexes", "reIndex"));

    private final AutoRefreshPluginConfigImpl config;

    public TraceMongoDBCommandListener(AutoRefreshPluginConfigImpl config) {
        this.config = config;
    }

    @Override
    public void commandStarted(CommandStartedEvent event) {
        if (!this.config.getConfig().enabled()) {
            return;
        }
        Context context = EaseAgent.getContext();
        Span span = context.nextSpan();
        context.put(SPAN_KEY, span);

        String databaseName = event.getDatabaseName();
        if ("admin".equals(databaseName)) return; // don't trace commands like "endSessions"

        if (span == null || span.isNoop()) return;

        String commandName = event.getCommandName();
        BsonDocument command = event.getCommand();
        String collectionName = getCollectionName(command, commandName);

        span.name(getSpanName(commandName, collectionName))
            .kind(Span.Kind.CLIENT)
            .remoteServiceName("mongodb-" + databaseName)
            .tag("mongodb.command", commandName);

        if (collectionName != null) {
            span.tag("mongodb.collection", collectionName);
        }

        ConnectionDescription connectionDescription = event.getConnectionDescription();
        if (connectionDescription != null) {
            ConnectionId connectionId = connectionDescription.getConnectionId();
            if (connectionId != null) {
                span.tag("mongodb.cluster_id", connectionId.getServerId().getClusterId().getValue());
            }

            try {
                InetSocketAddress socketAddress =
                    connectionDescription.getServerAddress().getSocketAddress();
                span.remoteIpAndPort(socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
            } catch (MongoSocketException ignored) {

            }
        }

        span.start();
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
        Context context = EaseAgent.getContext();
        Span span = context.get(SPAN_KEY);
        if (span == null) {
            return;
        }
        span.finish();
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
        Context context = EaseAgent.getContext();
        Span span = context.get(SPAN_KEY);
        if (span == null) {
            return;
        }
        span.error(event.getThrowable());
        span.finish();
    }


    String getCollectionName(BsonDocument command, String commandName) {
        if (COMMANDS_WITH_COLLECTION_NAME.contains(commandName)) {
            String collectionName = getNonEmptyBsonString(command.get(commandName));
            if (collectionName != null) {
                return collectionName;
            }
        }
        // Some other commands, like getMore, have a field like {"collection": collectionName}.
        return getNonEmptyBsonString(command.get("collection"));
    }

    /**
     * @return trimmed string from {@code bsonValue} or null if the trimmed string was empty or the
     * value wasn't a string
     */
    static String getNonEmptyBsonString(BsonValue bsonValue) {
        if (bsonValue == null || !bsonValue.isString()) return null;
        String stringValue = bsonValue.asString().getValue().trim();
        return stringValue.isEmpty() ? null : stringValue;
    }

    static String getSpanName(String commandName, String collectionName) {
        if (collectionName == null) return commandName;
        return commandName + " " + collectionName;
    }
}
