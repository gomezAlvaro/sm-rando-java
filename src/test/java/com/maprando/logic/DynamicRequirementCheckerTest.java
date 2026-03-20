package com.maprando.logic;

import com.maprando.data.DataLoader;
import com.maprando.model.GameState;
import com.maprando.model.TechRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DynamicRequirementChecker class.
 * Aligned with Rust MapRandomizer architecture.
 */
class DynamicRequirementCheckerTest {
    private DataLoader dataLoader;
    private GameState gameState;
    private DynamicRequirementChecker checker;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();
        gameState = new GameState();
        checker = new DynamicRequirementChecker(dataLoader);
    }

    @Test
    void testCheckRequirementsNoRequirements() {
        // CHARGE_BEAM has no requirements (items.json has empty requires array)
        assertTrue(checker.checkRequirements("CHARGE_BEAM", gameState));
    }

    @Test
    void testCheckRequirementsInvalidItem() {
        // Should handle invalid item IDs gracefully
        assertFalse(checker.checkRequirements("NONEXISTENT_ITEM", gameState));
    }

    @Test
    void testTechRequirementsFromTechDefinition() {
        // Tech definitions in Rust format don't have requirements
        // Requirements are defined in logic layer using Requirement interface
        TechRegistry techRegistry = dataLoader.getTechRegistry();

        var tech = techRegistry.getById("canHeatRun");
        assertNotNull(tech);
        assertEquals("canHeatRun", tech.getName());
        // Techs don't have getRequires() in Rust format
    }

    @Test
    void testItemsDoNotAutoEnableTechs() {
        // In Rust architecture, items don't auto-enable techs
        // Techs are enabled via difficulty presets, not item collection
        var inv = gameState.getInventory();

        // Collecting MORPH_BALL should NOT auto-enable techs
        assertEquals(0, inv.getTechCount());
        inv.addItem("MORPH_BALL");

        assertEquals(0, inv.getTechCount()); // Still 0 - no auto-enable
    }

    @Test
    void testTechsAreManuallyEnabled() {
        // Techs must be manually enabled (e.g., from difficulty presets)
        var inv = gameState.getInventory();

        assertEquals(0, inv.getTechCount());

        // Manually enable a tech
        inv.enableTech("canHeatRun");

        assertEquals(1, inv.getTechCount());
        assertTrue(inv.hasTech("canHeatRun"));
    }

    @Test
    void testGetAvailableTechsFromGameState() {
        // Get available techs should return enabled techs from inventory
        var techs = checker.getAvailableTechs(gameState);

        assertNotNull(techs);
        assertTrue(techs.isEmpty()); // No techs enabled by default

        // Enable a tech
        gameState.getInventory().enableTech("canWalljump");

        var techsAfter = checker.getAvailableTechs(gameState);
        assertTrue(techsAfter.contains("canWalljump"));
    }
}
