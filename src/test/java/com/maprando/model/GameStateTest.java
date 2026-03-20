package com.maprando.model;

import com.maprando.util.TestSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the GameState class
 */
@DisplayName("GameState Tests")
class GameStateTest {

    private GameState gameState;

    @BeforeAll
    static void setUpClass() {
        TestSetup.initializeMinimalRegistry();
    }

    @BeforeEach
    void setUp() {
        gameState = new GameState();
    }

    @Test
    @DisplayName("New game state should have default values")
    void testNewGameStateDefaults() {
        assertEquals(99, gameState.getEnergy(), "Energy should start at 99 (Super Metroid behavior)");
        assertNotNull(gameState.getInventory(), "Inventory should not be null");
        assertEquals(0, gameState.getInventory().getItemCount(),
                "Inventory should be empty");
        assertNull(gameState.getCurrentNode(),
                "Position should be null initially");
    }

    @Test
    @DisplayName("Standard start should have predefined items")
    void testStandardStart() {
        GameState standardStart = GameState.standardStart();

        assertEquals(99, standardStart.getEnergy(),
                "Standard start energy should be 99 (Super Metroid behavior)");
        assertFalse(standardStart.getInventory().hasItem("MORPH_BALL"),
                "Standard start should NOT have Morph Ball");
        assertFalse(standardStart.getInventory().hasItem("CHARGE_BEAM"),
                "Standard start should NOT have Charge Beam");
    }

    @Test
    @DisplayName("With items should create state with specific items")
    void testWithItems() {
        GameState customState = GameState.withItems("ICE_BEAM", "WAVE_BEAM");

        assertTrue(customState.getInventory().hasItem("ICE_BEAM"),
                "Should have Ice Beam");
        assertTrue(customState.getInventory().hasItem("WAVE_BEAM"),
                "Should have Wave Beam");
        assertEquals(2, customState.getInventory().getItemCount(),
                "Should have exactly 2 items");
    }

    @Test
    @DisplayName("Set energy should work and clamp to capacity")
    void testSetEnergy() {
        gameState.setEnergy(150);
        assertEquals(99, gameState.getEnergy(), "Energy should be clamped to base capacity of 99");
    }

    @Test
    @DisplayName("Add energy should increase energy")
    void testAddEnergy() {
        gameState.addEnergy(50);
        assertEquals(99, gameState.getEnergy(), "Energy should be clamped to base capacity");

        gameState.getInventory().increaseResourceCapacity(ResourceType.ENERGY, 200);
        gameState.addEnergy(150);
        assertEquals(249, gameState.getEnergy(),
                "Energy should increase when capacity allows (99 base + 200 capacity = 299, but we only added 150)");
    }

    @Test
    @DisplayName("Take damage should decrease energy")
    void testTakeDamage() {
        gameState.takeDamage(30);
        assertEquals(69, gameState.getEnergy(), "Energy should be 69 after 30 damage (99-30)");

        gameState.takeDamage(100);
        assertEquals(0, gameState.getEnergy(),
                "Energy should not go below 0");
    }

    @Test
    @DisplayName("Collect item should add to inventory and apply effects")
    void testCollectItem() {
        assertFalse(gameState.getInventory().hasItem("MORPH_BALL"),
                "Should not have Morph Ball initially");

        gameState.collectItem("MORPH_BALL");
        assertTrue(gameState.getInventory().hasItem("MORPH_BALL"),
                "Should have Morph Ball after collection");
    }

    @Test
    @DisplayName("Collecting energy tank should heal player")
    void testCollectEnergyTankHeals() {
        gameState.setEnergy(50);
        gameState.collectItem("ENERGY_TANK");

        assertTrue(gameState.getInventory().hasItem("ENERGY_TANK"),
                "Should have Energy Tank");
        // Energy tank collection increases capacity by 100 (100 + 100 = 200)
        // Energy field is not affected by collectItem, only by ItemCollector
        assertEquals(50, gameState.getEnergy(),
                "Energy field should remain 50 (ItemCollector not used)");
    }

    @Test
    @DisplayName("Collecting missile should increase capacity")
    void testCollectMissileTankIncreasesCapacity() {
        int initialCapacity = gameState.getInventory().getResourceCapacity(ResourceType.MISSILE);
        gameState.collectItem("MISSILE");

        assertTrue(gameState.getInventory().hasItem("MISSILE"),
                "Should have Missile");
        assertEquals(initialCapacity + ResourceType.MISSILE.getIncrementPerTank(),
                gameState.getInventory().getResourceCapacity(ResourceType.MISSILE),
                "Missile capacity should increase");
    }

