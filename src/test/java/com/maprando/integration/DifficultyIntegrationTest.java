package com.maprando.integration;

import com.maprando.data.DataLoader;
import com.maprando.data.model.DifficultyData;
import com.maprando.model.GameState;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.ItemPoolFactory;
import com.maprando.traversal.TraversalState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

/**
 * Integration tests for difficulty-based seed generation.
 * Verifies that difficulty presets properly affect item pools, starting items, and tech assumptions.
 */
@DisplayName("Difficulty Integration Tests")
class DifficultyIntegrationTest {

    private DataLoader dataLoader;
    private ItemPoolFactory itemPoolFactory;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();
        itemPoolFactory = new ItemPoolFactory(dataLoader);
    }

    @Test
    @DisplayName("Should load all difficulty presets")
    void testLoadDifficultyPresets() {
        assertNotNull(dataLoader.getDifficultyPreset("casual"), "Casual preset should exist");
        assertNotNull(dataLoader.getDifficultyPreset("normal"), "Normal preset should exist");
        assertNotNull(dataLoader.getDifficultyPreset("hard"), "Hard preset should exist");
        assertNotNull(dataLoader.getDifficultyPreset("expert"), "Expert preset should exist");
        assertNotNull(dataLoader.getDifficultyPreset("nightmare"), "Nightmare preset should exist");

        assertEquals(5, dataLoader.getAllDifficultyPresets().size(), "Should have 5 difficulty presets");
    }

    @Test
    @DisplayName("Casual difficulty should have easier settings")
    void testCasualDifficultySettings() {
        DifficultyData casual = dataLoader.getDifficultyPreset("casual");

        assertNotNull(casual, "Casual preset should exist");
        assertEquals("casual", casual.getId());
        assertEquals(1.0, casual.getItemPool().getProgressionRate(), 0.01, "Casual should have full progression rate");
        assertEquals(1.5, casual.getItemPool().getFillerItemRate(), 0.01, "Casual should have increased filler rate");
        assertEquals("beginner", casual.getTechAssumptions(), "Casual should use beginner tech");

        // Casual should give starting items
        assertFalse(casual.getStartingItems().isEmpty(), "Casual should have starting items");
        assertTrue(casual.getStartingItems().contains("MORPH_BALL"), "Casual should start with Morph Ball");
        assertTrue(casual.getStartingItems().contains("CHARGE_BEAM"), "Casual should start with Charge Beam");
    }

    @Test
    @DisplayName("Normal difficulty should have standard settings")
    void testNormalDifficultySettings() {
        DifficultyData normal = dataLoader.getDifficultyPreset("normal");

        assertNotNull(normal, "Normal preset should exist");
        assertEquals("normal", normal.getId());
        assertEquals(1.0, normal.getItemPool().getProgressionRate(), 0.01, "Normal should have full progression rate");
        assertEquals(1.0, normal.getItemPool().getFillerItemRate(), 0.01, "Normal should have standard filler rate");
        assertEquals("intermediate", normal.getTechAssumptions(), "Normal should use intermediate tech");

        // Normal should give minimal starting items
        assertEquals(2, normal.getStartingItems().size(), "Normal should start with 2 items");
        assertTrue(normal.getStartingItems().contains("MORPH_BALL"), "Normal should start with Morph Ball");
        assertTrue(normal.getStartingItems().contains("CHARGE_BEAM"), "Normal should start with Charge Beam");
    }

    @Test
    @DisplayName("Nightmare difficulty should have harsh settings")
    void testNightmareDifficultySettings() {
        DifficultyData nightmare = dataLoader.getDifficultyPreset("nightmare");

        assertNotNull(nightmare, "Nightmare preset should exist");
        assertEquals("nightmare", nightmare.getId());
        assertEquals(0.5, nightmare.getItemPool().getProgressionRate(), 0.01, "Nightmare should have reduced progression rate");
        assertEquals(0.3, nightmare.getItemPool().getFillerItemRate(), 0.01, "Nightmare should have severely reduced filler rate");
        assertEquals("nightmare", nightmare.getTechAssumptions(), "Nightmare should use nightmare tech");

        // Nightmare should have no starting items
        assertTrue(nightmare.getStartingItems().isEmpty(), "Nightmare should have no starting items");
    }

    @Test
    @DisplayName("ItemPoolFactory should create different pools for different difficulties")
    void testItemPoolFactoryCreatesDifferentPools() {
        ItemPool casualPool = itemPoolFactory.createPool("casual");
        ItemPool normalPool = itemPoolFactory.createPool("normal");
        ItemPool nightmarePool = itemPoolFactory.createPool("nightmare");

        // Casual should have more items than nightmare due to filler rate
        int casualFillerCount = casualPool.getFillerItems().stream()
            .mapToInt(casualPool::getItemCount)
            .sum();

        int nightmareFillerCount = nightmarePool.getFillerItems().stream()
            .mapToInt(nightmarePool::getItemCount)
            .sum();

        assertTrue(casualFillerCount >= nightmareFillerCount,
            "Casual should have at least as many filler items as nightmare");
    }

    @Test
    @DisplayName("GameState should apply starting items from difficulty")
    void testGameStateWithStartingItems() {
        DifficultyData casual = dataLoader.getDifficultyPreset("casual");

        GameState state = GameState.withStartingItems(
            dataLoader.getItemRegistry(),
            casual.getStartingItems()
        );

        assertTrue(state.getInventory().hasItem("MORPH_BALL"),
            "State should have Morph Ball from starting items");
        assertTrue(state.getInventory().hasItem("CHARGE_BEAM"),
            "State should have Charge Beam from starting items");

        // Check for multiple missile tanks
        long missileCount = casual.getStartingItems().stream()
            .filter(item -> item.equals("MISSILE_TANK"))
            .count();
        assertTrue(missileCount > 0, "Casual should start with missile tanks");
    }

    @Test
    @DisplayName("TraversalState should apply tech assumptions from difficulty")
    void testTraversalStateWithTechAssumptions() {
        TraversalState beginnerState = new TraversalState(GameState.standardStart());
        beginnerState.setDifficultyTechLevel("beginner");

        TraversalState nightmareState = new TraversalState(GameState.standardStart());
        nightmareState.setDifficultyTechLevel("nightmare");

        // Beginner should have no advanced tech
        assertFalse(beginnerState.hasTech("can_walljump"),
            "Beginner should not have walljump tech");
        assertFalse(beginnerState.hasTech("can_shinespark"),
            "Beginner should not have shinespark tech");

        // Nightmare should have all advanced tech
        assertTrue(nightmareState.hasTech("can_walljump"),
            "Nightmare should have walljump tech");
        assertTrue(nightmareState.hasTech("can_shinespark"),
            "Nightmare should have shinespark tech");
        assertTrue(nightmareState.hasTech("can_horizontal_shinespark"),
            "Nightmare should have horizontal shinespark tech");
        assertTrue(nightmareState.hasTech("can_suitless_lava_dive"),
            "Nightmare should have suitless lava dive tech");
    }

    @Test
    @DisplayName("TraversalState tech should affect requirement satisfaction")
    void testTechAffectsRequirementSatisfaction() {
        TraversalState normalState = new TraversalState(GameState.standardStart());
        normalState.setDifficultyTechLevel("intermediate");

        TraversalState expertState = new TraversalState(GameState.standardStart());
        expertState.setDifficultyTechLevel("expert");

        // Expert can satisfy advanced tech requirements that normal cannot
        assertFalse(normalState.canSatisfyRequirement("can_horizontal_shinespark"),
            "Normal state should not satisfy horizontal shinespark without tech");

        assertTrue(expertState.canSatisfyRequirement("can_horizontal_shinespark"),
            "Expert state should satisfy horizontal shinespark with tech");

        assertFalse(normalState.canSatisfyRequirement("can_suitless_lava_dive"),
            "Normal state should not satisfy suitless lava dive without tech");

        assertTrue(expertState.canSatisfyRequirement("can_suitless_lava_dive"),
            "Expert state should satisfy suitless lava dive with tech");
    }

    @Test
    @DisplayName("ItemPoolFactory with starting items should track both")
    void testItemPoolWithStartingItems() {
        ItemPoolFactory.ItemPoolWithStartingItems casual =
            itemPoolFactory.createPoolWithStartingItems("casual");

        assertNotNull(casual.getPool(), "Pool should not be null");
        assertNotNull(casual.getStartingItems(), "Starting items should not be null");
        assertFalse(casual.getStartingItems().isEmpty(), "Casual should have starting items");

        // Verify starting items list is populated
        assertTrue(casual.getStartingItems().contains("MORPH_BALL"),
            "Starting items should include Morph Ball");
        assertTrue(casual.getStartingItems().contains("CHARGE_BEAM"),
            "Starting items should include Charge Beam");

        // Note: Starting items remain in the pool for placement
        // The player starts with them, but they're also placed in the world
    }

    @Test
    @DisplayName("Difficulty settings should be consistent across presets")
    void testDifficultySettingsConsistency() {
        DifficultyData[] difficulties = {
            dataLoader.getDifficultyPreset("casual"),
            dataLoader.getDifficultyPreset("normal"),
            dataLoader.getDifficultyPreset("hard"),
            dataLoader.getDifficultyPreset("expert"),
            dataLoader.getDifficultyPreset("nightmare")
        };

        // Verify progression rate gets harder
        double prevProgressionRate = 1.1; // Start higher than casual
        for (DifficultyData difficulty : difficulties) {
            assertTrue(difficulty.getItemPool().getProgressionRate() <= prevProgressionRate,
                difficulty.getName() + " progression rate should be <= previous");
            prevProgressionRate = difficulty.getItemPool().getProgressionRate();
        }

        // Verify filler rate decreases
        double prevFillerRate = 1.6; // Start higher than casual
        for (DifficultyData difficulty : difficulties) {
            assertTrue(difficulty.getItemPool().getFillerItemRate() <= prevFillerRate,
                difficulty.getName() + " filler rate should be <= previous");
            prevFillerRate = difficulty.getItemPool().getFillerItemRate();
        }
    }

    @Test
    @DisplayName("Unknown difficulty should default to normal")
    void testUnknownDifficultyDefaultsToNormal() {
        ItemPool unknownPool = itemPoolFactory.createPool("unknown_difficulty");

        ItemPool normalPool = itemPoolFactory.createPool("normal");

        // Should be same as normal
        assertEquals(normalPool.getTotalItemCount(), unknownPool.getTotalItemCount(),
            "Unknown difficulty should default to normal pool");
    }

    @Test
    @DisplayName("Empty starting items list should be handled")
    void testEmptyStartingItems() {
        GameState state = GameState.withStartingItems(
            dataLoader.getItemRegistry(),
            java.util.Collections.emptyList()
        );

        // Should be same as standard start
        assertFalse(state.getInventory().hasItem("MORPH_BALL"),
            "Should not have Morph Ball without starting items");
    }

    @Test
    @DisplayName("Null starting items list should be handled")
    void testNullStartingItems() {
        GameState state = GameState.withStartingItems(
            dataLoader.getItemRegistry(),
            null
        );

        // Should be same as standard start
        assertFalse(state.getInventory().hasItem("MORPH_BALL"),
            "Should not have Morph Ball with null starting items");
    }
}
