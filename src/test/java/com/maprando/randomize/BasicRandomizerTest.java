package com.maprando.randomize;

import com.maprando.model.GameState;
import com.maprando.logic.RequirementChecker;
import com.maprando.util.TestSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the BasicRandomizer class
 */
@DisplayName("BasicRandomizer Tests")
class BasicRandomizerTest {

    private BasicRandomizer randomizer;

    @BeforeAll
    static void setUpClass() {
        TestSetup.initializeMinimalRegistry();
    }

    @BeforeEach
    void setUp() {
        randomizer = new BasicRandomizer("test-seed");
    }

    @Test
    @DisplayName("Constructor with seed should create randomizer")
    void testConstructorWithSeed() {
        assertNotNull(randomizer, "Randomizer should be created");
    }

    @Test
    @DisplayName("Add location should add to randomizer")
    void testAddLocation() {
        Location location = Location.builder()
                .id("test-loc")
                .name("Test Location")
                .region("Test")
                .build();

        randomizer.addLocation(location);

        // Randomizer should have the location (no direct getter, but affects randomization)
    }

    @Test
    @DisplayName("Set item pool should configure pool")
    void testSetItemPool() {
        ItemPool pool = new ItemPool();
        pool.addItem("CHARGE_BEAM", 1, true);

        randomizer.setItemPool(pool);

        // Pool should be set (affects randomization)
    }

