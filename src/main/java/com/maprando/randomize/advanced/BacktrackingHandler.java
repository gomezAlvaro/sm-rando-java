package com.maprando.randomize.advanced;

import com.maprando.data.DataLoader;
import com.maprando.model.GameState;
import com.maprando.randomize.Location;
import com.maprando.traversal.TraversalState;

import java.util.*;

/**
 * Handles placement failures and retries during randomization.
 */
public class BacktrackingHandler {

    private final DataLoader dataLoader;
    private final Deque<PlacementAttempt> retryStack;
    private int retryCount;
    private int maxRetries;
    private final List<BacktrackingEvent> eventLog;

    public BacktrackingHandler(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
        this.retryStack = new ArrayDeque<>();
        this.retryCount = 0;
        this.maxRetries = 100;
        this.eventLog = new ArrayList<>();
    }

    public Deque<PlacementAttempt> getRetryStack() {
        return new ArrayDeque<>(retryStack);
    }

    public PlacementAttempt attemptPlacement(Location location, String itemId, TraversalState state) {
        PlacementAttempt attempt = new PlacementAttempt(location, itemId, state, true);
        retryStack.push(attempt);
        retryCount++;
        eventLog.add(new BacktrackingEvent("PLACEMENT_ATTEMPT", location.getId(), itemId));

        return attempt;
    }

    public boolean rollbackLastPlacement() {
        if (!retryStack.isEmpty()) {
            PlacementAttempt attempt = retryStack.pop();
            eventLog.add(new BacktrackingEvent("ROLLBACK", attempt.getLocationId(), attempt.getItemId()));
            return true;
        }
        return false;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public boolean isDeadlocked() {
        return hasExceededMaxRetries();
    }

    public boolean hasExceededMaxRetries() {
        return retryCount > maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public BacktrackingStatistics getStatistics() {
        return new BacktrackingStatistics(retryCount, retryStack.size(), eventLog.size());
    }

    public CombinedStatistics getCombinedStatistics(ProgressionManager progressionManager) {
        return new CombinedStatistics(
            retryStack.size(),
            progressionManager.getProgressionPercentage(),
            progressionManager.getProgressionItemIds().size()
        );
    }

    public List<BacktrackingEvent> getBacktrackingLog() {
        return new ArrayList<>(eventLog);
    }

    public void reset() {
        retryStack.clear();
        retryCount = 0;
        eventLog.clear();
    }
}
