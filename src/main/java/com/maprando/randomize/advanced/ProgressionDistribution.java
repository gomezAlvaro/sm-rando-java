package com.maprando.randomize.advanced;

import java.util.HashMap;
import java.util.Map;

/**
 * Distribution of progression items through the game.
 */
public class ProgressionDistribution {
    private final double earlyGamePercentage;
    private final double midGamePercentage;
    private final double lateGamePercentage;
    private final Map<String, String> placements;

    public ProgressionDistribution(double earlyGamePercentage, double midGamePercentage, double lateGamePercentage) {
        this.earlyGamePercentage = earlyGamePercentage;
        this.midGamePercentage = midGamePercentage;
        this.lateGamePercentage = lateGamePercentage;
        this.placements = new HashMap<>();
    }

    public double getEarlyGamePercentage() { return earlyGamePercentage; }
    public double getMidGamePercentage() { return midGamePercentage; }
    public double getLateGamePercentage() { return lateGamePercentage; }

    /**
     * Get a copy of the placements map.
     */
    public Map<String, String> getPlacements() { return new HashMap<>(placements); }

    /**
     * Add a placement to the distribution.
     */
    public void addPlacement(String itemName, String locationId) {
        this.placements.put(itemName, locationId);
    }

    /**
     * Get the internal placements map for direct modification.
     * Use addPlacement() instead for better encapsulation.
     */
    Map<String, String> getPlacementsInternal() { return placements; }
}
