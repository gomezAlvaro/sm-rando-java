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
        // BOMB requires "can_morph"
        // Initially should fail
        assertFalse(checker.checkRequirements("BOMB", gameState));

        // Enable can_morph tech
        gameState.getInventory().enableTech("can_morph");

        // Now should pass
        assertTrue(checker.checkRequirements("BOMB", gameState));
    }

    @Test
    void testCheckRequirementsWithItemAndTechRequirements() {
        // Item that requires both morph ball item and can_morph tech
        gameState.getInventory().addItem("MORPH_BALL");
        gameState.getInventory().enableTech("can_morph");

        assertTrue(checker.checkRequirements("BOMB", gameState));
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
    void testGetEnabledTechsBomb() {
        var techs = checker.getEnabledTechs("BOMB");
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
        // BOMB requires can_morph
        assertFalse(checker.canCollectItem("BOMB", gameState));

        gameState.getInventory().enableTech("can_morph");
        assertTrue(checker.canCollectItem("BOMB", gameState));
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
        // can_shinespark requires can_speed_booster
        TechRegistry techRegistry = dataLoader.getTechRegistry();

        var shinesparkTech = techRegistry.getById("can_shinespark");
        assertNotNull(shinesparkTech);
        assertNotNull(shinesparkTech.getRequires());
        assertTrue(shinesparkTech.getRequires().contains("can_speed_booster"));
    }

    @Test
    void testItemEnablesTechsOnCollection() {
        Inventory inv = gameState.getInventory();

        // Collecting MORPH_BALL should enable can_morph and can_fit_small_spaces
        assertEquals(0, inv.getTechCount());
        inv.addItem("MORPH_BALL");

        assertEquals(2, inv.getTechCount());
        assertTrue(inv.hasTech("can_morph"));
        assertTrue(inv.hasTech("can_fit_small_spaces"));
    }

    @Test
    void testSpeedBoosterEnablesShinespark() {
        Inventory inv = gameState.getInventory();

        inv.addItem("SPEED_BOOSTER");

        assertEquals(2, inv.getTechCount());
        assertTrue(inv.hasTech("can_speed_booster"));
        assertTrue(inv.hasTech("can_shinespark"));
    }

    @Test
    void testGrappleBeamEnablesGrapple() {
        Inventory inv = gameState.getInventory();

        inv.addItem("GRAPPLE_BEAM");

        assertEquals(1, inv.getTechCount());
        assertTrue(inv.hasTech("can_grapple"));
    }

    @Test
    void testGravitySuitEnablesLavaSwimming() {
        Inventory inv = gameState.getInventory();

        inv.addItem("GRAVITY_SUIT");

        assertEquals(2, inv.getTechCount());
        assertTrue(inv.hasTech("can_swim_lava"));
        assertTrue(inv.hasTech("can_move_underwater"));
    }
}
