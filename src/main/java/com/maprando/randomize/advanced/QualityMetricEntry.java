package com.maprando.randomize.advanced;

/**
 * Entry in the quality metrics history.
 */
public class QualityMetricEntry {
    private final String seed;
    private final double score;

    public QualityMetricEntry(String seed, double score) {
        this.seed = seed;
        this.score = score;
    }

    public String getSeed() { return seed; }
    public double getScore() { return score; }
}
