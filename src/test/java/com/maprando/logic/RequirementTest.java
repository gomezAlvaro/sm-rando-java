package com.maprando.logic;

import com.maprando.model.GameState;
import com.maprando.model.Inventory;
import com.maprando.model.ItemDefinition;
import com.maprando.model.ItemRegistry;
import com.maprando.model.TechDefinition;
import com.maprando.model.TechRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Requirement system matching Rust MapRandomizer architecture.
 */
@DisplayName("Requirement System Tests")
class RequirementTest {
    private Inventory inventory;
    private ItemRegistry itemRegistry;
    private TechRegistry techRegistry;
    private RequirementContext context;

    @BeforeEach
    void setUp() {
        // Create minimal item registry
        itemRegistry = new ItemRegistry();
        itemRegistry.registerItem(new ItemDefinition("MORPH_BALL", "Morph Ball", "Morph capability", "major", true, 0, null, null, null, null, null, null, null));
        itemRegistry.registerItem(new ItemDefinition("BOMBS", "Bombs", "Bomb capability", "major", true, 1, null, null, null, null, null, null, null));
        itemRegistry.registerItem(new ItemDefinition("POWER_BOMB", "Power Bomb", "Power bomb capability", "major", true, 2, null, null, null, null, null, null, null));
        itemRegistry.registerItem(new ItemDefinition("VARIA_SUIT", "Varia Suit", "Heat protection", "major", true, 3, null, null, null, null, null, null, null));
        itemRegistry.registerItem(new ItemDefinition("GRAVITY_SUIT", "Gravity Suit", "Lava protection", "major", true, 4, null, null, null, null, null, null, null));
        itemRegistry.registerItem(new ItemDefinition("GRAPPLE_BEAM", "Grapple Beam", "Grapple capability", "major", true, 5, null, null, null, null, null, null, null));
        itemRegistry.registerItem(new ItemDefinition("ICE_BEAM", "Ice Beam", "Ice capability", "major", true, 6, null, null, null, null, null, null, null));
        itemRegistry.registerItem(new ItemDefinition("SPEED_BOOSTER", "Speed Booster", "Speed capability", "major", true, 7, null, null, null, null, null, null, null));

        // Create minimal tech registry
        techRegistry = new TechRegistry();
        techRegistry.registerTech(new TechDefinition(1, "canHeatRun", "Basic", 899));
        techRegistry.registerTech(new TechDefinition(2, "canWalljump", "Basic", null));

        inventory = new Inventory(itemRegistry, techRegistry);
        context = RequirementContext.fromInventory(inventory);
    }

    @Test
    @DisplayName("Free requirement should always be satisfied")
    void testFreeRequirement() {
        Requirement req = Requirement.free();
        assertTrue(req.isSatisfied(context), "Free requirement should always be satisfied");
    }

    @Test
    @DisplayName("Never requirement should never be satisfied")
    void testNeverRequirement() {
        Requirement req = Requirement.never();
        assertFalse(req.isSatisfied(context), "Never requirement should never be satisfied");
    }

    @Test
    @DisplayName("Item requirement should check for item in inventory")
    void testItemRequirement() {
        Requirement req = Requirement.item("MORPH_BALL");

        assertFalse(req.isSatisfied(context), "Should not have morph ball initially");
        inventory.addItem("MORPH_BALL");
        assertTrue(req.isSatisfied(context), "Should have morph ball after adding");
    }

    @Test
    @DisplayName("Tech requirement should check for tech in inventory")
    void testTechRequirement() {
        Requirement req = Requirement.tech("canHeatRun");

        assertFalse(req.isSatisfied(context), "Should not have canHeatRun initially");
        inventory.enableTech("canHeatRun");
        assertTrue(req.isSatisfied(context), "Should have canHeatRun after enabling");
    }

