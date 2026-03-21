package com.maprando.web.dto;

import java.util.List;
import java.util.Map;

/**
 * Structured spoiler data for the web UI.
 * Contains item placements organized by region with metadata.
 */
public record SpoilerData(
        String seedId,
        String seed,
        String status,
        int itemsPlaced,
        Map<String, List<ItemPlacement>> placementsByRegion,
        ItemSummary majorItems,
        ItemSummary resourceTanks
) {
    /**
     * Represents a single item placement.
     */
    public record ItemPlacement(
            String locationId,
            String locationName,
            String itemId,
            String itemName,
            String region,
            boolean isProgression
    ) {}

    /**
     * Summary of items by type.
     */
    public record ItemSummary(
            Map<String, Integer> items
    ) {}
}
