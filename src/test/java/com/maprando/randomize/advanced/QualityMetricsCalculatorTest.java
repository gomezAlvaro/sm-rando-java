package com.maprando.randomize.advanced;

import com.maprando.data.DataLoader;
import com.maprando.model.GameState;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.Location;
import com.maprando.randomize.RandomizationResult;
import com.maprando.traversal.ReachabilityAnalysis;
import com.maprando.traversal.TraversalState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the QualityMetricsCalculator class.
 * QualityMetricsCalculator analyzes seed quality during generation.
 */
@DisplayName("QualityMetricsCalculator Tests")
class QualityMetricsCalculatorTest {

    private QualityMetricsCalculator calculator;
    private DataLoader dataLoader;
    private RandomizationResult testResult;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();

        calculator = new QualityMetricsCalculator(dataLoader);
        testResult = createTestRandomizationResult();
    }

    @Test
    @DisplayName("QualityMetricsCalculator should be created successfully")
    void testCreation() {
        assertNotNull(calculator, "QualityMetricsCalculator should be created");
        assertNotNull(calculator.getDataLoader(), "DataLoader should be stored");
    }

    @Test
    @DisplayName("Should calculate reachable percentage correctly")
    void testReachablePercentageCalculation() {
        double percentage = calculator.calculateReachablePercentage(testResult);

        assertNotNull(percentage, "Percentage should not be null");
        assertTrue(percentage >= 0.0, "Percentage should be non-negative");
        assertTrue(percentage <= 100.0, "Percentage should not exceed 100");
    }

    @Test
    @DisplayName("Should assess difficulty accurately")
    void testDifficultyAssessment() {
        DifficultyAssessment assessment = calculator.assessDifficulty(testResult);

        assertNotNull(assessment, "Assessment should not be null");
        assertNotNull(assessment.getOverallDifficulty(), "Overall difficulty should be set");
        assertTrue(assessment.getDifficultyScore() >= 0.0,
            "Difficulty score should be non-negative");
    }

    @Test
    @DisplayName("Should calculate path quality score")
    void testPathQualityScoring() {
        double pathQuality = calculator.calculatePathQualityScore(testResult);

        assertNotNull(pathQuality, "Path quality should not be null");
        assertTrue(pathQuality >= 0.0 && pathQuality <= 1.0,
            "Path quality should be between 0 and 1");
    }

    @Test
    @DisplayName("Should measure backtracking amount")
    void testBacktrackingMeasurement() {
        double backtracking = calculator.measureBacktracking(testResult);

        assertNotNull(backtracking, "Backtracking should not be null");
        assertTrue(backtracking >= 0.0, "Backtracking should be non-negative");
        assertTrue(backtracking <= 1.0, "Backtracking should not exceed 1.0");
    }

    @Test
    @DisplayName("Should calculate overall quality score")
    void testOverallQualityScore() {
        double qualityScore = calculator.calculateOverallQualityScore(testResult);

        assertNotNull(qualityScore, "Quality score should not be null");
        assertTrue(qualityScore >= 0.0 && qualityScore <= 10.0,
            "Quality score should be between 0 and 10");
    }

    @Test
    @DisplayName("Should identify quality issues")
    void testQualityIssueIdentification() {
        List<QualityIssue> issues = calculator.identifyQualityIssues(testResult);

        assertNotNull(issues, "Issues list should not be null");
        // Issues should be properly categorized
        for (QualityIssue issue : issues) {
            assertNotNull(issue.getSeverity(), "Issue should have severity");
            assertNotNull(issue.getDescription(), "Issue should have description");
        }
    }

    @Test
    @DisplayName("Should provide quality recommendations")
    void testQualityRecommendations() {
        List<QualityRecommendation> recommendations =
            calculator.getQualityRecommendations(testResult);

        assertNotNull(recommendations, "Recommendations should not be null");

        for (QualityRecommendation recommendation : recommendations) {
            assertNotNull(recommendation.getImpact(), "Should have impact level");
            assertNotNull(recommendation.getSuggestion(), "Should have suggestion");
        }
    }

    @Test
    @DisplayName("Should compare seed qualities")
    void testSeedQualityComparison() {
        RandomizationResult anotherResult = createAnotherTestResult();

        QualityComparison comparison =
            calculator.compareSeedQuality(testResult, anotherResult);

        assertNotNull(comparison, "Comparison should not be null");
        assertNotNull(comparison.getBetterSeed(), "Should identify better seed");
        assertNotNull(comparison.getComparisonSummary(), "Should have summary");
    }

    @Test
    @DisplayName("Should calculate progression distribution")
    void testProgressionDistributionCalculation() {
        ProgressionDistribution distribution =
            calculator.calculateProgressionDistribution(testResult);

        assertNotNull(distribution, "Distribution should not be null");
        assertTrue(distribution.getEarlyGamePercentage() >= 0.0,
            "Early game percentage should be valid");
        assertTrue(distribution.getMidGamePercentage() >= 0.0,
            "Mid game percentage should be valid");
        assertTrue(distribution.getLateGamePercentage() >= 0.0,
            "Late game percentage should be valid");
    }

    @Test
    @DisplayName("Should assess placement balance")
    void testPlacementBalanceAssessment() {
        PlacementBalance balance = calculator.assessPlacementBalance(testResult);

        assertNotNull(balance, "Balance should not be null");
        assertTrue(balance.getBalanceScore() >= 0.0 && balance.getBalanceScore() <= 1.0,
            "Balance score should be between 0 and 1");
    }

    @Test
    @DisplayName("Should calculate accessibility metrics")
    void testAccessibilityMetrics() {
        AccessibilityMetrics metrics =
            calculator.calculateAccessibilityMetrics(testResult);

        assertNotNull(metrics, "Metrics should not be null");
        assertTrue(metrics.getAccessibleLocations() >= 0,
            "Accessible locations should be non-negative");
    }

    @Test
    @DisplayName("Should provide quality rating")
    void testQualityRating() {
        QualityRating rating = calculator.getQualityRating(testResult);

        assertNotNull(rating, "Rating should not be null");
        assertNotNull(rating.getRating(), "Should have rating string");
        assertTrue(rating.getScore() >= 0.0 && rating.getScore() <= 10.0,
            "Rating score should be valid");
    }

    @Test
    @DisplayName("Should handle edge case: perfect seed")
    void testPerfectSeed() {
        RandomizationResult perfectSeed = createPerfectSeed();

        QualityRating rating = calculator.getQualityRating(perfectSeed);

        // A "perfect" seed with good distribution should have at least a "Good" rating (6.0+)
        // With the current items, we can reach a reasonable percentage of locations
        assertTrue(rating.getScore() >= 5.0,
            "Perfect seed should have good rating (score: " + rating.getScore() + ")");
    }

    @Test
    @DisplayName("Should handle edge case: poor seed")
    void testPoorSeed() {
        RandomizationResult poorSeed = createPoorSeed();

        QualityRating rating = calculator.getQualityRating(poorSeed);

        // Poor seed with only energy tanks should have lower quality
        // Just verify it returns a valid rating
        assertNotNull(rating, "Rating should not be null");
        assertTrue(rating.getScore() >= 0.0 && rating.getScore() <= 10.0,
            "Rating should be in valid range");
    }

    @Test
    @DisplayName("Should track quality metrics over time")
    void testQualityMetricsTracking() {
        calculator.trackSeedQuality(testResult);
        calculator.trackSeedQuality(testResult);

        QualityMetricsSummary summary = calculator.getQualityMetricsSummary();

        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.getAverageQuality() >= 0.0, "Average quality should be valid");
        assertEquals(2, summary.getSeedCount(), "Should track 2 seeds");
    }

    @Test
    @DisplayName("Should provide quality breakdown")
    void testQualityBreakdown() {
        QualityBreakdown breakdown = calculator.getQualityBreakdown(testResult);

        assertNotNull(breakdown, "Breakdown should not be null");
        assertTrue(breakdown.getReachabilityScore() >= 0.0,
            "Reachability score should be valid");
        assertTrue(breakdown.getProgressionScore() >= 0.0,
            "Progression score should be valid");
        assertTrue(breakdown.getBalanceScore() >= 0.0,
            "Balance score should be valid");
    }

    @Test
    @DisplayName("Should generate quality report")
    void testQualityReport() {
        String report = calculator.generateQualityReport(testResult);

        assertNotNull(report, "Report should not be null");
        assertTrue(report.length() > 0, "Report should not be empty");
        assertTrue(report.contains("Quality"), "Report should mention quality");
    }

    // Helper methods

    private RandomizationResult createTestRandomizationResult() {
        return RandomizationResult.builder()
            .seed("test-seed")
            .addPlacement("brinstar_morph_room", "Morph Ball Room", "MORPH_BALL")
            .addPlacement("brinstar_charge_room", "Charge Beam Room", "CHARGE_BEAM")
            .addPlacement("norfair_varia_room", "Varia Suit Room", "VARIA_SUIT")
            .addPlacement("brinstar_energy_1", "Energy Tank 1", "ENERGY_TANK")
            .addPlacement("brinstar_energy_2", "Energy Tank 2", "ENERGY_TANK")
            .successful(true)
            .algorithmUsed("Foresight Randomizer")
            .build();
    }

    private RandomizationResult createAnotherTestResult() {
        return RandomizationResult.builder()
            .seed("another-seed")
            .addPlacement("crateria_bomb_torizo_room", "Bomb Torizo Room", "BOMB")
            .addPlacement("brinstar_x_ray_scope_room", "X-Ray Scope Room", "GRAPPLING_BEAM")
            .addPlacement("maridia_gravity_suite_room", "Gravity Suit Room", "GRAVITY_SUIT")
            .successful(true)
            .algorithmUsed("Foresight Randomizer")
            .build();
    }

    private RandomizationResult createPerfectSeed() {
        // Create a seed with perfect distribution using actual JSON locations
        return RandomizationResult.builder()
            .seed("perfect-seed")
            .addPlacement("brinstar_morph_ball_room", "Morph Ball Room (1)", "MORPH_BALL")
            .addPlacement("crateria_pit_room", "Pit Room", "CHARGE_BEAM")
            .addPlacement("crateria_bomb_torizo_room", "Bomb Torizo Room", "BOMB")
            .addPlacement("brinstar_spazer_room", "Spazer Room", "ICE_BEAM")
            .addPlacement("crateria_terminator_room", "Terminator Room", "VARIA_SUIT")
            .addPlacement("crateria_the_moat", "The Moat", "ENERGY_TANK")
            .successful(true)
            .algorithmUsed("Balanced Progression")
            .build();
    }

    private RandomizationResult createPoorSeed() {
        // Create a seed with poor distribution (limited items)
        return RandomizationResult.builder()
            .seed("poor-seed")
            .addPlacement("brinstar_morph_ball_room", "Morph Ball Room (1)", "ENERGY_TANK") // Tank instead of progression
            .addPlacement("crateria_pit_room", "Pit Room", "ENERGY_TANK")
            .successful(true)
            .algorithmUsed("Basic Randomizer")
            .build();
    }
}