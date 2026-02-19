package de.htwsaar.minicdn.common.dto;

import java.util.Map;

/**
 * DTO for metrics
 *
 * has :
 * - total no of requests
 * - no of routing errors
 * - requests by region
 */
public record MetricsInfoDto(
        long totalRequests,
        long routingErrors,
        Map<String, Long> requestsByRegion,
        Map<String, Long> selectionsByNode) {}
