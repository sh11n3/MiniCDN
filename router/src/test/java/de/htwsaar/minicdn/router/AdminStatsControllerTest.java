package de.htwsaar.minicdn.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.htwsaar.minicdn.router.dto.EdgeNode;
import de.htwsaar.minicdn.router.service.EdgeHttpClient;
import de.htwsaar.minicdn.router.service.RouterStatsService;
import de.htwsaar.minicdn.router.service.RoutingIndex;
import de.htwsaar.minicdn.router.web.AdminStatsController;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests für die Download-Statistik-Aggregation im AdminStatsController. */
class AdminStatsControllerTest {

    /**
     * Stellt sicher, dass Downloadzahlen pro Datei über alle Edges sowie pro Edge aggregiert werden.
     */
    @SuppressWarnings("unchecked")
    @Test
    void shouldAggregateDownloadsByFileAndByEdge() throws Exception {
        RouterStatsService routerStatsService = new RouterStatsService();
        RoutingIndex routingIndex = new RoutingIndex();
        routingIndex.addEdge("EU", new EdgeNode("http://edge-1:8081", "EU"));
        routingIndex.addEdge("EU", new EdgeNode("http://edge-2:8082", "EU"));

        EdgeHttpClient edgeHttpClient = mock(EdgeHttpClient.class);
        HttpResponse<String> firstEdgeResponse = mock(HttpResponse.class);
        HttpResponse<String> secondEdgeResponse = mock(HttpResponse.class);

        when(firstEdgeResponse.statusCode()).thenReturn(200);
        when(firstEdgeResponse.body())
                .thenReturn("{" +
                        "\"cacheHits\":2," +
                        "\"cacheMisses\":1," +
                        "\"filesCached\":3," +
                        "\"downloadsByFile\":{\"a.txt\":5,\"b.txt\":2}" +
                        "}");

        when(secondEdgeResponse.statusCode()).thenReturn(200);
        when(secondEdgeResponse.body())
                .thenReturn("{" +
                        "\"cacheHits\":1," +
                        "\"cacheMisses\":4," +
                        "\"filesCached\":2," +
                        "\"downloadsByFile\":{\"a.txt\":7,\"c.txt\":1}" +
                        "}");

        when(edgeHttpClient.fetchEdgeAdminStats(any(EdgeNode.class), anyInt(), any())).thenReturn(firstEdgeResponse, secondEdgeResponse);

        AdminStatsController controller =
                new AdminStatsController(routerStatsService, routingIndex, edgeHttpClient, new ObjectMapper());

        Map<String, Object> body = controller.getStats(60, true).getBody();
        Map<String, Object> downloads = (Map<String, Object>) body.get("downloads");
        Map<String, Number> totals = (Map<String, Number>) downloads.get("byFileTotal");
        Map<String, Map<String, Number>> byEdge = (Map<String, Map<String, Number>>) downloads.get("byFileByEdge");

        assertEquals(12L, totals.get("a.txt").longValue());
        assertEquals(2L, totals.get("b.txt").longValue());
        assertEquals(1L, totals.get("c.txt").longValue());

        assertEquals(5L, byEdge.get("a.txt").get("http://edge-1:8081").longValue());
        assertEquals(7L, byEdge.get("a.txt").get("http://edge-2:8082").longValue());
        assertEquals(2L, byEdge.get("b.txt").get("http://edge-1:8081").longValue());
        assertEquals(1L, byEdge.get("c.txt").get("http://edge-2:8082").longValue());
    }
}
