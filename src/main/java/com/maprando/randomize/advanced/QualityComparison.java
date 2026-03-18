package com.maprando.randomize.advanced;

import com.maprando.randomize.RandomizationResult;

/**
 * Comparison between two seed qualities.
 */
public class QualityComparison {
    private final RandomizationResult betterSeed;
    private final String comparisonSummary;

    public QualityComparison(RandomizationResult betterSeed, String comparisonSummary) {
        this.betterSeed = betterSeed;
        this.comparisonSummary = comparisonSummary;
    }

    public RandomizationResult getBetterSeed() { return betterSeed; }
    public String getComparisonSummary() { return comparisonSummary; }
}
