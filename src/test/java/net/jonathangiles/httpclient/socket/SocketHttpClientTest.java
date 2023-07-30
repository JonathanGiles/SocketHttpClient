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

    private void assertResponseEquals(HttpRequest request, HttpResponse response) {
        // Because we echo the request in the response, we should test that everything in the
        // request was received and parsed in the response
        HttpResponseBody expectedResponse = HttpResponseBody.fromJson(response.getBody());

        expectedResponse.getRequestHeaders().remove("Host"); // this is added by the client
        expectedResponse.getRequestHeaders().remove("Content-length"); // this is added by the client

        if (request.getBody() == null) {
            assertNull(expectedResponse.getRequestBody());
        } else {
            assertEquals(expectedResponse.getResponseBody(), new String(request.getBody()));
        }
        assertEquals(expectedResponse.getRequestHeaders(), request.getHeaders());
        assertEquals(expectedResponse.getRequestQueryParams(), request.getQueryParams());
    }

    @Test
    public void testGet() throws IOException, URISyntaxException {
//        final String expectedResponse = "Hello, world!";
        initContext("/", new EchoingRequestHandler());
        final SocketHttpClient client = createClient();
        final HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "/");
        final HttpResponse httpResponse = client.sendRequest(httpRequest);
        assertResponseEquals(httpRequest, httpResponse);
    }

    @Test
    public void testGet_queryParams_oneQueryParam() throws IOException, URISyntaxException {
        initContext("/", new EchoingRequestHandler());
        final SocketHttpClient client = createClient();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "/")
                .addQueryParam("param1", "value1");
        final HttpResponse httpResponse = client.sendRequest(httpRequest);
        assertResponseEquals(httpRequest, httpResponse);
    }

    @Test
    public void testGet_queryParams_twoQueryParams() throws IOException, URISyntaxException {
        initContext("/", new EchoingRequestHandler());
        final SocketHttpClient client = createClient();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "/")
                .addQueryParam("param1", "value1")
                .addQueryParam("param2", "value2");
        final HttpResponse httpResponse = client.sendRequest(httpRequest);
        assertResponseEquals(httpRequest, httpResponse);
    }

    @Test
    public void testPost() throws IOException, URISyntaxException {
//        HttpResponse response = client.sendRequest(new HttpRequest(HttpMethod.POST, "/", headers, "Hello, world!"));
//        assertEquals("Hello, world!", response.getBody());

//        String expectedResponse = "Hello, world!";
        initContext("/", new EchoingRequestHandler());
        final SocketHttpClient client = createClient();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, "/", "Hello World!");
        final HttpResponse httpResponse = client.sendRequest(httpRequest);
        assertResponseEquals(httpRequest, httpResponse);
//        assertEquals("Received: " + expectedResponse, response.getBody());
    }

    static class EchoingRequestHandler implements HttpHandler {

//        private String response;

//        public GetRequestHandler(final String response) {
//            this.response = response;
//        }

        @Override
        public void handle(HttpExchange exchange) {
//            if ("GET".equals(exchange.getRequestMethod())) {
                // We just echo back the request as the response, encoding it in all the response body
                HttpResponseBody responseBody = new HttpResponseBody();

                // get the query params from the HTTP request
                // create a map of the query params to values
                final Map<String, String> queryParams = getQueryParams(exchange.getRequestURI().getQuery());
                responseBody.setRequestQueryParams(queryParams);

                responseBody.setRequestHeaders(exchange.getRequestHeaders());

                // convert exchange body to string
                final InputStream requestBody = exchange.getRequestBody();
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                final byte[] data = new byte[1024];
                try {
                    while ((nRead = requestBody.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    if (buffer.size() > 0) {
                        responseBody.setResponseBody(buffer.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String responseAsJsonString = responseBody.toString();

                try {
                    exchange.sendResponseHeaders(200, responseAsJsonString.length());
                    exchange.getResponseBody().write(responseAsJsonString.getBytes());
                    exchange.getResponseBody().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//            } else {
//                fail("Expected a GET request");
//            }
        }
    }

//    static class PostRequestHandler implements HttpHandler {
//
//        @Override
//        public void handle(final HttpExchange exchange) throws IOException {
//            if ("POST".equals(exchange.getRequestMethod())) {
//                // read request body
//                final InputStream requestBody = exchange.getRequestBody();
//                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//                int nRead;
//                final byte[] data = new byte[1024];
//                while ((nRead = requestBody.read(data, 0, data.length)) != -1) {
//                    buffer.write(data, 0, nRead);
//                }
//                buffer.flush();
//                final String requestBodyString = buffer.toString();
//
//                // send response with request body
//                final String response = "Received: " + requestBodyString;
//                exchange.sendResponseHeaders(200, response.length());
//                final OutputStream responseBody = exchange.getResponseBody();
//                responseBody.write(response.getBytes());
//                responseBody.close();
//            } else {
//                fail("Expected a POST request");
//            }
//        }
//    }

    private static Map<String, String> getQueryParams(String query){
        // get the query params from the HTTP request
        // create a map of the query params to values
        final Map<String, String> queryParams = new HashMap<>();
        if (query != null) {
            final String[] pairs = query.split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                queryParams.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
        }
        return queryParams;
    }
}