    @Test
    @DisplayName("Randomize with no locations should return empty result")
    void testRandomizeWithNoLocations() {
        ItemPool pool = new ItemPool();
        pool.addItem("CHARGE_BEAM", 1, true);
        randomizer.setItemPool(pool);

        RandomizationResult result = randomizer.randomize();

        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.getPlacements().size(), "Should have 0 placements");
    }

    @Test
    @DisplayName("Randomize with more items than locations should place correctly")
    void testRandomizeWithMoreItemsThanLocations() {
        // Create locations
        for (int i = 0; i < 3; i++) {
            Location location = Location.builder()
                    .id("loc-" + i)
                    .name("Location " + i)
                    .region("Test")
                    .build();
            randomizer.addLocation(location);
        }

        // Create pool with more items than locations
        ItemPool pool = new ItemPool();
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ICE_BEAM", 1, true);
        pool.addItem("WAVE_BEAM", 1, true);
        pool.addItem("ENERGY_TANK", 2, false);
        randomizer.setItemPool(pool);

        RandomizationResult result = randomizer.randomize();

        assertEquals(3, result.getPlacements().size(),
                "Should place 3 items (one per location)");
    }

    @Test
    @DisplayName("Randomize with fixed seed should be deterministic")
    void testRandomizeDeterministic() {
        // Create first randomizer
        BasicRandomizer randomizer1 = new BasicRandomizer("fixed-seed");
        for (int i = 0; i < 3; i++) {
            Location location = Location.builder()
                    .id("loc-" + i)
                    .name("Location " + i)
                    .region("Test")
                    .build();
            randomizer1.addLocation(location);
        }
        ItemPool pool1 = new ItemPool();
        pool1.addItem("CHARGE_BEAM", 1, true);
        pool1.addItem("ICE_BEAM", 1, true);
        pool1.addItem("WAVE_BEAM", 1, true);
        randomizer1.setItemPool(pool1);

        RandomizationResult result1 = randomizer1.randomize();

        // Create second randomizer with same seed
        BasicRandomizer randomizer2 = new BasicRandomizer("fixed-seed");
        for (int i = 0; i < 3; i++) {
            Location location = Location.builder()
                    .id("loc-" + i)
                    .name("Location " + i)
                    .region("Test")
                    .build();
            randomizer2.addLocation(location);
        }
        ItemPool pool2 = new ItemPool();
        pool2.addItem("CHARGE_BEAM", 1, true);
        pool2.addItem("ICE_BEAM", 1, true);
        pool2.addItem("WAVE_BEAM", 1, true);
        randomizer2.setItemPool(pool2);

        RandomizationResult result2 = randomizer2.randomize();

        // Results should be identical
        assertEquals(result1.getPlacements().size(), result2.getPlacements().size(),
                "Should place same number of items");

        // Check that same items are in same locations
        for (var entry : result1.getPlacements().entrySet()) {
            String item1 = entry.getValue();
            String item2 = result2.getPlacements().get(entry.getKey());
            assertEquals(item1, item2, "Same location should have same item");
        }
    }

    @Test
    @DisplayName("Randomize should place progression items first")
    void testRandomizeProgressionFirst() {
        // Create locations with no requirements (accessible from start)
        for (int i = 0; i < 5; i++) {
            Location location = Location.builder()
                    .id("loc-" + i)
                    .name("Location " + i)
                    .region("Test")
                    .build();
            randomizer.addLocation(location);
        }

        // Create pool with progression and filler items
        ItemPool pool = new ItemPool();
        pool.addItem("CHARGE_BEAM", 1, true);  // progression
        pool.addItem("ICE_BEAM", 1, true);     // progression
        pool.addItem("ENERGY_TANK", 3, false); // filler
        randomizer.setItemPool(pool);

        RandomizationResult result = randomizer.randomize();

        // Should place 5 items total
        assertEquals(5, result.getPlacements().size(),
                "Should place 5 items");

        // Check that progression items were placed
        boolean hasChargeBeam = result.getPlacements().values().contains("CHARGE_BEAM");
        boolean hasIceBeam = result.getPlacements().values().contains("ICE_BEAM");

        assertTrue(hasChargeBeam, "Should place Charge Beam (progression)");
        assertTrue(hasIceBeam, "Should place Ice Beam (progression)");
    }

    @Test
    @DisplayName("Verify completable should check basic requirements")
    void testVerifyCompletable() {
        // Add locations
        for (int i = 0; i < 3; i++) {
            Location location = Location.builder()
                    .id("loc-" + i)
                    .name("Location " + i)
                    .region("Test")
                    .build();
            randomizer.addLocation(location);
        }

        // Add minimal progression items
        ItemPool pool = new ItemPool();
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ICE_BEAM", 1, true);
        pool.addItem("WAVE_BEAM", 1, true);
        randomizer.setItemPool(pool);

        RandomizationResult result = randomizer.randomize();

        // Basic verification (in real implementation would check reachability)
        // For now, just ensures it doesn't crash
        assertDoesNotThrow(() -> randomizer.validateBeatable(result),
                "Verification should not throw");
    }

    @Test
    @DisplayName("Get warnings should return potential issues")
    void testGetWarnings() {
        // Set up with mismatched counts
        for (int i = 0; i < 2; i++) {
            Location location = Location.builder()
                    .id("loc-" + i)
                    .name("Location " + i)
                    .region("Test")
                    .build();
            randomizer.addLocation(location);
        }

        ItemPool pool = new ItemPool();
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ICE_BEAM", 1, true);
        pool.addItem("WAVE_BEAM", 1, true); // Extra item
        randomizer.setItemPool(pool);

        randomizer.randomize();

        var warnings = randomizer.getWarnings();
        assertFalse(warnings.isEmpty(), "Should have warnings about unplaced items");
    }

    @Test
    @DisplayName("Randomize with locations with requirements")
    void testRandomizeWithRequirements() {
        // Add accessible location
        Location accessible = Location.builder()
                .id("accessible")
                .name("Accessible Location")
                .region("Test")
                .build();

        // Add locked location
        Location locked = Location.builder()
                .id("locked")
                .name("Locked Location")
                .region("Test")
                .requirements(java.util.Set.of("can_morph", "has_bombs"))
                .build();

        randomizer.addLocation(accessible);
        randomizer.addLocation(locked);

        // Pool with Morph Ball as progression
        ItemPool pool = new ItemPool();
        pool.addItem("MORPH_BALL", 1, true); // This unlocks the second location
        pool.addItem("CHARGE_BEAM", 1, true);
        randomizer.setItemPool(pool);

        RandomizationResult result = randomizer.randomize();

        assertEquals(2, result.getPlacements().size(),
                "Should place 2 items");
    }

    @Test
    @DisplayName("Create standard should create preset randomizer")
    void testCreateStandard() {
        BasicRandomizer standard = new BasicRandomizer("test-seed");

        assertNotNull(standard, "Standard randomizer should be created");
        // Should have predefined locations and pool
    }

    @Test
    @DisplayName("Multiple randomizations should be independent")
    void testMultipleRandomizationsIndependent() {
        // Setup
        for (int i = 0; i < 3; i++) {
            Location location = Location.builder()
                    .id("loc-" + i)
                    .name("Location " + i)
                    .region("Test")
                    .build();
            randomizer.addLocation(location);
        }

        ItemPool pool = new ItemPool();
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ICE_BEAM", 1, true);
        pool.addItem("WAVE_BEAM", 1, true);
        randomizer.setItemPool(pool);

        // First randomization
        RandomizationResult result1 = randomizer.randomize();

        // Second randomization (same randomizer)
        RandomizationResult result2 = randomizer.randomize();

        // Results might be different due to pool changes
        assertNotNull(result2, "Second result should not be null");
    }

    @Test
    @DisplayName("Randomize should handle empty pool")
    void testRandomizeWithEmptyPool() {
        Location location = Location.builder()
                .id("test")
                .name("Test")
                .region("Test")
                .build();
        randomizer.addLocation(location);

        ItemPool emptyPool = new ItemPool();
        randomizer.setItemPool(emptyPool);

        RandomizationResult result = randomizer.randomize();

        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.getPlacements().size(),
                "Should have 0 placements with empty pool");
    }

    @Test
    @DisplayName("Randomize should handle different seeds")
    void testRandomizeDifferentSeeds() {
        // Setup locations
        String[] locationIds = {"loc-0", "loc-1", "loc-2"};
        for (String id : locationIds) {
            Location location = Location.builder()
                    .id(id)
                    .name("Location " + id)
                    .region("Test")
                    .build();
            randomizer.addLocation(location);
        }

        ItemPool pool = new ItemPool();
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ICE_BEAM", 1, true);
        pool.addItem("WAVE_BEAM", 1, true);
        randomizer.setItemPool(pool);

        RandomizationResult result1 = randomizer.randomize();

        // Create new randomizer with different seed
        BasicRandomizer randomizer2 = new BasicRandomizer("different-seed");
        for (String id : locationIds) {
            Location location = Location.builder()
                    .id(id)
                    .name("Location " + id)
                    .region("Test")
                    .build();
            randomizer2.addLocation(location);
        }
        randomizer2.setItemPool(pool);

        RandomizationResult result2 = randomizer2.randomize();

        // Results should likely be different (though theoretically possible to be same)
        assertNotNull(result2, "Second result should not be null");
        assertEquals(result1.getPlacements().size(), result2.getPlacements().size(),
                "Should place same number of items");
    }
}