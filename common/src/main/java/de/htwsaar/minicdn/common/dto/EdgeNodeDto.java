package de.htwsaar.minicdn.common.dto;

/**
 * Used to send edge node information
 */
public record EdgeNodeDto(String url, String region, boolean online) {}
