package com.maprando.randomize.advanced;

/**
 * Represents a quality issue found in a seed.
 */
public class QualityIssue {
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    private final String title;
    private final String description;
    private final Severity severity;
    private final String suggestion;

    public QualityIssue(String title, String description, Severity severity, String suggestion) {
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.suggestion = suggestion;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Severity getSeverity() { return severity; }
    public String getSuggestion() { return suggestion; }
}
