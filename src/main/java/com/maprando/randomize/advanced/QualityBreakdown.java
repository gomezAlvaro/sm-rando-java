package com.maprando.randomize.advanced;

/**
 * Breakdown of quality scores by category.
 */
public class QualityBreakdown {
    private final double reachabilityScore;
    private final double progressionScore;
    private final double balanceScore;

    public QualityBreakdown(double reachabilityScore, double progressionScore, double balanceScore) {
        this.reachabilityScore = reachabilityScore;
        this.progressionScore = progressionScore;
        this.balanceScore = balanceScore;
    }

    public double getReachabilityScore() { return reachabilityScore; }
    public double getProgressionScore() { return progressionScore; }
    public double getBalanceScore() { return balanceScore; }
}
