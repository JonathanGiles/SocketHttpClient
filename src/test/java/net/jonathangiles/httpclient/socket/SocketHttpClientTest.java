package net.jonathangiles.httpclient.socket;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketHttpClientTest {

    private static final int PORT = 8000;
    private static HttpServer server;

    private final List<HttpContext> testContexts = new ArrayList<>();

    @BeforeAll
    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(null);
        server.start();
    }

    @AfterAll
    public static void stopServer() {
        server.stop(0);
    }

    @AfterEach
    public void clearContexts() {
        testContexts.forEach(httpContext -> server.removeContext(httpContext));
        testContexts.clear();
    }

    private void initContext(String path, HttpHandler handler) {
        testContexts.add(server.createContext(path, handler));
    }

    private SocketHttpClient createClient() {
        return new SocketHttpClient("localhost", PORT, null, null);
    }

    @Test
    public void testGet() throws IOException, URISyntaxException {
        final String expectedResponse = "Hello, world!";
        initContext("/", new GetRequestHandler(expectedResponse));
        final SocketHttpClient client = createClient();
        final HttpResponse response = client.sendRequest(new HttpRequest(HttpMethod.GET, "/"));
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void testPost() throws IOException, URISyntaxException {
//        final SocketHttpClient client = new SocketHttpClient("localhost", PORT, null, null);
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "text/plain");
//        HttpResponse response = client.sendRequest(new HttpRequest(HttpMethod.POST, "/", headers, "Hello, world!"));
//        assertEquals("Hello, world!", response.getBody());

        String expectedResponse = "Hello, world!";
        initContext("/", new PostRequestHandler());
        final SocketHttpClient client = createClient();
        final HttpResponse response = client.sendRequest(new HttpRequest(HttpMethod.POST, "/", expectedResponse));
        assertEquals("Received: " + expectedResponse, response.getBody());
    }

    static class GetRequestHandler implements HttpHandler {

        private String response;

        public GetRequestHandler(final String response) {
            this.response = response;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, response.length());
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(response.getBytes());
                responseBody.close();
            } else {
                fail("Expected a GET request");
            }
        }
    }

    static class PostRequestHandler implements HttpHandler {

        @Override
        public void handle(final HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // read request body
                final InputStream requestBody = exchange.getRequestBody();
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                final byte[] data = new byte[1024];
                while ((nRead = requestBody.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                final String requestBodyString = buffer.toString();

                // send response with request body
                final String response = "Received: " + requestBodyString;
                exchange.sendResponseHeaders(200, response.length());
                final OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(response.getBytes());
                responseBody.close();
            } else {
                fail("Expected a POST request");
            }
        }
    }
}