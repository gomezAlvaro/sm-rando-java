package com.maprando.randomize.advanced;

/**
 * Balance recommendation for improving seed quality.
 */
public class BalanceRecommendation {
    private final String suggestion;
    private final String impact;

    public BalanceRecommendation(String suggestion, String impact) {
        this.suggestion = suggestion;
        this.impact = impact;
    }

    public String getSuggestion() { return suggestion; }
    public String getImpact() { return impact; }
}
