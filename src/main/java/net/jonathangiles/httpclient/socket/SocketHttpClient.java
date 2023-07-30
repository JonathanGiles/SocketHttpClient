package net.jonathangiles.httpclient.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

// A socket-based HTTP client using Java 8 language features and the Java Socket APIs to connect to
// HTTP endpoints, with support for cookies, redirects, and gzip compression
public class SocketHttpClient {
    private static final Pattern PATTERN_COLON_SPLIT = Pattern.compile(": ");

    private static final String HTTP_REQUEST_VERSION = " HTTP/1.0";

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public SocketHttpClient(final String host, final int port) {
        this(host, port, null, null);
    }

    public SocketHttpClient(final String host, final int port, final String username, final String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public HttpResponse sendRequest(final HttpRequest httpRequest) throws URISyntaxException {
        final URI uri = new URI("http", null, host, port, httpRequest.getPath(), null, null);
        final String request = buildRequest(httpRequest, uri);

        try (final Socket socket = new Socket(uri.getHost(), port);
             final OutputStream output = socket.getOutputStream()) {
            output.write(request.getBytes(StandardCharsets.UTF_8));

            try (final BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                return handleResponse(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        final String responseString = response.toString();
//        if (responseHeaders.containsKey("Set-Cookie")) {
//            final String[] cookieHeaders = responseString.split("Set-Cookie: ");
//            for (int i = 1; i < cookieHeaders.length; i++) {
//                final String[] cookieParts = cookieHeaders[i].split(";")[0].split("=");
//                cookies.put(cookieParts[0], cookieParts[1]);
//            }
//        }
//
//        if (responseString.contains("Location")) {
//            final String redirectUrl = responseString.split("Location: ")[1].split("\r\n")[0];
//            return sendRequest(method, redirectUrl, headers, body);
//        }
//
//        return responseString;

        // FIXME
        return null;
    }

    private String buildRequest(final HttpRequest httpRequest, final URI uri) {
        final StringBuilder request = new StringBuilder();
        request.append(httpRequest.getMethod().toString())
               .append(" ")
               .append(uri.getPath())
               .append(HTTP_REQUEST_VERSION)
               .append("\r\n")
               .append("Host: ")
               .append(uri.getHost());

        if (uri.getPort() != -1) {
            request.append(":").append(uri.getPort());
        }
        request.append("\r\n");

        httpRequest.getHeaders().forEach((key, value) -> request.append(key).append(": ").append(value).append("\r\n"));

//        if (!cookies.isEmpty()) {
//            final StringBuilder cookieHeader = new StringBuilder();
//            cookies.forEach((key, value) -> cookieHeader.append(key).append("=").append(value).append("; "));
//            request.append("Cookie: ").append(cookieHeader.toString().trim()).append("\r\n");
//        }

        if (username != null && password != null) {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            request.append("Authorization: ").append(authHeader).append("\r\n");
        }

        if (httpRequest.getBody() != null) {
            final byte[] bodyBytes = httpRequest.getBody();
            request.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
            request.append("\r\n");
            request.append(new String(bodyBytes, StandardCharsets.UTF_8));
        } else {
            request.append("\r\n");
        }

        return request.toString();
    }

    private HttpResponse handleResponse(final BufferedReader input) throws IOException {
        final String statusLine = input.readLine();
        final int responseStatusCode = Integer.parseInt(statusLine.split(" ")[1]);

        final Map<String, String> responseHeaders = new HashMap<>();
        String headerLine;
        while ((headerLine = input.readLine()) != null && !headerLine.isEmpty()) {
            final String[] parts = PATTERN_COLON_SPLIT.split(headerLine);
            responseHeaders.put(parts[0], parts[1]);
        }

        final StringBuilder bodyBuilder = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) { // && !line.isEmpty()) {
            bodyBuilder.append(line);
        }
        final String responseBody = bodyBuilder.toString();

        return new HttpResponse(responseStatusCode, responseHeaders, responseBody);
    }
}
