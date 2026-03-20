package com.maprando.logic;

import com.maprando.data.DataLoader;
import com.maprando.model.GameState;
import com.maprando.model.Inventory;
import com.maprando.model.ItemDefinition;
import com.maprando.model.TechRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DynamicRequirementChecker class.
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
        // CHARGE_BEAM has no requirements
        assertTrue(checker.checkRequirements("CHARGE_BEAM", gameState));
    }

    @Test
    void testCheckRequirementsWithTechRequirement() {
        // BOMBS requires "can_morph"
        // Initially should fail
        assertFalse(checker.checkRequirements("BOMBS", gameState));

        // Enable can_morph tech
        gameState.getInventory().enableTech("can_morph");

        // Now should pass
        assertTrue(checker.checkRequirements("BOMBS", gameState));
    }

    @Test
    void testCheckRequirementsWithItemAndTechRequirements() {
        // Item that requires both morph ball item and can_morph tech
        gameState.getInventory().addItem("MORPH_BALL");
        gameState.getInventory().enableTech("can_morph");

        assertTrue(checker.checkRequirements("BOMBS", gameState));
    }

    @Test
    void testGetEnabledTechsMorphBall() {
        var techs = checker.getEnabledTechs("MORPH_BALL");
        assertNotNull(techs);
        assertEquals(2, techs.size());
        assertTrue(techs.contains("can_morph"));
        assertTrue(techs.contains("can_fit_small_spaces"));
    }

    @Test
    void testGetEnabledTechsBombs() {
        var techs = checker.getEnabledTechs("BOMBS");
        assertNotNull(techs);
        assertEquals(2, techs.size());
        assertTrue(techs.contains("can_place_bombs"));
        assertTrue(techs.contains("can_bomb_weak_walls"));
    }

    @Test
    void testGetEnabledTechsSpeedBooster() {
        var techs = checker.getEnabledTechs("SPEED_BOOSTER");
        assertNotNull(techs);
        assertEquals(2, techs.size());
        assertTrue(techs.contains("can_speed_booster"));
        assertTrue(techs.contains("can_shinespark"));
    }

    @Test
    void testGetEnabledTechsGravitySuit() {
        var techs = checker.getEnabledTechs("GRAVITY_SUIT");
        assertNotNull(techs);
        assertEquals(2, techs.size());
        assertTrue(techs.contains("can_swim_lava"));
        assertTrue(techs.contains("can_move_underwater"));
    }

    @Test
    void testGetEnabledTechsNoEnables() {
        // CHARGE_BEAM has no enables
        var techs = checker.getEnabledTechs("CHARGE_BEAM");
        assertNull(techs);
    }

    @Test
    void testCanCollectItemNoRequirements() {
        assertTrue(checker.canCollectItem("CHARGE_BEAM", gameState));
    }

    @Test
    void testCanCollectItemWithRequirements() {
        // BOMBS requires can_morph
        assertFalse(checker.canCollectItem("BOMBS", gameState));

        gameState.getInventory().enableTech("can_morph");
        assertTrue(checker.canCollectItem("BOMBS", gameState));
    }

    @Test
    void testCanCollectItemAlreadyCollected() {
        gameState.getInventory().addItem("CHARGE_BEAM");
        assertFalse(checker.canCollectItem("CHARGE_BEAM", gameState));
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
        Inventory inv = gameState.getInventory();

        // Collecting MORPH_BALL should NOT auto-enable techs
        assertEquals(0, inv.getTechCount());
        inv.addItem("MORPH_BALL");

        assertEquals(0, inv.getTechCount()); // Still 0 - no auto-enable
    }

    @Test
    void testTechsAreManuallyEnabled() {
        // Techs must be manually enabled (e.g., from difficulty presets)
        Inventory inv = gameState.getInventory();

        assertEquals(0, inv.getTechCount());

        // Manually enable a tech
        inv.enableTech("canHeatRun");

        assertEquals(1, inv.getTechCount());
        assertTrue(inv.hasTech("canHeatRun"));
    }
}
