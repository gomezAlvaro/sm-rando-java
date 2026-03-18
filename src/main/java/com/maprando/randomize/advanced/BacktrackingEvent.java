package com.maprando.randomize.advanced;

/**
 * Represents an event in the backtracking log.
 */
public class BacktrackingEvent {
    private final String eventType;
    private final String locationId;
    private final String itemName;
    private final long timestamp;

    public BacktrackingEvent(String eventType, String locationId, String itemName) {
        this.eventType = eventType;
        this.locationId = locationId;
        this.itemName = itemName;
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventType() { return eventType; }
    public String getLocationId() { return locationId; }
    public String getItemName() { return itemName; }
    public long getTimestamp() { return timestamp; }
}
