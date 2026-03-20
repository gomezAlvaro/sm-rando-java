package com.maprando.model;

import com.maprando.util.TestSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Inventory class
 */
@DisplayName("Inventory Tests")
class InventoryTest {

    private Inventory inventory;

    @BeforeAll
    static void setUpClass() {
        TestSetup.initializeMinimalRegistry();
    }

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }

    @Test
    @DisplayName("New inventory should be empty")
    void testNewInventoryIsEmpty() {
        assertEquals(0, inventory.getItemCount(), "New inventory should have 0 items");
        assertTrue(inventory.getCollectedItems().isEmpty(), "New inventory should have no collected items");
    }

    @Test
    @DisplayName("Adding item should increase count")
    void testAddItem() {
        inventory.addItem("CHARGE_BEAM");
        assertEquals(1, inventory.getItemCount(), "Should have 1 item after adding");
        assertTrue(inventory.hasItem("CHARGE_BEAM"), "Should have the added item");
    }

    @Test
    @DisplayName("Adding duplicate item should not increase count")
    void testAddDuplicateItem() {
        inventory.addItem("CHARGE_BEAM");
        inventory.addItem("CHARGE_BEAM");
        assertEquals(1, inventory.getItemCount(), "Duplicate items should not increase count");
    }

    @Test
    @DisplayName("Removing item should decrease count")
    void testRemoveItem() {
        inventory.addItem("CHARGE_BEAM");
        inventory.addItem("ICE_BEAM");
        inventory.removeItem("CHARGE_BEAM");
        assertEquals(1, inventory.getItemCount(), "Should have 1 item after removal");
        assertFalse(inventory.hasItem("CHARGE_BEAM"), "Should not have removed item");
        assertTrue(inventory.hasItem("ICE_BEAM"), "Should still have other item");
    }

    @Test
    @DisplayName("Has item should return correct status")
    void testHasItem() {
        assertFalse(inventory.hasItem("MORPH_BALL"), "Should not have item before adding");

        inventory.addItem("MORPH_BALL");
        assertTrue(inventory.hasItem("MORPH_BALL"), "Should have item after adding");
    }

    @Test
    @DisplayName("Can morph should require morph ball")
    void testCanMorph() {
        assertFalse(inventory.canMorph(), "Cannot morph without Morph Ball");

        inventory.addItem("MORPH_BALL");
        assertTrue(inventory.canMorph(), "Can morph with Morph Ball");
    }

    @Test
    @DisplayName("Can place bombs should require morph ball and bomb")
    void testCanPlaceBombs() {
        assertFalse(inventory.canPlaceBombs(), "Cannot place bombs without required items");

        inventory.addItem("MORPH_BALL");
        assertFalse(inventory.canPlaceBombs(), "Cannot place bombs with just Morph Ball");

        inventory.addItem("BOMBS");
        assertTrue(inventory.canPlaceBombs(), "Can place bombs with Morph Ball and Bombs");
    }

    @Test
    @DisplayName("Can use power bombs should require morph ball and power bomb")
    void testCanUsePowerBombs() {
        assertFalse(inventory.canUsePowerBombs(), "Cannot use power bombs without required items");

        inventory.addItem("MORPH_BALL");
        assertFalse(inventory.canUsePowerBombs(), "Cannot use power bombs with just Morph Ball");

        inventory.addItem("POWER_BOMB");
        assertTrue(inventory.canUsePowerBombs(), "Can use power bombs with Morph Ball and Power Bomb");
    }

    @Test
    @DisplayName("Resource capacity should start at defaults")
    void testInitialResourceCapacity() {
        assertEquals(99, inventory.getResourceCapacity(ResourceType.ENERGY),
                "Energy capacity should start at 99 (base amount)");
        assertEquals(0, inventory.getResourceCapacity(ResourceType.MISSILE),
                "Missile capacity should start at 0");
        assertEquals(0, inventory.getResourceCapacity(ResourceType.SUPER_MISSILE),
                "Super Missile capacity should start at 0");
        assertEquals(0, inventory.getResourceCapacity(ResourceType.POWER_BOMB),
                "Power Bomb capacity should start at 0");
    }

    @Test
    @DisplayName("Increase resource capacity should work")
    void testIncreaseResourceCapacity() {
        inventory.increaseResourceCapacity(ResourceType.MISSILE, 5);
        assertEquals(5, inventory.getResourceCapacity(ResourceType.MISSILE),
                "Missile capacity should be 5 after increase");

        inventory.increaseResourceCapacity(ResourceType.MISSILE, 3);
        assertEquals(8, inventory.getResourceCapacity(ResourceType.MISSILE),
                "Missile capacity should be 8 after second increase");
    }

    @Test
    @DisplayName("Set resource capacity should work")
    void testSetResourceCapacity() {
        inventory.setResourceCapacity(ResourceType.MISSILE, 10);
        assertEquals(10, inventory.getResourceCapacity(ResourceType.MISSILE),
                "Missile capacity should be 10");

        inventory.setResourceCapacity(ResourceType.MISSILE, 15);
        assertEquals(15, inventory.getResourceCapacity(ResourceType.MISSILE),
                "Missile capacity should be 15 after update");
    }

    @Test
    @DisplayName("Collected items should be immutable")
    void testCollectedItemsImmutability() {
        inventory.addItem("CHARGE_BEAM");
        var items = inventory.getCollectedItemIds();

        // Try to modify the returned set (should not affect inventory)
        assertThrows(UnsupportedOperationException.class, () -> {
            items.add("ICE_BEAM");
        }, "Returned set should be unmodifiable");
    }

    @Test
    @DisplayName("Copy should create independent inventory")
    void testCopy() {
        inventory.addItem("CHARGE_BEAM");
        inventory.addItem("MORPH_BALL");
        inventory.increaseResourceCapacity(ResourceType.MISSILE, 5);

        Inventory copy = inventory.copy();

        // Verify copy has same data
        assertEquals(2, copy.getItemCount(), "Copy should have same item count");
        assertTrue(copy.hasItem("CHARGE_BEAM"), "Copy should have same items");
        assertEquals(5, copy.getResourceCapacity(ResourceType.MISSILE),
                "Copy should have same resource capacities");

        // Verify independence
        copy.addItem("ICE_BEAM");
        assertFalse(inventory.hasItem("ICE_BEAM"),
                "Adding to copy should not affect original");
    }

    @Test
    @DisplayName("Multiple items can be added and checked")
    void testMultipleItems() {
        inventory.addItem("CHARGE_BEAM");
        inventory.addItem("ICE_BEAM");
        inventory.addItem("WAVE_BEAM");
        inventory.addItem("MORPH_BALL");

        assertEquals(4, inventory.getItemCount(), "Should have 4 items");
        assertTrue(inventory.hasItem("CHARGE_BEAM"), "Should have Charge Beam");
        assertTrue(inventory.hasItem("ICE_BEAM"), "Should have Ice Beam");
        assertTrue(inventory.hasItem("WAVE_BEAM"), "Should have Wave Beam");
        assertTrue(inventory.hasItem("MORPH_BALL"), "Should have Morph Ball");
    }

    @Test
    @DisplayName("Resource capacity should respect maximums")
    void testResourceCapacityMaximums() {
        // Test energy capacity (should not exceed 299 for normal suit)
        inventory.setResourceCapacity(ResourceType.ENERGY, 299);
        assertEquals(299, inventory.getResourceCapacity(ResourceType.ENERGY),
                "Energy capacity should be 299");

        // Test missile capacity (should not exceed 250)
        inventory.setResourceCapacity(ResourceType.MISSILE, 250);
        assertEquals(250, inventory.getResourceCapacity(ResourceType.MISSILE),
                "Missile capacity should be 250");
    }
}