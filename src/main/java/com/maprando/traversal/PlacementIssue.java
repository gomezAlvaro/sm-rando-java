package com.maprando.traversal;

/**
 * Represents a potential issue with item placement in a seed.
 */
public class PlacementIssue {

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    private final String location;
    private final String description;
    private final Severity severity;
    private final String suggestion;

    public PlacementIssue(String location, String description,
                         Severity severity, String suggestion) {
        this.location = location;
        this.description = description;
        this.severity = severity;
        this.suggestion = suggestion;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getSuggestion() {
        return suggestion;
    }

    @Override
    public String toString() {
        return "PlacementIssue{" +
                "location='" + location + '\'' +
                ", severity=" + severity +
                ", description='" + description + '\'' +
                '}';
    }
}