package com.maprando.randomize.advanced;

/**
 * Statistics about item placements during randomization.
 */
public class PlacementStatistics {

    private final int totalPlacements;
    private final int backtrackCount;
    private final int failedAttempts;

    public PlacementStatistics(int totalPlacements, int backtrackCount, int failedAttempts) {
        this.totalPlacements = totalPlacements;
        this.backtrackCount = backtrackCount;
        this.failedAttempts = failedAttempts;
    }

    public int getTotalPlacements() {
        return totalPlacements;
    }

    public int getBacktrackCount() {
        return backtrackCount;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    @Override
    public String toString() {
        return "PlacementStatistics{" +
                "total=" + totalPlacements +
                ", backtracks=" + backtrackCount +
                ", failed=" + failedAttempts +
                '}';
    }
}
