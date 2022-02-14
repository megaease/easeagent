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
 *
 */
package zipkin2.reporter.brave;

import brave.handler.SpanHandler;
import com.megaease.easeagent.plugin.report.zipkin.ReportSpan;
import zipkin2.reporter.Reporter;

public class ConvertZipkinSpanHandler extends ZipkinSpanHandler {
    public static final class Builder extends ZipkinSpanHandler.Builder {
        final Reporter<ReportSpan> spanReporter;

        Builder(Reporter<ReportSpan> spanReporter) {
            this.spanReporter = spanReporter;
        }

        @Override
        public Builder alwaysReportSpans(boolean alwaysReportSpans) {
            this.alwaysReportSpans = alwaysReportSpans;
            return this;
        }

        // SpanHandler not ZipkinSpanHandler as it can coerce to NOOP
        public SpanHandler build() {
            if (spanReporter == null) {
                return SpanHandler.NOOP;
            }
            return new ConvertZipkinSpanHandler(this);
        }

    }

    public static Builder builder(Reporter<ReportSpan> spanReporter) {
        if (spanReporter == null) {
            throw new NullPointerException("spanReporter == null");
        }
        return new Builder(spanReporter);
    }

    ConvertZipkinSpanHandler(Builder builder) {
        super(new ConvertSpanReporter(builder.spanReporter, builder.errorTag),
            builder.errorTag, builder.alwaysReportSpans);
    }
}
