package com.maprando.traversal;

/**
 * Quality metrics for a randomized seed.
 */
public class SeedQualityMetrics {

    private final double reachablePercentage;
    private final double pathQualityScore;
    private final double pathDiversity;
    private final String difficultyRating;
    private final int criticalPathLength;
    private final double backtrackingAmount;

    public SeedQualityMetrics(double reachablePercentage, double pathQualityScore,
                            double pathDiversity, String difficultyRating,
                            int criticalPathLength, double backtrackingAmount) {
        this.reachablePercentage = reachablePercentage;
        this.pathQualityScore = pathQualityScore;
        this.pathDiversity = pathDiversity;
        this.difficultyRating = difficultyRating;
        this.criticalPathLength = criticalPathLength;
        this.backtrackingAmount = backtrackingAmount;
    }

    public double getReachablePercentage() {
        return reachablePercentage;
    }

    public double getPathQualityScore() {
        return pathQualityScore;
    }

    public double getPathDiversity() {
        return pathDiversity;
    }

    public String getDifficultyRating() {
        return difficultyRating;
    }

    public int getCriticalPathLength() {
        return criticalPathLength;
    }

    public double getBacktrackingAmount() {
        return backtrackingAmount;
    }

    @Override
    public String toString() {
        return "SeedQualityMetrics{" +
                "reachable=" + String.format("%.1f%%", reachablePercentage) +
                ", pathQuality=" + String.format("%.2f", pathQualityScore) +
                ", difficulty=" + difficultyRating +
                '}';
    }
}