package com.megaease.easeagent.plugin.api.trace.utils;

import com.megaease.easeagent.plugin.api.trace.Span;

import static com.megaease.easeagent.plugin.api.trace.utils.TraceConst.HTTP_HEADER_X_FORWARDED_FOR;

public class HttpUtils {
    public static void handleReceive(Span span, HttpRequest httpRequest) {
        span.name(httpRequest.name());
        span.tag(TraceConst.HTTP_TAG_ROUTE, httpRequest.route());
        span.tag(TraceConst.HTTP_TAG_METHOD, httpRequest.method());
        span.tag(TraceConst.HTTP_TAG_PATH, httpRequest.path());
        if (!parseHttpClientIpFromXForwardedFor(span, httpRequest)) {
            span.remoteIpAndPort(httpRequest.getRemoteAddr(), httpRequest.getRemotePort());
        }
        span.start();
    }

    private static boolean parseHttpClientIpFromXForwardedFor(Span span, HttpRequest httpRequest) {
        String forwardedFor = httpRequest.header(HTTP_HEADER_X_FORWARDED_FOR);
        if (forwardedFor == null) return false;
        int indexOfComma = forwardedFor.indexOf(',');
        if (indexOfComma != -1) forwardedFor = forwardedFor.substring(0, indexOfComma);
        return span.remoteIpAndPort(forwardedFor, 0);
    }

    public static void finish(Span span, HttpResponse httpResponse) {
        Throwable error = httpResponse.maybeError();
        if (error != null) {
            span.error(error); // Ensures MutableSpan.error() for SpanHandler
        }
        int statusCode = httpResponse.statusCode();
        if (statusCode != 0) {
            String nameFromRoute = spanNameFromRoute(httpResponse, statusCode);
            if (nameFromRoute != null) span.name(nameFromRoute);
            if (statusCode < 200 || statusCode > 299) { // not success code
                span.tag(TraceConst.HTTP_TAG_STATUS_CODE, String.valueOf(statusCode));
            }
        }
        if (error == null && (statusCode < 100 || statusCode > 399)) {
            span.tag(TraceConst.HTTP_TAG_ERROR, String.valueOf(statusCode));
        }
        span.finish();
    }

    static String spanNameFromRoute(HttpResponse httpRequest, int statusCode) {
        String method = httpRequest.method();
        if (method == null) return null; // don't undo a valid name elsewhere
        String route = httpRequest.route();
        if (route == null) return null; // don't undo a valid name elsewhere
        if (!"".equals(route)) return method + " " + route;
        return catchAllName(method, statusCode);
    }

    static String catchAllName(String method, int statusCode) {
        switch (statusCode) {
            // from https://tools.ietf.org/html/rfc7231#section-6.4
            case 301:
            case 302:
            case 303:
            case 305:
            case 306:
            case 307:
                return method + " redirected";
            case 404:
                return method + " not_found";
            default:
                return null;
        }
    }
}
