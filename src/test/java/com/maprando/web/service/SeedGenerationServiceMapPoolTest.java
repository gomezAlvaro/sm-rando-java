package com.maprando.web.service;

import com.maprando.data.DataLoader;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.Location;
import com.maprando.randomize.RandomizationResult;
import com.maprando.randomize.advanced.ForesightRandomizer;
import com.maprando.web.dto.SeedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test map pool selection in seed generation.
 */
public class SeedGenerationServiceMapPoolTest {

    private DataLoader dataLoader;

    @BeforeEach
    public void setUp() throws Exception {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();
    }

    @Test
    public void testMapPoolParameter_PassedToRandomizer() {
        // Create a ForesightRandomizer with custom map pool
        ForesightRandomizer randomizer = new ForesightRandomizer("test-seed", dataLoader);
        randomizer.setMapPool("small");

        // Configure with standard item pool
        ItemPool itemPool = ItemPool.createStandardPool();
        randomizer.setItemPool(itemPool);

        // Add locations
        List<Location> locations = createTestLocations();
        for (Location location : locations) {
            randomizer.addLocation(location);
        }

        // Generate seed - should use "small" map pool
        RandomizationResult result = randomizer.randomize();

        // Verify result is successful
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccessful(), "Seed generation should be successful");
        assertNotNull(result.getMapData(), "Map data should be generated");
    }

    @Test
    public void testMapPoolParameter_StandardPool() {
        // Create a ForesightRandomizer with standard map pool
        ForesightRandomizer randomizer = new ForesightRandomizer("test-seed-2", dataLoader);
        randomizer.setMapPool("standard");

        // Configure with standard item pool
        ItemPool itemPool = ItemPool.createStandardPool();
        randomizer.setItemPool(itemPool);

        // Add locations
        List<Location> locations = createTestLocations();
        for (Location location : locations) {
            randomizer.addLocation(location);
        }

        // Generate seed - should use "standard" map pool
        RandomizationResult result = randomizer.randomize();

        // Verify result is successful
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccessful(), "Seed generation should be successful");
        assertNotNull(result.getMapData(), "Map data should be generated");
    }

    @Test
    public void testMapPoolParameter_WildPool() {
        // Create a ForesightRandomizer with wild map pool
        ForesightRandomizer randomizer = new ForesightRandomizer("test-seed-3", dataLoader);
        randomizer.setMapPool("wild");

        // Configure with standard item pool
        ItemPool itemPool = ItemPool.createStandardPool();
        randomizer.setItemPool(itemPool);

        // Add locations
        List<Location> locations = createTestLocations();
        for (Location location : locations) {
            randomizer.addLocation(location);
        }

        // Generate seed - should use "wild" map pool
        RandomizationResult result = randomizer.randomize();

        // Verify result is successful
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccessful(), "Seed generation should be successful");
        assertNotNull(result.getMapData(), "Map data should be generated");
    }

    @Test
    public void testSeedRequest_MapPoolDefaultsToStandard() {
        // Create a SeedRequest without map pool
        SeedRequest request = new SeedRequest(
            null,  // seed
            "foresight",
            null,  // skill preset (will default to "Hard")
            null,  // enable spoiler
            null,  // quality validation
            null,  // randomize doors
            null   // map pool (should default to "standard")
        );

        // Verify defaults
        assertEquals("standard", request.getEffectiveMapPool(),
            "Map pool should default to 'standard'");
    }

    @Test
    public void testSeedRequest_MapPoolExplicitValue() {
        // Create a SeedRequest with explicit map pool
        SeedRequest request = new SeedRequest(
            null,  // seed
            "foresight",
            null,  // skill preset (will default to "Hard")
            null,  // enable spoiler
            null,  // quality validation
            null,  // randomize doors
            "small"  // explicit map pool
        );

        // Verify the explicit value is used
        assertEquals("small", request.getEffectiveMapPool(),
            "Map pool should use the explicit value 'small'");
    }

    /**
     * Create a small set of test locations for testing.
     */
    private List<Location> createTestLocations() {
        List<Location> locations = new ArrayList<>();

        // Add a few test locations
        locations.add(Location.builder()
            .id("loc_001")
            .name("Test Location 1")
            .region("Crateria")
            .build());

        locations.add(Location.builder()
            .id("loc_002")
            .name("Test Location 2")
            .region("Brinstar")
            .build());

        return locations;
    }
}
