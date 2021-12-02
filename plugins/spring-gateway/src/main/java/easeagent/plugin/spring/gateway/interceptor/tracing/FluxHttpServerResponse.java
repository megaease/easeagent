package easeagent.plugin.spring.gateway.interceptor.tracing;

import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

public class FluxHttpServerResponse implements HttpResponse {
    private final FluxHttpServerRequest request;
    private final ServerHttpResponse response;
    private final String route;
    private final Throwable error;

    public FluxHttpServerResponse(FluxHttpServerRequest request,
                                  ServerHttpResponse response, String route, Throwable error) {
        this.request = request;
        this.response = response;
        this.route = route;
        this.error = error;
    }

    public FluxHttpServerResponse(ServerWebExchange exchange, Throwable error) {
        this.request = new FluxHttpServerRequest(exchange.getRequest());
        this.response = exchange.getResponse();

        PathPattern bestPattern = exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String route = null;
        if (bestPattern != null) {
            route = bestPattern.getPatternString();
        }

        this.route = route;
        this.error = error;
    }

    @Override
    public String header(String name) {
        if (this.response == null) {
            return null;
        }
        HttpHeaders hs = this.response.getHeaders();
        return hs.getFirst(name);
    }

    @Override
    public String method() {
        return this.request.method();
    }

    @Override
    public String route() {
        return this.route;
    }

    @Override
    public int statusCode() {
        if (this.response != null && this.response.getStatusCode() != null) {
            return this.response.getStatusCode().value();
        } else {
            return 0;
        }
    }

    @Override
    public Throwable maybeError() {
        return this.error;
    }
}
