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
 * Verifies that difficulty presets properly affect tech assumptions.
 *
 * Note: Starting items are a separate setting in the Rust project, not part of difficulty.
 * Item pool scaling is also not part of the difficulty system.
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
    }

    @Test
    @DisplayName("Normal difficulty should have standard settings")
    void testNormalDifficultySettings() {
        DifficultyData normal = dataLoader.getDifficultyPreset("normal");

        assertNotNull(normal, "Normal preset should exist");
        assertEquals("normal", normal.getId());
        assertEquals("intermediate", normal.getTechAssumptions(), "Normal should use intermediate tech");
    }

    @Test
    @DisplayName("Nightmare difficulty should have harsh settings")
    void testNightmareDifficultySettings() {
        DifficultyData nightmare = dataLoader.getDifficultyPreset("nightmare");

        assertNotNull(nightmare, "Nightmare preset should exist");
        assertEquals("nightmare", nightmare.getId());
        assertEquals("nightmare", nightmare.getTechAssumptions(), "Nightmare should use nightmare tech");
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
