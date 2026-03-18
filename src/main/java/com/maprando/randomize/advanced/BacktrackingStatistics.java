package com.maprando.randomize.advanced;

/**
 * Statistics about backtracking operations.
 */
public class BacktrackingStatistics {
    private final int totalAttempts;
    private final int currentStackSize;
    private final int totalEvents;

    public BacktrackingStatistics(int totalAttempts, int currentStackSize, int totalEvents) {
        this.totalAttempts = totalAttempts;
        this.currentStackSize = currentStackSize;
        this.totalEvents = totalEvents;
    }

    public int getTotalAttempts() { return totalAttempts; }
    public int getCurrentStackSize() { return currentStackSize; }
    public int getTotalEvents() { return totalEvents; }
}
