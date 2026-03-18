package com.maprando.randomize.advanced;

import com.maprando.data.DataLoader;
import com.maprando.model.GameState;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.Location;
import com.maprando.randomize.RandomizationResult;
import com.maprando.traversal.ReachabilityAnalysis;
import com.maprando.traversal.TraversalState;

import java.util.*;

/**
 * Calculates and analyzes quality metrics for randomized seeds.
 */
public class QualityMetricsCalculator {

    private final DataLoader dataLoader;
    private final List<QualityMetricEntry> qualityHistory;

    public QualityMetricsCalculator(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
        this.qualityHistory = new ArrayList<>();
    }

    public DataLoader getDataLoader() {
        return dataLoader;
    }

    public Double calculateReachablePercentage(RandomizationResult result) {
        TraversalState state = new TraversalState(GameState.standardStart());

        // Collect all items
        for (String itemId : result.getPlacements().values()) {
            if (itemId != null) {
                state.collectItem(itemId);
            }
        }

        ReachabilityAnalysis analysis = new ReachabilityAnalysis(dataLoader, state);
        return analysis.getReachablePercentage();
    }

    public DifficultyAssessment assessDifficulty(RandomizationResult result) {
        double reachablePercentage = calculateReachablePercentage(result);

        String difficulty;
        double score;

        if (reachablePercentage >= 80) {
            difficulty = "Easy";
            score = 2.0;
        } else if (reachablePercentage >= 60) {
            difficulty = "Normal";
            score = 5.0;
        } else if (reachablePercentage >= 40) {
            difficulty = "Hard";
            score = 7.0;
        } else {
            difficulty = "Expert";
            score = 9.0;
        }

        return new DifficultyAssessment(difficulty, score);
    }

    public double calculatePathQualityScore(RandomizationResult result) {
        // Simplified calculation
        double reachablePercentage = calculateReachablePercentage(result);
        return (reachablePercentage / 100.0) * 0.8 + 0.2;
    }

    public Double measureBacktracking(RandomizationResult result) {
        // Simplified backtracking measurement
        return 0.1; // Placeholder
    }

    public double calculateOverallQualityScore(RandomizationResult result) {
        double reachablePercentage = calculateReachablePercentage(result);
        double pathQuality = calculatePathQualityScore(result);

        return (reachablePercentage / 100.0 * 0.6 + pathQuality * 0.4) * 10.0;
    }

    public List<QualityIssue> identifyQualityIssues(RandomizationResult result) {
        List<QualityIssue> issues = new ArrayList<>();
        double reachablePercentage = calculateReachablePercentage(result);

        if (reachablePercentage < 50.0) {
            issues.add(new QualityIssue("Low Reachability",
                "Only " + String.format("%.1f%%", reachablePercentage) + " of locations are reachable",
                QualityIssue.Severity.HIGH,
                "Consider adjusting item placement to improve accessibility"));
        }

        return issues;
    }

    public List<QualityRecommendation> getQualityRecommendations(RandomizationResult result) {
        List<QualityRecommendation> recommendations = new ArrayList<>();
        double qualityScore = calculateOverallQualityScore(result);

        if (qualityScore < 5.0) {
            recommendations.add(new QualityRecommendation(
                "Redistribute progression items to improve accessibility",
                "Medium"));
        }

        return recommendations;
    }

    public QualityComparison compareSeedQuality(RandomizationResult result1, RandomizationResult result2) {
        double score1 = calculateOverallQualityScore(result1);
        double score2 = calculateOverallQualityScore(result2);

        RandomizationResult betterSeed = score1 > score2 ? result1 : result2;
        String summary = String.format("Seed 1: %.2f, Seed 2: %.2f", score1, score2);

        return new QualityComparison(betterSeed, summary);
    }

    public ProgressionDistribution calculateProgressionDistribution(RandomizationResult result) {
        return new ProgressionDistribution(
            calculateReachablePercentage(result),
            calculateReachablePercentage(result) * 0.8,
            calculateReachablePercentage(result) * 0.6
        );
    }

    public PlacementBalance assessPlacementBalance(RandomizationResult result) {
        double balanceScore = 0.8; // Placeholder
        return new PlacementBalance(balanceScore);
    }

    public AccessibilityMetrics calculateAccessibilityMetrics(RandomizationResult result) {
        double reachablePercentage = calculateReachablePercentage(result);
        int totalLocations = dataLoader.getLocationData().getLocations().size();
        int accessibleLocations = (int) ((reachablePercentage / 100.0) * totalLocations);

        return new AccessibilityMetrics(accessibleLocations, totalLocations, reachablePercentage);
    }

    public QualityRating getQualityRating(RandomizationResult result) {
        double score = calculateOverallQualityScore(result);

        String rating;
        if (score >= 8.0) rating = "Excellent";
        else if (score >= 6.0) rating = "Good";
        else if (score >= 4.0) rating = "Fair";
        else rating = "Poor";

        return new QualityRating(rating, score);
    }

    public void trackSeedQuality(RandomizationResult result) {
        double score = calculateOverallQualityScore(result);
        qualityHistory.add(new QualityMetricEntry(result.getSeed(), score));
    }

    public QualityMetricsSummary getQualityMetricsSummary() {
        if (qualityHistory.isEmpty()) {
            return new QualityMetricsSummary(0, 0.0);
        }

        double average = qualityHistory.stream()
            .mapToDouble(QualityMetricEntry::getScore)
            .average()
            .orElse(0.0);

        return new QualityMetricsSummary(qualityHistory.size(), average);
    }

    public QualityBreakdown getQualityBreakdown(RandomizationResult result) {
        double reachabilityScore = calculateReachablePercentage(result);
        double progressionScore = calculatePathQualityScore(result);
        double balanceScore = 0.8; // Placeholder

        return new QualityBreakdown(reachabilityScore, progressionScore, balanceScore);
    }

    public String generateQualityReport(RandomizationResult result) {
        QualityRating rating = getQualityRating(result);

        StringBuilder report = new StringBuilder();
        report.append("=== Quality Report ===\n");
        report.append("Seed: ").append(result.getSeed()).append("\n");
        report.append("Rating: ").append(rating.getRating()).append("\n");
        report.append("Score: ").append(String.format("%.2f", rating.getScore())).append("\n");
        report.append("Reachable: ").append(String.format("%.1f%%", calculateReachablePercentage(result))).append("\n");

        return report.toString();
    }
}
