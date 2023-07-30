package net.jonathangiles.httpclient.socket;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private Map<String, List<String>> headers;
    private Map<String, String> queryParams;
    private byte[] body;

    public HttpRequest(final HttpMethod method, final String path) {
        this.method = method;
        this.path = path;
    }

    public HttpRequest(final HttpMethod method, final String path, final String body) {
        this.method = method;
        this.path = path;
        setBody(body);
    }

    public HttpRequest(final HttpMethod method, final String path, final byte[] body) {
        this.method = method;
        this.path = path;
        setBody(body);
    }

//    public HttpRequest(final HttpMethod method, final String path, Map<String, String> headers, final String body) {
//        this(method, path, headers, null, body);
//    }

//    public HttpRequest(HttpMethod method, String path, Map<String, String> headers, Map<String, String> queryParams, String body) {
//        this.method = method;
//        this.path = path;
//        this.headers = headers;
//        this.queryParams = queryParams;
//        this.body = body;
//    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        } else {
            StringBuilder pathBuilder = new StringBuilder(path);
            pathBuilder.append("?");
            queryParams.forEach((key, value) -> {
                pathBuilder.append(key);
                pathBuilder.append("=");
                pathBuilder.append(value);
                pathBuilder.append("&");
            });
            pathBuilder.deleteCharAt(pathBuilder.length() - 1);
            return pathBuilder.toString();
        }
    }

    public Map<String, List<String>> getHeaders() {
        return headers == null ? Collections.emptyMap() : headers;
    }

    public HttpRequest addHeader(String name, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return this;
    }

    public Map<String, String> getQueryParams() {
        return queryParams == null ? Collections.emptyMap() : queryParams;
    }

    public HttpRequest addQueryParam(String name, String value) {
        if (queryParams == null) {
            queryParams = new HashMap<>();
        }
        queryParams.put(name, value);
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public HttpRequest setBody(final String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public HttpRequest setBody(final byte[] body) {
        this.body = body;
        return this;
    }

}