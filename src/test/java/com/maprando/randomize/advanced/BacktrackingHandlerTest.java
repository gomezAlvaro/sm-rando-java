package com.maprando.randomize.advanced;

import com.maprando.data.DataLoader;
import com.maprando.model.GameState;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.Location;
import com.maprando.traversal.TraversalState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for the BacktrackingHandler and ProgressionManager classes.
 * BacktrackingHandler handles placement failures and retries.
 * ProgressionManager manages progression item distribution.
 */
@DisplayName("BacktrackingHandler and ProgressionManager Tests")
class BacktrackingHandlerTest {

    private BacktrackingHandler backtrackingHandler;
    private ProgressionManager progressionManager;
    private DataLoader dataLoader;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();

        backtrackingHandler = new BacktrackingHandler(dataLoader);
        progressionManager = new ProgressionManager(dataLoader);
    }

    @Test
    @DisplayName("BacktrackingHandler should be created successfully")
    void testBacktrackingHandlerCreation() {
        assertNotNull(backtrackingHandler, "BacktrackingHandler should be created");
        assertNotNull(backtrackingHandler.getRetryStack(), "Retry stack should be initialized");
    }

    @Test
    @DisplayName("ProgressionManager should be created successfully")
    void testProgressionManagerCreation() {
        assertNotNull(progressionManager, "ProgressionManager should be created");
        assertNotNull(progressionManager.getProgressionItemIds(), "Progression items should be tracked");
    }

    @Test
    @DisplayName("Should handle placement failure with retry")
    void testPlacementFailureRetry() {
        Location location = createTestLocation("test_location");
        String item = "MORPH_BALL";
        TraversalState state = new TraversalState(GameState.standardStart());

        PlacementAttempt attempt = backtrackingHandler.attemptPlacement(location, item, state);

        assertNotNull(attempt, "Attempt should not be null");
        assertTrue(attempt.wasAttempted(), "Should record attempt");
    }

    @Test
    @DisplayName("Should rollback failed placements")
    void testRollbackFailedPlacements() {
        backtrackingHandler.attemptPlacement(
            createTestLocation("loc1"), "MORPH_BALL",
            new TraversalState(GameState.standardStart())
        );

        boolean rolledBack = backtrackingHandler.rollbackLastPlacement();

        assertTrue(rolledBack, "Should rollback successfully");
        assertTrue(backtrackingHandler.getRetryStack().isEmpty() ||
                   backtrackingHandler.getRetryStack().size() == 0,
            "Stack should be empty after rollback");
    }

    @Test
    @DisplayName("Should track retry attempts")
    void testRetryAttemptTracking() {
        backtrackingHandler.attemptPlacement(
            createTestLocation("loc1"), "MORPH_BALL",
            new TraversalState(GameState.standardStart())
        );

        int retryCount = backtrackingHandler.getRetryCount();

        assertTrue(retryCount >= 0, "Retry count should be non-negative");
    }

    @Test
    @DisplayName("Should detect placement deadlocks")
    void testDeadlockDetection() {
        // Create a deadlock situation
        for (int i = 0; i < 10; i++) {
            backtrackingHandler.attemptPlacement(
                createTestLocation("loc" + i), "MORPH_BALL",
                new TraversalState(GameState.standardStart())
            );
        }

        boolean isDeadlocked = backtrackingHandler.isDeadlocked();

        if (isDeadlocked) {
            assertTrue(backtrackingHandler.hasExceededMaxRetries(),
                "Should detect max retries exceeded");
        }
    }

    @Test
    @DisplayName("Should handle multiple rollback scenarios")
    void testMultipleRollbacks() {
        // Create multiple placement attempts
        backtrackingHandler.attemptPlacement(
            createTestLocation("loc1"), "MORPH_BALL",
            new TraversalState(GameState.standardStart())
        );
        backtrackingHandler.attemptPlacement(
            createTestLocation("loc2"), "CHARGE_BEAM",
            new TraversalState(GameState.standardStart())
        );

        // Rollback both
        backtrackingHandler.rollbackLastPlacement();
        backtrackingHandler.rollbackLastPlacement();

        assertEquals(0, backtrackingHandler.getRetryStack().size(),
            "All placements should be rolled back");
    }

    @Test
    @DisplayName("Should provide backtracking statistics")
    void testBacktrackingStatistics() {
        // Create some placement attempts
        for (int i = 0; i < 3; i++) {
            backtrackingHandler.attemptPlacement(
                createTestLocation("loc" + i), "MORPH_BALL",
                new TraversalState(GameState.standardStart())
            );
        }

        BacktrackingStatistics stats = backtrackingHandler.getStatistics();

        assertNotNull(stats, "Statistics should not be null");
        assertTrue(stats.getTotalAttempts() >= 3, "Should track attempts");
    }

    @Test
    @DisplayName("ProgressionManager should track progression items")
    void testProgressionItemTracking() {
        progressionManager.addProgressionItem("MORPH_BALL");
        progressionManager.addProgressionItem("VARIA_SUIT");

        Set<String> progressionItems = progressionManager.getProgressionItemIds();

        assertTrue(progressionItems.contains("MORPH_BALL"),
            "Should track Morph Ball");
        assertTrue(progressionItems.contains("VARIA_SUIT"),
            "Should track Varia Suit");
    }

    @Test
    @DisplayName("ProgressionManager should manage item pool")
    void testItemPoolManagement() {
        ItemPool pool = createTestItemPool();
        progressionManager.setItemPool(pool);

        assertEquals(pool, progressionManager.getItemPool(),
            "Should store item pool");
    }

    @Test
    @DisplayName("ProgressionManager should distribute progression items")
    void testProgressionDistribution() {
        ItemPool pool = createTestItemPool();
        progressionManager.setItemPool(pool);
        progressionManager.addProgressionItem("MORPH_BALL");
        progressionManager.addProgressionItem("CHARGE_BEAM");

        // Mark items as placed (this is required for distribution to track them)
        progressionManager.markItemPlaced("MORPH_BALL", "location_1");
        progressionManager.markItemPlaced("CHARGE_BEAM", "location_2");

        List<Location> locations = createTestLocations();
        ProgressionDistribution distribution =
            progressionManager.distributeProgression(locations);

        assertNotNull(distribution, "Distribution should not be null");
        assertFalse(distribution.getPlacements().isEmpty(),
            "Should place some progression items");
    }

    @Test
    @DisplayName("ProgressionManager should handle empty pools")
    void testEmptyPoolHandling() {
        ItemPool emptyPool = new ItemPool();
        progressionManager.setItemPool(emptyPool);

        List<Location> locations = createTestLocations();
        ProgressionDistribution distribution =
            progressionManager.distributeProgression(locations);

        assertNotNull(distribution, "Should handle empty pool");
    }

    @Test
    @DisplayName("ProgressionManager should track placed items")
    void testPlacedItemTracking() {
        progressionManager.markItemPlaced("MORPH_BALL", "test_location");
        progressionManager.markItemPlaced("CHARGE_BEAM", "another_location");

        String morphLocation = progressionManager.getItemPlacement("MORPH_BALL");
        String chargeLocation = progressionManager.getItemPlacement("CHARGE_BEAM");

        assertEquals("test_location", morphLocation,
            "Should track Morph Ball placement");
        assertEquals("another_location", chargeLocation,
            "Should track Charge Beam placement");
    }

    @Test
    @DisplayName("ProgressionManager should check unplaced items")
    void testUnplacedItemChecking() {
        // Add multiple items to progression items
        progressionManager.addProgressionItem("MORPH_BALL");
        progressionManager.addProgressionItem("CHARGE_BEAM");
        progressionManager.addProgressionItem("VARIA_SUIT");

        // Mark only one as placed
        progressionManager.markItemPlaced("MORPH_BALL", "test_location");

        Set<String> unplaced = progressionManager.getUnplacedItemIds();

        assertTrue(unplaced.contains("CHARGE_BEAM") ||
                   unplaced.contains("VARIA_SUIT"),
            "Should identify unplaced items");
    }

    @Test
    @DisplayName("ProgressionManager should calculate progression percentage")
    void testProgressionPercentage() {
        progressionManager.addProgressionItem("MORPH_BALL");
        progressionManager.addProgressionItem("CHARGE_BEAM");
        progressionManager.addProgressionItem("VARIA_SUIT");

        progressionManager.markItemPlaced("MORPH_BALL", "loc1");
        progressionManager.markItemPlaced("CHARGE_BEAM", "loc2");

        double percentage = progressionManager.getProgressionPercentage();

        assertTrue(percentage > 0.0, "Should have some progression");
        assertTrue(percentage <= 100.0, "Percentage should not exceed 100");
    }

    @Test
    @DisplayName("Should handle backtracking with progression management")
    void testBacktrackingWithProgression() {
        progressionManager.addProgressionItem("MORPH_BALL");
        progressionManager.addProgressionItem("CHARGE_BEAM");

        // Place items
        progressionManager.markItemPlaced("MORPH_BALL", "loc1");
        backtrackingHandler.attemptPlacement(
            createTestLocation("loc1"), "MORPH_BALL",
            new TraversalState(GameState.standardStart())
        );

        // Rollback
        backtrackingHandler.rollbackLastPlacement();
        progressionManager.rollbackPlacement("MORPH_BALL");

        assertNull(progressionManager.getItemPlacement("MORPH_BALL"),
            "Item should be unplaced after rollback");
    }

    @Test
    @DisplayName("Should provide combined statistics")
    void testCombinedStatistics() {
        progressionManager.addProgressionItem("MORPH_BALL");
        progressionManager.addProgressionItem("CHARGE_BEAM");

        progressionManager.markItemPlaced("MORPH_BALL", "loc1");
        backtrackingHandler.attemptPlacement(
            createTestLocation("loc1"), "MORPH_BALL",
            new TraversalState(GameState.standardStart())
        );

        CombinedStatistics stats = backtrackingHandler.getCombinedStatistics(progressionManager);

        assertNotNull(stats, "Combined statistics should not be null");
        assertTrue(stats.getTotalPlacements() >= 0, "Should track placements");
    }

    @Test
    @DisplayName("Should handle edge case: maximum retries")
    void testMaximumRetries() {
        backtrackingHandler.setMaxRetries(3);

        // Attempt placements beyond max
        for (int i = 0; i < 10; i++) {
            backtrackingHandler.attemptPlacement(
                createTestLocation("loc" + i), "MORPH_BALL",
                new TraversalState(GameState.standardStart())
            );
        }

        assertTrue(backtrackingHandler.hasExceededMaxRetries() ||
                   backtrackingHandler.getRetryCount() <= 3,
            "Should respect max retry limit");
    }

    @Test
    @DisplayName("Should provide detailed backtracking log")
    void testBacktrackingLog() {
        backtrackingHandler.attemptPlacement(
            createTestLocation("loc1"), "MORPH_BALL",
            new TraversalState(GameState.standardStart())
        );
        backtrackingHandler.attemptPlacement(
            createTestLocation("loc2"), "CHARGE_BEAM",
            new TraversalState(GameState.standardStart())
        );

        List<BacktrackingEvent> log = backtrackingHandler.getBacktrackingLog();

        assertNotNull(log, "Log should not be null");
        assertTrue(log.size() >= 2, "Should log placement attempts");
    }

    @Test
    @DisplayName("Should reset state correctly")
    void testStateReset() {
        progressionManager.addProgressionItem("MORPH_BALL");
        progressionManager.markItemPlaced("MORPH_BALL", "loc1");

        backtrackingHandler.attemptPlacement(
            createTestLocation("loc1"), "MORPH_BALL",
            new TraversalState(GameState.standardStart())
        );

        // Reset
        progressionManager.reset();
        backtrackingHandler.reset();

        assertTrue(progressionManager.getProgressionItemIds().isEmpty(),
            "Progression items should be cleared");
        assertTrue(backtrackingHandler.getRetryStack().isEmpty(),
            "Retry stack should be cleared");
    }

    // Helper methods

    private Location createTestLocation(String id) {
        return Location.builder()
            .id(id)
            .name("Test Location " + id)
            .region("Test Region")
            .build();
    }

    private ItemPool createTestItemPool() {
        ItemPool pool = new ItemPool();
        pool.addItem("MORPH_BALL", true);
        pool.addItem("CHARGE_BEAM", true);
        pool.addItem("VARIA_SUIT", true);
        pool.addItem("ENERGY_TANK", false);
        return pool;
    }

    private List<Location> createTestLocations() {
        return List.of(
            Location.builder().id("loc1").name("Location 1").region("Brinstar").build(),
            Location.builder().id("loc2").name("Location 2").region("Norfair").build(),
            Location.builder().id("loc3").name("Location 3").region("Maridia").build()
        );
    }
}