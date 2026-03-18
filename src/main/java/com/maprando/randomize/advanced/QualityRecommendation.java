package com.maprando.randomize.advanced;

/**
 * Quality recommendation for improving a seed.
 */
public class QualityRecommendation {
    private final String suggestion;
    private final String impact;

    public QualityRecommendation(String suggestion, String impact) {
        this.suggestion = suggestion;
        this.impact = impact;
    }

    public String getSuggestion() { return suggestion; }
    public String getImpact() { return impact; }
}
