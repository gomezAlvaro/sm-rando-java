package com.maprando.randomize;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Represents the result of a randomization process.
 * Contains information about the seed, placement, and metadata.
 */
public class RandomizationResult {
    private final String seed;
    private final LocalDateTime timestamp;
    private final Map<String, String> placements; // locationId -> itemId
    private final Map<String, String> locationNames;
    private final List<String> warnings;
    private final boolean successful;
    private final String algorithmUsed;

    private RandomizationResult(Builder builder) {
        this.seed = builder.seed;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
        this.placements = Map.copyOf(builder.placements);
        this.locationNames = Map.copyOf(builder.locationNames);
        this.warnings = List.copyOf(builder.warnings);
        this.successful = builder.successful;
        this.algorithmUsed = builder.algorithmUsed;
    }

    /**
     * Gets the random seed used for this randomization.
     */
    public String getSeed() {
        return seed;
    }

    /**
     * Gets the timestamp when this randomization was created.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Gets a formatted string of the timestamp.
     */
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Gets the item ID placed at a specific location.
     */
    public String getItemAtLocation(String locationId) {
        return placements.get(locationId);
    }

    /**
     * Gets all location-to-item placements.
     */
    public Map<String, String> getPlacements() {
        return placements;
    }

    /**
     * Gets the name of a location by ID.
     */
    public String getLocationName(String locationId) {
        return locationNames.getOrDefault(locationId, locationId);
    }

    /**
     * Gets all warnings generated during randomization.
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Checks if the randomization was successful.
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets the algorithm used for randomization.
     */
    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    /**
     * Gets the number of items placed.
     */
    public int getPlacementCount() {
        return placements.size();
    }

    /**
     * Gets all placed item IDs as a set.
     */
    public Set<String> getPlacedItemIds() {
        return Set.copyOf(placements.values());
    }

    /**
     * Generates a human-readable spoiler log.
     */
    public String generateSpoilerLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MAP RANDOMIZER SPOILER LOG ===\n");
        sb.append("Seed: ").append(seed).append("\n");
        sb.append("Timestamp: ").append(getFormattedTimestamp()).append("\n");
        sb.append("Algorithm: ").append(algorithmUsed).append("\n");
        sb.append("Status: ").append(successful ? "SUCCESS" : "FAILED").append("\n");
        sb.append("Items Placed: ").append(getPlacementCount()).append("\n");

        if (!warnings.isEmpty()) {
            sb.append("\n=== WARNINGS ===\n");
            for (String warning : warnings) {
                sb.append("  - ").append(warning).append("\n");
            }
        }

        // Group by region
        Map<String, List<Map.Entry<String, String>>> byRegion = new HashMap<>();
        for (Map.Entry<String, String> entry : placements.entrySet()) {
            String locationId = entry.getKey();
            String locationName = locationNames.getOrDefault(locationId, locationId);

            // Extract region from location name (format: "Name (Region)")
            String region = "Unknown";
            int parenIndex = locationName.lastIndexOf(" (");
            if (parenIndex > 0 && locationName.endsWith(")")) {
                region = locationName.substring(parenIndex + 2, locationName.length() - 1);
            }

            byRegion.computeIfAbsent(region, k -> new ArrayList<>()).add(entry);
        }

        sb.append("\n=== ITEM PLACEMENTS ===\n");
        for (Map.Entry<String, List<Map.Entry<String, String>>> regionEntry : byRegion.entrySet()) {
            sb.append("\n[").append(regionEntry.getKey()).append("]\n");
            for (Map.Entry<String, String> entry : regionEntry.getValue()) {
                String locationId = entry.getKey();
                String itemId = entry.getValue();
                String locationName = locationNames.getOrDefault(locationId, locationId);
                sb.append("  ").append(locationName).append(" -> ").append(itemId).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Creates a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating RandomizationResult objects.
     */
    public static class Builder {
        private String seed;
        private LocalDateTime timestamp;
        private Map<String, String> placements = new HashMap<>();
        private Map<String, String> locationNames = new HashMap<>();
        private List<String> warnings = new ArrayList<>();
        private boolean successful = true;
        private String algorithmUsed = "Unknown";

        public Builder seed(String seed) {
            this.seed = seed;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder addPlacement(String locationId, String itemId) {
            this.placements.put(locationId, itemId);
            return this;
        }

        public Builder addPlacement(String locationId, String locationName, String itemId) {
            this.placements.put(locationId, itemId);
            this.locationNames.put(locationId, locationName);
            return this;
        }

        public Builder placements(Map<String, String> placements) {
            this.placements = new HashMap<>(placements);
            return this;
        }

        public Builder locationNames(Map<String, String> locationNames) {
            this.locationNames = new HashMap<>(locationNames);
            return this;
        }

        public Builder addWarning(String warning) {
            this.warnings.add(warning);
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = new ArrayList<>(warnings);
            return this;
        }

        public Builder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public Builder algorithmUsed(String algorithmUsed) {
            this.algorithmUsed = algorithmUsed;
            return this;
        }

        public RandomizationResult build() {
            if (seed == null) {
                seed = UUID.randomUUID().toString().substring(0, 8);
            }
            return new RandomizationResult(this);
        }
    }
}