    @Test
    @DisplayName("And requirement should require all sub-requirements")
    void testAndRequirement() {
        inventory.addItem("MORPH_BALL");
        inventory.addItem("BOMBS");

        Requirement req = Requirement.and(
            Requirement.item("MORPH_BALL"),
            Requirement.item("BOMBS"),
            Requirement.item("POWER_BOMB")
        );

        assertFalse(req.isSatisfied(context), "Should not be satisfied with only morph and bombs");

        inventory.addItem("POWER_BOMB");
        assertTrue(req.isSatisfied(context), "Should be satisfied with all items");
    }

    @Test
    @DisplayName("Or requirement should require at least one sub-requirement")
    void testOrRequirement() {
        Requirement req = Requirement.or(
            Requirement.item("VARIA_SUIT"),
            Requirement.item("GRAVITY_SUIT")
        );

        assertFalse(req.isSatisfied(context), "Should not be satisfied without suits");

        inventory.addItem("VARIA_SUIT");
        assertTrue(req.isSatisfied(context), "Should be satisfied with Varia suit");

        inventory.addItem("GRAVITY_SUIT");
        assertTrue(req.isSatisfied(context), "Should still be satisfied with both suits");
    }

    @Test
    @DisplayName("And requirement should simplify - Never short-circuits")
    void testAndRequirementSimplification() {
        Requirement req = Requirement.and(
            Requirement.free(),
            Requirement.never(),
            Requirement.item("MORPH_BALL")
        );

        assertFalse(req.isSatisfied(context), "Never should make AND fail");
    }

    @Test
    @DisplayName("And requirement should simplify - Free is ignored")
    void testAndRequirementSimplifiesFree() {
        inventory.addItem("MORPH_BALL");

        Requirement req = Requirement.and(
            Requirement.free(),
            Requirement.item("MORPH_BALL")
        );

        assertTrue(req.isSatisfied(context), "Free should be ignored in AND");
    }

    @Test
    @DisplayName("Or requirement should simplify - Free short-circuits")
    void testOrRequirementSimplification() {
        Requirement req = Requirement.or(
            Requirement.free(),
            Requirement.never(),
            Requirement.item("MORPH_BALL")
        );

        assertTrue(req.isSatisfied(context), "Free should make OR succeed");
    }

    @Test
    @DisplayName("Or requirement should simplify - Never is ignored")
    void testOrRequirementSimplifiesNever() {
        inventory.addItem("MORPH_BALL");

        Requirement req = Requirement.or(
            Requirement.never(),
            Requirement.item("MORPH_BALL")
        );

        assertTrue(req.isSatisfied(context), "Never should be ignored in OR");
    }

    @Test
    @DisplayName("Nested And requirements should be flattened")
    void testNestedAndFlattening() {
        inventory.addItem("MORPH_BALL");
        inventory.addItem("BOMBS");
        inventory.addItem("POWER_BOMB");

        Requirement innerAnd = Requirement.and(
            Requirement.item("MORPH_BALL"),
            Requirement.item("BOMBS")
        );

        Requirement req = Requirement.and(
            innerAnd,
            Requirement.item("POWER_BOMB")
        );

        assertTrue(req.isSatisfied(context), "Nested AND should be flattened and satisfied");
    }

    @Test
    @DisplayName("Nested Or requirements should be flattened")
    void testNestedOrFlattening() {
        inventory.addItem("VARIA_SUIT");

        Requirement innerOr = Requirement.or(
            Requirement.item("VARIA_SUIT"),
            Requirement.item("GRAVITY_SUIT")
        );

        Requirement req = Requirement.or(
            innerOr,
            Requirement.item("ICE_BEAM")
        );

        assertTrue(req.isSatisfied(context), "Nested OR should be flattened and satisfied");
    }

    @Test
    @DisplayName("Empty And should simplify to Free")
    void testEmptyAndSimplifies() {
        Requirement req = Requirement.and(
            Requirement.free(),
            Requirement.free()
        );

        assertTrue(req.isSatisfied(context), "Empty AND should simplify to Free");
        assertEquals(Requirement.free().toString(), req.toString(), "Should simplify to Free");
    }

