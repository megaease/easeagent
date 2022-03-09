package com.megaease.easeagent.report.async;

/*
 * Copyright 2016-2019 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 *  borrow from zipkin2.reporter.ReporterMetrics
 */
@SuppressWarnings("unused")
public interface AsyncReporterMetrics {

    /**
     * Increments count of message attempts, which contain 1 or more items(spans/logs). Ex POST requests or Kafka
     * messages sent.
     */
    void incrementMessages();

    /**
     * Increments count of messages that could not be sent. Ex host unavailable, or peer disconnect.
     */
    void incrementMessagesDropped(Throwable cause);

    /**
     * Increments the count of items(spans/logs) reported. When {@link AsyncReporter} is used, reported items will
     * usually be a larger number than messages.
     */
    void incrementItems(int quantity);

    /**
     * Increments the number of encoded item(span/log) bytes reported.
     */
    void incrementSpanBytes(int quantity);

    /**
     * Increments the number of bytes containing encoded spans in a message.
     *
     * <p>This is a function of item(span/access-log) bytes per message and overhead
     */
    void incrementMessageBytes(int quantity);

    /**
     * Increments the count of spans dropped for any reason. For example, failure queueing or
     * sending.
     */
    void incrementItemsDropped(int quantity);

    /** Updates the count of items(spans/logs) pending, following a flush activity. */
    void updateQueuedItems(int update);

    /** Updates the count of encoded items(spans/logs) bytes pending, following a flush activity. */
    void updateQueuedBytes(int update);

    AsyncReporterMetrics NOOP_METRICS = new AsyncReporterMetrics() {
        @Override public void incrementMessages() {
            // noop
        }

        @Override public void incrementMessagesDropped(Throwable cause) {
            // noop
        }

        @Override public void incrementItems(int quantity) {
            // noop
        }

        @Override public void incrementSpanBytes(int quantity) {
            // noop
        }

        @Override public void incrementMessageBytes(int quantity) {
            // noop
        }

        @Override public void incrementItemsDropped(int quantity) {
            // noop
        }

        @Override public void updateQueuedItems(int update) {
            // noop
        }

        @Override public void updateQueuedBytes(int update) {
            // noop
        }

        @Override public String toString() {
            return "NoOpReporterMetrics";
        }
    };
}

