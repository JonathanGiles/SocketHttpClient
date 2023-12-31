package net.jonathangiles.httpclient.socket;

import java.util.Collections;
import java.util.Map;

public class HttpResponse {
    private final int statusCode;
    private final Map<String, String> headers;
    private final String body;

    public HttpResponse(int statusCode, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getBody() {
        return body;
    }
}