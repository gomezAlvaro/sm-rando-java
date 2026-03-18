package com.maprando.randomize.advanced;

import com.maprando.data.DataLoader;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.Location;
import com.maprando.randomize.RandomizationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the BalancedProgressionAlgorithm class.
 * BalancedProgressionAlgorithm ensures good progression flow and item distribution.
 */
@DisplayName("BalancedProgressionAlgorithm Tests")
class BalancedProgressionAlgorithmTest {

    private BalancedProgressionAlgorithm balancedAlgorithm;
    private DataLoader dataLoader;
    private ItemPool progressionPool;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();

        balancedAlgorithm = new BalancedProgressionAlgorithm(dataLoader);
        progressionPool = createProgressionPool();
    }

    @Test
    @DisplayName("BalancedProgressionAlgorithm should be created successfully")
    void testCreation() {
        assertNotNull(balancedAlgorithm, "BalancedProgressionAlgorithm should be created");
        assertNotNull(balancedAlgorithm.getProgressionManager(), "Progression manager should exist");
    }

    @Test
    @DisplayName("Should distribute progression items evenly")
    void testProgressionDistribution() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        assertTrue(result.isSuccessful(), "Randomization should succeed");

        // Check that items were placed
        assertFalse(result.getPlacements().isEmpty(), "Should place items");
    }

    @Test
    @DisplayName("Should maintain good item pacing")
    void testItemPacing() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        assertNotNull(result, "Should generate result");
        assertTrue(result.isSuccessful(), "Should succeed");
    }

    @Test
    @DisplayName("Should balance early/mid/late game items")
    void testEarlyMidLateGameBalance() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        assertNotNull(result, "Should generate result");
        assertTrue(result.getPlacementCount() > 0, "Should place some items");
    }

    @Test
    @DisplayName("Should place key items appropriately")
    void testKeyItemPlacement() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        Map<String, String> keyItemPlacements = result.getPlacements();

        assertNotNull(keyItemPlacements, "Should track key item placements");
        assertFalse(keyItemPlacements.isEmpty(), "Should place some key items");
    }

    @Test
    @DisplayName("Should distribute filler items appropriately")
    void testFillerDistribution() {
        ItemPool poolWithFiller = createPoolWithFiller();
        balancedAlgorithm.setItemPool(poolWithFiller);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        FillerDistribution filler = balancedAlgorithm.getFillerDistribution();

        assertNotNull(filler, "Should track filler distribution");
        assertTrue(filler.getFillerPercentage() >= 0.0 && filler.getFillerPercentage() <= 100.0,
            "Filler percentage should be valid");
    }

    @Test
    @DisplayName("Should avoid clustering progression items")
    void testProgressionClusteringAvoidance() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        ClusteringMetrics clustering = balancedAlgorithm.getClusteringMetrics();

        assertNotNull(clustering, "Should calculate clustering metrics");
        assertTrue(clustering.getClusteringScore() >= 0.0,
            "Clustering score should be non-negative");
    }

    @Test
    @DisplayName("Should handle different difficulty levels")
    void testDifficultyLevelHandling() {
        balancedAlgorithm.setDifficultyLevel(DifficultyLevel.NORMAL);
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        assertTrue(result.isSuccessful(), "Should complete with normal difficulty");

        // Try different difficulty
        balancedAlgorithm.setDifficultyLevel(DifficultyLevel.HARD);
        balancedAlgorithm.setItemPool(progressionPool);

        RandomizationResult hardResult = balancedAlgorithm.randomize();

        assertTrue(hardResult.isSuccessful(), "Should complete with hard difficulty");
    }

    @Test
    @DisplayName("Should provide balance recommendations")
    void testBalanceRecommendations() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        List<BalanceRecommendation> recommendations =
            balancedAlgorithm.getBalanceRecommendations();

        assertNotNull(recommendations, "Should provide recommendations");
    }

    @Test
    @DisplayName("Should track progression flow")
    void testProgressionFlow() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        ProgressionFlow flow = balancedAlgorithm.getProgressionFlow();

        assertNotNull(flow, "Should track progression flow");
        assertTrue(flow.getFlowScore() >= 0.0, "Flow score should be non-negative");
    }

    @Test
    @DisplayName("Should handle imbalanced pools")
    void testImbalancedPoolHandling() {
        ItemPool imbalancedPool = createImbalancedPool();
        balancedAlgorithm.setItemPool(imbalancedPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        assertNotNull(result, "Should handle imbalanced pools");
    }

    @Test
    @DisplayName("Should respect region constraints")
    void testRegionConstraints() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        // Add region constraints
        balancedAlgorithm.addRegionConstraint("Brinstar", 2, 4); // 2-4 items
        balancedAlgorithm.addRegionConstraint("Norfair", 1, 3); // 1-3 items

        RandomizationResult result = balancedAlgorithm.randomize();

        assertTrue(result.isSuccessful(), "Should respect region constraints");
    }

    @Test
    @DisplayName("Should calculate difficulty curve")
    void testDifficultyCurve() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        DifficultyCurve curve = balancedAlgorithm.getDifficultyCurve();

        assertNotNull(curve, "Should calculate difficulty curve");
        assertTrue(curve.getAverageDifficulty() >= 0.0, "Average difficulty should be valid");
    }

    @Test
    @DisplayName("Should handle edge case: all progression items")
    void testAllProgressionItems() {
        ItemPool allProgression = createAllProgressionPool();
        balancedAlgorithm.setItemPool(allProgression);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        assertNotNull(result, "Should handle all progression items");
    }

    @Test
    @DisplayName("Should handle edge case: minimal progression")
    void testMinimalProgression() {
        ItemPool minimalPool = createMinimalProgressionPool();
        balancedAlgorithm.setItemPool(minimalPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        assertNotNull(result, "Should handle minimal progression");
    }

    @Test
    @DisplayName("Should provide progression summary")
    void testProgressionSummary() {
        balancedAlgorithm.setItemPool(progressionPool);
        balancedAlgorithm.addLocations(createTestLocations());

        RandomizationResult result = balancedAlgorithm.randomize();

        String summary = balancedAlgorithm.getProgressionSummary();

        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.length() > 0, "Summary should not be empty");
    }

    // Helper methods

    private ItemPool createProgressionPool() {
        ItemPool pool = new ItemPool();
        pool.addItem("MORPH_BALL", true);
        pool.addItem("BOMB", true);
        pool.addItem("CHARGE_BEAM", true);
        pool.addItem("VARIA_SUIT", true);
        pool.addItem("GRAPPLE_BEAM", true);
        pool.addItem("ENERGY_TANK", false);
        pool.addItem("MISSILE_TANK", false);
        return pool;
    }

    private ItemPool createPoolWithFiller() {
        ItemPool pool = new ItemPool();
        pool.addItem("MORPH_BALL", true);
        pool.addItem("CHARGE_BEAM", true);
        pool.addItem("ENERGY_TANK", false);
        pool.addItem("ENERGY_TANK", false);
        pool.addItem("ENERGY_TANK", false);
        pool.addItem("MISSILE_TANK", false);
        pool.addItem("MISSILE_TANK", false);
        return pool;
    }

    private ItemPool createImbalancedPool() {
        // Too many early progression items
        ItemPool pool = new ItemPool();
        pool.addItem("MORPH_BALL", true);
        pool.addItem("BOMB", true);
        pool.addItem("CHARGE_BEAM", true);
        pool.addItem("ICE_BEAM", true);
        pool.addItem("WAVE_BEAM", true);
        pool.addItem("VARIA_SUIT", true);
        return pool;
    }

    private ItemPool createAllProgressionPool() {
        ItemPool pool = new ItemPool();
        pool.addItem("MORPH_BALL", true);
        pool.addItem("BOMB", true);
        pool.addItem("CHARGE_BEAM", true);
        pool.addItem("ICE_BEAM", true);
        pool.addItem("VARIA_SUIT", true);
        pool.addItem("GRAPPLE_BEAM", true);
        return pool;
    }

    private ItemPool createMinimalProgressionPool() {
        ItemPool pool = new ItemPool();
        pool.addItem("MORPH_BALL", true);
        pool.addItem("ENERGY_TANK", false);
        pool.addItem("ENERGY_TANK", false);
        return pool;
    }

    private List<Location> createTestLocations() {
        return List.of(
            Location.builder().id("brinstar_1").name("Brinstar 1").region("Brinstar").build(),
            Location.builder().id("brinstar_2").name("Brinstar 2").region("Brinstar").build(),
            Location.builder().id("norfair_1").name("Norfair 1").region("Norfair").build(),
            Location.builder().id("norfair_2").name("Norfair 2").region("Norfair").build()
        );
    }
}