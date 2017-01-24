package com.hexdecteam.log4j2.appender;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Plugin(name = "Http", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class PostAppender extends AbstractAppender {

    private final URI                 uri;
    private final String              contentType;
    private final boolean             compress;
    private final CloseableHttpClient client;

    private PostAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignore, URI uri,
                         String contentType, boolean compress, CloseableHttpClient client) {
        super(name, filter, layout, ignore);
        this.uri = uri;
        this.contentType = contentType;
        this.compress = compress;
        this.client = client;
    }

    public void append(LogEvent event) {
        final HttpPost post = new HttpPost(uri);

        final HttpEntity entity = entity(event, contentType);

        post.setEntity(compress ? new GzipCompressingEntity(entity) : entity);

        try {
            check(client.execute(post));
        } catch (IOException e) {
            throw new AppenderLoggingException(e);
        }
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        try { client.close(); } catch (IOException ignore) { }
        return super.stop(timeout, timeUnit);
    }

    private static HttpEntity entity(LogEvent event, String contentType) {
        final String content = event.getMessage().getFormattedMessage();
        return new StringEntity(content, ContentType.create(contentType));
    }

    private static void check(CloseableHttpResponse response) {
        try {
            final StatusLine line = response.getStatusLine();
            if (line.getStatusCode() >= 400) {
                throw new AppenderLoggingException("HTTP response: " + line);
            }
        } finally {
            try { response.close(); } catch (IOException ignore) { }
        }
    }

    @PluginFactory
    public static PostAppender createHTTPAppender(
            @Required @PluginAttribute("name") final String name,
            @PluginAttribute(value = "ignoreExceptions") final boolean ignore,
            @PluginElement("Filter") final Filter filter,
            @Required @PluginAttribute("uri") final URI uri,
            @Required @PluginAttribute("contentType") final String contentType,
            @Required @PluginAttribute("userAgent") final String userAgent,
            @PluginAttribute("compress") final boolean compress,
            @PluginElement("Headers") final Header[] headers

    ) throws Exception {

        return new PostAppender(name, filter, null, ignore, uri, contentType, compress,
                                HttpClients.custom()
                                           .setUserAgent(userAgent)
                                           .setDefaultHeaders(asHttpHeaders(headers))
                                           .build());
    }

    private static List<org.apache.http.Header> asHttpHeaders(final Header[] headers) {
        return new AbstractList<org.apache.http.Header>() {
            public int size() {
                return headers == null ? 0 : headers.length;
            }

            public org.apache.http.Header get(int index) {
                final Header header = headers[index];
                return new BasicHeader(header.name, header.value);
            }
        };
    }

    @Plugin(name = "header", category = "Core", printObject = true)
    public static class Header {
        private final String name;
        private final String value;

        private Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @PluginFactory
        public static Header createHeader(
                @Required @PluginAttribute("name") String name,
                @Required @PluginValue("value") String value
        ) {
            return new Header(name, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Header header = (Header) o;

            return value != null ? value.equals(header.value) : header.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Header{value='" + value + '\'' + '}';
        }
    }

}
