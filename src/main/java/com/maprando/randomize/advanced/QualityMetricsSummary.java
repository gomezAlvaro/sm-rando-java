package com.maprando.randomize.advanced;

/**
 * Summary of quality metrics across multiple seeds.
 */
public class QualityMetricsSummary {
    private final int seedCount;
    private final double averageQuality;

    public QualityMetricsSummary(int seedCount, double averageQuality) {
        this.seedCount = seedCount;
        this.averageQuality = averageQuality;
    }

    public int getSeedCount() { return seedCount; }
    public double getAverageQuality() { return averageQuality; }
}
