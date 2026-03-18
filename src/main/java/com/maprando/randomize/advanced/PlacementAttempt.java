package com.maprando.randomize.advanced;

import com.maprando.randomize.Location;
import com.maprando.traversal.TraversalState;

/**
 * Represents a single placement attempt during randomization.
 */
public class PlacementAttempt {
    private final Location location;
    private final String itemId;
    private final TraversalState state;
    private final boolean wasAttempted;
    private final long timestamp;

    public PlacementAttempt(Location location, String itemId, TraversalState state, boolean wasAttempted) {
        this.location = location;
        this.itemId = itemId;
        this.state = state;
        this.wasAttempted = wasAttempted;
        this.timestamp = System.currentTimeMillis();
    }

    public Location getLocation() { return location; }
    public String getItemId() { return itemId; }
    public TraversalState getState() { return state; }
    public boolean wasAttempted() { return wasAttempted; }
    public long getTimestamp() { return timestamp; }
    public String getLocationId() { return location.getId(); }
}
