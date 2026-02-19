package de.htwsaar.minicdn.router.service;

import de.htwsaar.minicdn.common.dto.MetricsInfoDto;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Verwaltet einfache Router-Metriken im Speicher.
 */
@Service
public class MetricsService {

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong routingErrors = new AtomicLong(0);
    private final Map<String, AtomicLong> regionStats = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> nodeSelectionStats = new ConcurrentHashMap<>();

    /**
     * Erfasst eine neue Anfrage für eine Region.
     *
     * @param region angefragte Region
     */
    public void recordRequest(String region) {
        totalRequests.incrementAndGet();
        regionStats.computeIfAbsent(region, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Erhöht den Auswahlzähler für einen Edge-Knoten.
     *
     * @param url URL des ausgewählten Knotens
     */
    public void recordNodeSelection(String url) {
        nodeSelectionStats.computeIfAbsent(url, k -> new AtomicLong(0)).incrementAndGet();
    }

    /** Erfasst einen Routingfehler. */
    public void recordError() {
        routingErrors.incrementAndGet();
    }

    /**
     * gets the current metrics as a DTO
     *
     * @return DTO with the current metrics values
     */
    public MetricsInfoDto getMetricsInfo() {

        Map<String, Long> regionCounts = regionStats.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        Map<String, Long> nodeCounts = nodeSelectionStats.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        return new MetricsInfoDto(totalRequests.get(), routingErrors.get(), regionCounts, nodeCounts);
    }
}
