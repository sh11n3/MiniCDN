package de.htwsaar.minicdn.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.htwsaar.minicdn.router.dto.EdgeNode;
import de.htwsaar.minicdn.router.service.EdgeHttpClient;
import de.htwsaar.minicdn.router.service.RouterStatsService;
import de.htwsaar.minicdn.router.service.RoutingIndex;
import de.htwsaar.minicdn.router.web.AdminStatsController;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.Test;

/** Tests für die Download-Statistik-Aggregation im Router-Admin-Stats-Endpoint. */
class AdminStatsControllerTest {

    @Test
    void shouldAggregateDownloadStatsByFileAndByEdge() {
        RouterStatsService routerStatsService = new RouterStatsService();
        RoutingIndex routingIndex = new RoutingIndex();
        routingIndex.addEdge("eu-west", new EdgeNode("http://edge-a:8081"));
        routingIndex.addEdge("eu-west", new EdgeNode("http://edge-b:8081"));

        EdgeHttpClient edgeHttpClient = new FakeEdgeHttpClient(Map.of(
                "http://edge-a:8081",
                """
                {
                  "cacheHits": 7,
                  "cacheMisses": 3,
                  "filesCached": 10,
                  "downloadsByFile": {
                    "docs/manual.pdf": 4,
                    "img/logo.png": 1
                  }
                }
                """,
                "http://edge-b:8081",
                """
                {
                  "cacheHits": 5,
                  "cacheMisses": 5,
                  "filesCached": 8,
                  "downloadsByFile": {
                    "docs/manual.pdf": 2,
                    "video/intro.mp4": 9
                  }
                }
                """));

        AdminStatsController controller = new AdminStatsController(
                routerStatsService,
                routingIndex,
                edgeHttpClient,
                new ObjectMapper());

        Map<String, Object> body = controller.getStats(60, true).getBody();
        assertTrue(body != null);

        @SuppressWarnings("unchecked")
        Map<String, Object> downloads = (Map<String, Object>) body.get("downloads");

        @SuppressWarnings("unchecked")
        Map<String, Number> byFileTotal = (Map<String, Number>) downloads.get("byFileTotal");

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Number>> byFileByEdge =
                (Map<String, Map<String, Number>>) downloads.get("byFileByEdge");

        assertEquals(6L, byFileTotal.get("docs/manual.pdf").longValue());
        assertEquals(1L, byFileTotal.get("img/logo.png").longValue());
        assertEquals(9L, byFileTotal.get("video/intro.mp4").longValue());

        assertEquals(4L, byFileByEdge.get("docs/manual.pdf").get("http://edge-a:8081").longValue());
        assertEquals(2L, byFileByEdge.get("docs/manual.pdf").get("http://edge-b:8081").longValue());
        assertEquals(1L, byFileByEdge.get("img/logo.png").get("http://edge-a:8081").longValue());
        assertEquals(9L, byFileByEdge.get("video/intro.mp4").get("http://edge-b:8081").longValue());
    }

    /** Test-Doppel für Edge-HTTP-Aufrufe mit statischen JSON-Payloads je Edge-URL. */
    private static final class FakeEdgeHttpClient extends EdgeHttpClient {
        private final Map<String, String> responsesByEdge;

        private FakeEdgeHttpClient(Map<String, String> responsesByEdge) {
            super(HttpClient.newHttpClient());
            this.responsesByEdge = responsesByEdge;
        }

        @Override
        public HttpResponse<String> fetchEdgeAdminStats(EdgeNode node, int windowSec, Duration timeout) {
            String body = responsesByEdge.get(node.url());
            return new SimpleHttpResponse(body == null ? "{}" : body, 200);
        }
    }

    /** Minimale HttpResponse-Implementierung für Controller-Unit-Tests. */
    private static final class SimpleHttpResponse implements HttpResponse<String> {
        private final String body;
        private final int statusCode;

        private SimpleHttpResponse(String body, int statusCode) {
            this.body = body;
            this.statusCode = statusCode;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return HttpRequest.newBuilder().uri(URI.create("http://localhost")).build();
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (a, b) -> true);
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return URI.create("http://localhost");
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

    }
}