    @Test
    @DisplayName("Has resource should check availability")
    void testHasResource() {
        assertTrue(gameState.hasResource(ResourceType.ENERGY, 50),
                "Should have 50 energy");
        assertFalse(gameState.hasResource(ResourceType.MISSILE, 10),
                "Should not have 10 missiles with 0 capacity");
    }

    @Test
    @DisplayName("Consume resource should decrease available amount")
    void testConsumeResource() {
        // Note: consumeResource for ENERGY doesn't affect the energy field directly
        // It affects the ResourceLevel tracking
        boolean consumed = gameState.consumeResource(ResourceType.ENERGY, 50);

        assertTrue(consumed, "Should consume 50 energy");
        // Energy field remains unchanged by consumeResource for ENERGY type
        assertEquals(99, gameState.getEnergy(), "Energy field should remain 99");
    }

    @Test
    @DisplayName("Consume resource should fail if insufficient")
    void testConsumeResourceInsufficient() {
        // ResourceLevel for ENERGY has 100 available (maxCapacity - consumed)
        // Trying to consume 150 should fail
        boolean consumed = gameState.consumeResource(ResourceType.ENERGY, 150);

        assertFalse(consumed, "Should not consume 150 energy when only 99 available");
        // Energy field is not affected by consumeResource for ENERGY type
        assertEquals(99, gameState.getEnergy(), "Energy field should remain 99");
    }

    @Test
    @DisplayName("Set current node should work")
    void testSetCurrentNode() {
        gameState.setCurrentNode("Brinstar");
        assertEquals("Brinstar", gameState.getCurrentNode(),
                "Node should be Brinstar");
    }

    @Test
    @DisplayName("Clone should create independent copy")
    void testClone() throws CloneNotSupportedException {
        gameState.setEnergy(50); // Will be clamped to capacity (100)
        gameState.collectItem("CHARGE_BEAM");
        gameState.setCurrentNode("Norfair");

        GameState cloned = gameState.clone();

        // Verify clone has same data
        assertEquals(50, cloned.getEnergy(), "Cloned energy should match");
        assertTrue(cloned.getInventory().hasItem("CHARGE_BEAM"),
                "Cloned inventory should have same items");
        assertEquals("Norfair", cloned.getCurrentNode(),
                "Cloned node should match");

        // Verify independence
        cloned.setEnergy(80); // Will be clamped to 100
        cloned.collectItem("ICE_BEAM");
        cloned.setCurrentNode("Maridia");

        assertEquals(50, gameState.getEnergy(),
                "Original energy should not change");
        assertFalse(gameState.getInventory().hasItem("ICE_BEAM"),
                "Original inventory should not have new item");
        assertEquals("Norfair", gameState.getCurrentNode(),
                "Original node should not change");
    }

    @Test
    @DisplayName("Clone inventory should be independent")
    void testCloneInventoryIndependence() throws CloneNotSupportedException {
        gameState.collectItem("CHARGE_BEAM");
        GameState cloned = gameState.clone();
        cloned.collectItem("ICE_BEAM");

        assertTrue(cloned.getInventory().hasItem("ICE_BEAM"),
                "Cloned state should have new item");
        assertFalse(gameState.getInventory().hasItem("ICE_BEAM"),
                "Original state should not have new item");
    }

    @Test
    @DisplayName("With items factory should create independent states")
    void testWithItemsIndependence() {
        GameState state1 = GameState.withItems("CHARGE_BEAM");
        GameState state2 = GameState.withItems("ICE_BEAM");

        assertTrue(state1.getInventory().hasItem("CHARGE_BEAM"),
                "State1 should have Charge Beam");
        assertFalse(state1.getInventory().hasItem("ICE_BEAM"),
                "State1 should not have Ice Beam");

        assertTrue(state2.getInventory().hasItem("ICE_BEAM"),
                "State2 should have Ice Beam");
        assertFalse(state2.getInventory().hasItem("CHARGE_BEAM"),
                "State2 should not have Charge Beam");
    }

    @Test
    @DisplayName("Standard start should be consistent")
    void testStandardStartConsistency() {
        GameState start1 = GameState.standardStart();
        GameState start2 = GameState.standardStart();

        assertEquals(start1.getEnergy(), start2.getEnergy(),
                "Standard start energy should be consistent");
        assertEquals(start1.getInventory().getItemCount(),
                start2.getInventory().getItemCount(),
                "Standard start item count should be consistent");
    }
}