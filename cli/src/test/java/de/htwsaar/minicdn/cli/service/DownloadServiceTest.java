package de.htwsaar.minicdn.cli.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DownloadServiceTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void downloadFile_writesResponseBody() throws IOException {
        server.createContext(
                "/api/cdn/files/hello.txt",
                new TextHandler(200, "hello world"));

        DownloadService service = new DownloadService();
        Path tempDir = Files.createTempDirectory("minicdn-download-");
        Path output = tempDir.resolve("hello.txt");

        int rc = service.downloadFile(baseUrl, "eu", "hello.txt", output, true);

        assertEquals(0, rc);
        assertTrue(Files.exists(output));
        assertEquals("hello world", Files.readString(output));
    }

    @Test
    void downloadFile_returnsNotFoundCode() {
        server.createContext(
                "/api/cdn/files/missing.txt",
                new TextHandler(404, "not found"));

        DownloadService service = new DownloadService();
        int rc = service.downloadFile(baseUrl, "eu", "missing.txt", Path.of("build/missing.txt"), true);

        assertEquals(3, rc);
    }

    @Test
    void downloadFile_returnsBadRequestCode() {
        server.createContext(
                "/api/cdn/files/bad.txt",
                new TextHandler(400, "bad request"));

        DownloadService service = new DownloadService();
        int rc = service.downloadFile(baseUrl, "eu", "bad.txt", Path.of("build/bad.txt"), true);

        assertEquals(2, rc);
    }

    private static class TextHandler implements HttpHandler {
        private final int status;
        private final String body;

        private TextHandler(int status, String body) {
            this.status = status;
            this.body = body;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, payload.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(payload);
            }
        }
    }
}
