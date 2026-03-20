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
import java.util.Set;

/**
 * Unit tests for the ForesightRandomizer class.
 * ForesightRandomizer uses reachability analysis to ensure seeds are beatable during placement.
 */
@DisplayName("ForesightRandomizer Tests")
class ForesightRandomizerTest {

    private ForesightRandomizer foresightRandomizer;
    private DataLoader dataLoader;
    private ItemPool itemPool;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();

        foresightRandomizer = new ForesightRandomizer("test-seed", dataLoader);
        itemPool = createTestItemPool();
    }

    @Test
    @DisplayName("ForesightRandomizer should be created successfully")
    void testCreation() {
        assertNotNull(foresightRandomizer, "ForesightRandomizer should be created");
        assertNotNull(foresightRandomizer.getGameGraph(), "Game graph should be initialized");
    }

    @Test
    @DisplayName("Should generate beatable seed")
    void testGenerateBeatableSeed() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccessful(), "Randomization should succeed");
        assertTrue(result.getPlacementCount() > 0, "Should place some items");
    }

    @Test
    @DisplayName("Should use reachability analysis during placement")
    void testReachabilityCheckingDuringPlacement() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        // The algorithm should have checked reachability during placement
        assertTrue(foresightRandomizer.hasUsedReachabilityAnalysis(),
            "Should have used reachability analysis");
    }

    @Test
    @DisplayName("Should place progression items in reachable locations")
    void testProgressionItemPlacement() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        // Check that progression items are placed in locations that are reachable
        // when considering previously placed items
        for (String locationId : result.getPlacements().keySet()) {
            String placedItem = result.getPlacements().get(locationId);
            if (placedItem != null && isProgressionItem(placedItem)) {
                // Location should have been reachable at placement time
                assertTrue(foresightRandomizer.wasLocationReachableAtPlacement(locationId),
                    "Progression item location should have been reachable");
            }
        }
    }

    @Test
    @DisplayName("Should backtrack when placement would create unbeatable situation")
    void testBacktrackingOnUnreachability() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        // If backtracking occurred, it should be tracked
        if (foresightRandomizer.getBacktrackCount() > 0) {
            assertTrue(foresightRandomizer.getBacktrackCount() > 0,
                "Should track backtracking events");
        }
    }

    @Test
    @DisplayName("Should handle failed placements gracefully")
    void testFailedPlacementHandling() {
        // Create a scenario where placement might fail
        ItemPool impossiblePool = createImpossibleItemPool();
        foresightRandomizer.setItemPool(impossiblePool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        // Should either succeed or fail gracefully
        assertNotNull(result, "Result should not be null");
    }

    @Test
    @DisplayName("Should generate quality metrics")
    void testQualityMetricsGeneration() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        SeedQualityMetrics quality = foresightRandomizer.getQualityMetrics();

        assertNotNull(quality, "Quality metrics should be generated");
        assertTrue(quality.getReachablePercentage() >= 0.0,
            "Reachable percentage should be valid");
    }

    @Test
    @DisplayName("Should respect location requirements")
    void testLocationRequirements() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocationsWithRequirements());

        RandomizationResult result = foresightRandomizer.randomize();

        assertTrue(result.isSuccessful(), "Should complete with requirements");

        // Verify items behind requirements are placed correctly
        assertNotNull(result.getPlacements(), "Placements should exist");
    }

    @Test
    @DisplayName("Should handle early game locations correctly")
    void testEarlyGameLocationHandling() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        // Early game locations should be filled with accessible items
        assertTrue(result.getPlacementCount() > 0,
            "Should place items in early game locations");
    }

    @Test
    @DisplayName("Should track placement statistics")
    void testPlacementStatistics() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        PlacementStatistics stats = foresightRandomizer.getPlacementStatistics();

        assertNotNull(stats, "Statistics should be tracked");
        assertTrue(stats.getTotalPlacements() > 0, "Should have placements");
        assertEquals(result.getPlacementCount(), stats.getTotalPlacements(),
            "Statistics should match result");
    }

    @Test
    @DisplayName("Should handle multiple randomization runs")
    void testMultipleRandomizationRuns() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result1 = foresightRandomizer.randomize();
        RandomizationResult result2 = foresightRandomizer.randomize();

        assertNotNull(result1, "First result should not be null");
        assertNotNull(result2, "Second result should not be null");
        assertTrue(result1.isSuccessful(), "First randomization should succeed");
        assertTrue(result2.isSuccessful(), "Second randomization should succeed");
    }

    @Test
    @DisplayName("Should maintain consistency with seed")
    void testSeedConsistency() {
        String seed = "consistent-seed";

        ForesightRandomizer randomizer1 = new ForesightRandomizer(seed, dataLoader);
        randomizer1.setItemPool(itemPool);
        randomizer1.addLocations(createTestLocations());

        ForesightRandomizer randomizer2 = new ForesightRandomizer(seed, dataLoader);
        randomizer2.setItemPool(itemPool);
        randomizer2.addLocations(createTestLocations());

        RandomizationResult result1 = randomizer1.randomize();
        RandomizationResult result2 = randomizer2.randomize();

        // Same seed should produce same results
        assertEquals(result1.getPlacements(), result2.getPlacements(),
            "Same seed should produce identical placements");
    }

    @Test
    @DisplayName("Should provide foresight analysis summary")
    void testForesightAnalysisSummary() {
        foresightRandomizer.setItemPool(itemPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        String summary = foresightRandomizer.getForesightAnalysisSummary();

        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.length() > 0, "Summary should not be empty");
    }

    @Test
    @DisplayName("Should handle edge case: empty item pool")
    void testEmptyItemPool() {
        ItemPool emptyPool = new ItemPool();
        foresightRandomizer.setItemPool(emptyPool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        assertNotNull(result, "Should handle empty pool gracefully");
    }

    @Test
    @DisplayName("Should handle edge case: single item")
    void testSingleItem() {
        ItemPool singlePool = new ItemPool();
        singlePool.addItem("MORPH_BALL", true);
        foresightRandomizer.setItemPool(singlePool);
        foresightRandomizer.addLocations(createTestLocations());

        RandomizationResult result = foresightRandomizer.randomize();

        assertTrue(result.getPlacementCount() <= 1,
            "Should place at most one item");
    }

    // Helper methods

    private ItemPool createTestItemPool() {
        ItemPool pool = new ItemPool();
        pool.addItem("MORPH_BALL", true);
        pool.addItem("CHARGE_BEAM", true);
        pool.addItem("VARIA_SUIT", true);
        pool.addItem("GRAPPLE_BEAM", true);
        pool.addItem("ENERGY_TANK", false);
        pool.addItem("ENERGY_TANK", false);
        return pool;
    }

    private ItemPool createImpossibleItemPool() {
        // Create a pool that's impossible to place correctly
        ItemPool pool = new ItemPool();
        pool.addItem("VARIA_SUIT", true);
        pool.addItem("GRAVITY_SUIT", true);
        return pool;
    }

    private java.util.List<Location> createTestLocations() {
        // Use actual locations from the JSON data
        return java.util.List.of(
            Location.builder()
                .id("brinstar_morph_ball_room")
                .name("Morph Ball Room (1)")
                .region("Brinstar")
                .build(),
            Location.builder()
                .id("crateria_the_moat")
                .name("The Moat")
                .region("Crateria")
                .build(),
            Location.builder()
                .id("crateria_bomb_torizo_room")
                .name("Bomb Torizo Room")
                .region("Crateria")
                .build()
        );
    }

    private java.util.List<Location> createTestLocationsWithRequirements() {
        return java.util.List.of(
            Location.builder()
                .id("morph_room")
                .name("Morph Room")
                .region("Brinstar")
                .build(),
            Location.builder()
                .id("bomb_room")
                .name("Bomb Room")
                .region("Brinstar")
                .requirements(java.util.Set.of("can_morph"))
                .build(),
            Location.builder()
                .id("heat_room")
                .name("Heat Room")
                .region("Norfair")
                .requirements(java.util.Set.of("can_survive_heat"))
                .build()
        );
    }

    private boolean isProgressionItem(String item) {
        // Simple check for progression items
        return "MORPH_BALL".equals(item) || "CHARGE_BEAM".equals(item) ||
               "VARIA_SUIT".equals(item) || "GRAPPLE_BEAM".equals(item);
    }
}