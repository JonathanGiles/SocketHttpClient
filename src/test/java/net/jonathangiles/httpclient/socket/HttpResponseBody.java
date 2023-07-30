package net.jonathangiles.httpclient.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpResponseBody {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String responseBody = null;

    private String requestBody = null;
    private Map<String, List<String>> requestHeaders;
    private Map<String, String> requestQueryParams;

    public HttpResponseBody()  { }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(final String responseBody) {
        this.responseBody = responseBody;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(final String requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders == null ? Collections.emptyMap() : requestHeaders;
    }

    public void setRequestHeaders(final Map<String, List<String>> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public Map<String, String> getRequestQueryParams() {
        return requestQueryParams == null ? Collections.emptyMap() : requestQueryParams;
    }

    public void setRequestQueryParams(final Map<String, String> requestQueryParams) {
        this.requestQueryParams = requestQueryParams;
    }

    @Override
    public String toString() {
        // just output from Jackson ObjectMapper
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponseBody fromJson(String json) {
        try {
            return objectMapper.readValue(json, HttpResponseBody.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HttpResponseBody that = (HttpResponseBody) o;
        return Objects.equals(responseBody, that.responseBody) && Objects.equals(requestBody, that.requestBody) && Objects.equals(requestHeaders, that.requestHeaders) && Objects.equals(requestQueryParams, that.requestQueryParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseBody, requestBody, requestHeaders, requestQueryParams);
    }
}