    @Test
    @DisplayName("Empty Or should simplify to Never")
    void testEmptyOrSimplifies() {
        Requirement req = Requirement.or(
            Requirement.never(),
            Requirement.never()
        );

        assertFalse(req.isSatisfied(context), "Empty OR should simplify to Never");
        assertEquals(Requirement.never().toString(), req.toString(), "Should simplify to Never");
    }

    @Test
    @DisplayName("Requirements factory methods should work")
    void testRequirementsFactory() {
        assertTrue(Requirements.canMorph().isSatisfied(context) == false, "Can morph should require morph ball");

        inventory.addItem("MORPH_BALL");
        assertTrue(Requirements.canMorph().isSatisfied(context), "Can morph should be satisfied with morph ball");
    }

    @Test
    @DisplayName("Can place bombs should require morph and bombs")
    void testCanPlaceBombs() {
        inventory.addItem("MORPH_BALL");

        assertFalse(Requirements.canPlaceBombs().isSatisfied(context), "Should not be able to place bombs without bombs");

        inventory.addItem("BOMBS");
        assertTrue(Requirements.canPlaceBombs().isSatisfied(context), "Should be able to place bombs with morph and bombs");
    }

    @Test
    @DisplayName("Can survive heat should require Varia or Gravity suit")
    void testCanSurviveHeat() {
        assertFalse(Requirements.canSurviveHeat().isSatisfied(context), "Should not survive heat without suit");

        inventory.addItem("VARIA_SUIT");
        assertTrue(Requirements.canSurviveHeat().isSatisfied(context), "Should survive heat with Varia suit");
    }

    @Test
    @DisplayName("Can survive lava should require Gravity suit")
    void testCanSurviveLava() {
        assertFalse(Requirements.canSurviveLava().isSatisfied(context), "Should not survive lava without Gravity suit");

        inventory.addItem("GRAVITY_SUIT");
        assertTrue(Requirements.canSurviveLava().isSatisfied(context), "Should survive lava with Gravity suit");
    }

    @Test
    @DisplayName("Can use power bombs should require morph and at least 1 power bomb capacity")
    void testCanUsePowerBombs() {
        assertFalse(Requirements.canUsePowerBombs().isSatisfied(context), "Should not use power bombs without items");

        inventory.addItem("MORPH_BALL");
        assertFalse(Requirements.canUsePowerBombs().isSatisfied(context), "Should not use power bombs without capacity");

        // Increase capacity to have at least 1 power bomb
        inventory.increaseResourceCapacity(com.maprando.model.ResourceType.POWER_BOMB, 5);
        assertTrue(Requirements.canUsePowerBombs().isSatisfied(context), "Should use power bombs with morph and capacity");
    }

    @Test
    @DisplayName("Complex requirement: Yellow door with heat")
    void testYellowDoorWithHeat() {
        // Don't have morph ball or power bombs yet
        assertFalse(Requirements.yellowDoorWithHeat().isSatisfied(context), "Should not open door without morph or power bombs");

        inventory.addItem("MORPH_BALL");
        inventory.increaseResourceCapacity(com.maprando.model.ResourceType.POWER_BOMB, 5);
        assertTrue(Requirements.yellowDoorWithHeat().isSatisfied(context), "Should open door with morph, power bombs, and base energy");
    }

    @Test
    @DisplayName("Complex requirement: Green door")
    void testGreenDoor() {
        assertFalse(Requirements.greenDoor().isSatisfied(context), "Should not open green door without grapple or speed");

        inventory.addItem("GRAPPLE_BEAM");
        assertTrue(Requirements.greenDoor().isSatisfied(context), "Should open green door with grapple");
    }

    @Test
    @DisplayName("Complex requirement: Red door")
    void testRedDoor() {
        assertFalse(Requirements.redDoor().isSatisfied(context), "Should not open red door without beam and supers");

        inventory.addItem("ICE_BEAM");
        assertFalse(Requirements.redDoor().isSatisfied(context), "Should not open red door without supers");

        inventory.setResourceCapacity(com.maprando.model.ResourceType.SUPER_MISSILE, 5);
        assertTrue(Requirements.redDoor().isSatisfied(context), "Should open red door with beam and supers");
    }
}
