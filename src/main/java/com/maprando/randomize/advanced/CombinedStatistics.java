package com.maprando.randomize.advanced;

/**
 * Combined statistics from multiple sources.
 */
public class CombinedStatistics {
    private final int totalPlacements;
    private final double progressionPercentage;
    private final int progressionItemCount;

    public CombinedStatistics(int totalPlacements, double progressionPercentage, int progressionItemCount) {
        this.totalPlacements = totalPlacements;
        this.progressionPercentage = progressionPercentage;
        this.progressionItemCount = progressionItemCount;
    }

    public int getTotalPlacements() { return totalPlacements; }
    public double getProgressionPercentage() { return progressionPercentage; }
    public int getProgressionItemCount() { return progressionItemCount; }
}
