package com.maprando.randomize.advanced;

/**
 * Accessibility metrics for a seed.
 */
public class AccessibilityMetrics {
    private final int accessibleLocations;
    private final int totalLocations;
    private final double accessibilityPercentage;

    public AccessibilityMetrics(int accessibleLocations, int totalLocations, double accessibilityPercentage) {
        this.accessibleLocations = accessibleLocations;
        this.totalLocations = totalLocations;
        this.accessibilityPercentage = accessibilityPercentage;
    }

    public int getAccessibleLocations() { return accessibleLocations; }
    public double getAccessibilityPercentage() { return accessibilityPercentage; }
}
