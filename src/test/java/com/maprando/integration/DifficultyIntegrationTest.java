package com.maprando.integration;

import com.maprando.data.DataLoader;
import com.maprando.data.model.DifficultyData;
import com.maprando.model.GameState;
import com.maprando.traversal.TraversalState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

/**
 * Integration tests for difficulty system (aligned with Rust MapRandomizer).
 * Verifies that difficulty presets properly affect starting items and tech assumptions.
 * Item pool scaling removed as it's not in the original Rust project.
 */
@DisplayName("Difficulty Integration Tests (Rust-Aligned)")
class DifficultyIntegrationTest {

    private DataLoader dataLoader;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();
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
        assertEquals("nightmare", nightmare.getTechAssumptions(), "Nightmare should use nightmare tech");

        // Nightmare should have no starting items
        assertTrue(nightmare.getStartingItems().isEmpty(), "Nightmare should have no starting items");
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

    @Test
    @DisplayName("Starting items should increase by difficulty level")
    void testStartingItemsIncreaseByDifficulty() {
        DifficultyData casual = dataLoader.getDifficultyPreset("casual");
        DifficultyData normal = dataLoader.getDifficultyPreset("normal");
        DifficultyData hard = dataLoader.getDifficultyPreset("hard");
        DifficultyData expert = dataLoader.getDifficultyPreset("expert");
        DifficultyData nightmare = dataLoader.getDifficultyPreset("nightmare");

        // Starting items should decrease as difficulty increases
        assertTrue(casual.getStartingItems().size() >= normal.getStartingItems().size(),
            "Casual should have >= starting items than normal");
        assertTrue(normal.getStartingItems().size() >= hard.getStartingItems().size(),
            "Normal should have >= starting items than hard");
        assertTrue(hard.getStartingItems().size() >= expert.getStartingItems().size(),
            "Hard should have >= starting items than expert");
        assertTrue(expert.getStartingItems().size() >= nightmare.getStartingItems().size(),
            "Expert should have >= starting items than nightmare");

        // Nightmare should have no starting items
        assertEquals(0, nightmare.getStartingItems().size(), "Nightmare should have no starting items");
    }

    @Test
    @DisplayName("Tech assumptions should escalate by difficulty")
    void testTechAssumptionsEscalate() {
        DifficultyData casual = dataLoader.getDifficultyPreset("casual");
        DifficultyData normal = dataLoader.getDifficultyPreset("normal");
        DifficultyData hard = dataLoader.getDifficultyPreset("hard");
        DifficultyData expert = dataLoader.getDifficultyPreset("expert");
        DifficultyData nightmare = dataLoader.getDifficultyPreset("nightmare");

        // Verify tech assumptions escalate properly
        assertEquals("beginner", casual.getTechAssumptions(), "Casual should use beginner tech");
        assertEquals("intermediate", normal.getTechAssumptions(), "Normal should use intermediate tech");
        assertEquals("advanced", hard.getTechAssumptions(), "Hard should use advanced tech");
        assertEquals("expert", expert.getTechAssumptions(), "Expert should use expert tech");
        assertEquals("nightmare", nightmare.getTechAssumptions(), "Nightmare should use nightmare tech");
    }
}
