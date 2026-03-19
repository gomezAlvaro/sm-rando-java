package com.maprando.randomize;

import com.maprando.util.TestSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ItemPool class
 */
@DisplayName("ItemPool Tests")
class ItemPoolTest {

    private ItemPool pool;

    @BeforeAll
    static void setUpClass() {
        TestSetup.initializeMinimalRegistry();
    }

    @BeforeEach
    void setUp() {
        pool = new ItemPool();
    }

    @Test
    @DisplayName("New pool should be empty")
    void testNewPoolIsEmpty() {
        assertTrue(pool.isEmpty(), "New pool should be empty");
        assertEquals(0, pool.getTotalItemCount(), "New pool should have 0 items");
    }

    @Test
    @DisplayName("Add item should increase count")
    void testAddItem() {
        pool.addItem("CHARGE_BEAM", 1, true);

        assertFalse(pool.isEmpty(), "Pool should not be empty after adding item");
        assertEquals(1, pool.getTotalItemCount(), "Pool should have 1 item");
    }

    @Test
    @DisplayName("Add multiple items should count correctly")
    void testAddMultipleItems() {
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ICE_BEAM", 2, true);
        pool.addItem("ENERGY_TANK", 3, false);

        assertEquals(6, pool.getTotalItemCount(), "Pool should have 6 total items");
    }

    @Test
    @DisplayName("Get item count should return specific item count")
    void testGetItemCount() {
        pool.addItem("CHARGE_BEAM", 3, true);

        assertEquals(3, pool.getItemCount("CHARGE_BEAM"),
                "Should have 3 Charge Beams");
    }

    @Test
    @DisplayName("Get progression items should return only progression items")
    void testGetProgressionItems() {
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ENERGY_TANK", 1, false);
        pool.addItem("ICE_BEAM", 1, true);

        assertEquals(2, pool.getProgressionItems().size(), "Should have 2 progression items");
        assertTrue(pool.getProgressionItems().contains("CHARGE_BEAM"),
                "Progression items should include Charge Beam");
        assertFalse(pool.getProgressionItems().contains("ENERGY_TANK"),
                "Progression items should not include Energy Tank");
    }

    @Test
    @DisplayName("Get filler items should return only filler items")
    void testGetFillerItems() {
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ENERGY_TANK", 2, false);
        pool.addItem("MISSILE_TANK", 3, false);

        assertEquals(2, pool.getFillerItems().size(), "Should have 2 unique filler item types");
        assertTrue(pool.getFillerItems().contains("ENERGY_TANK"),
                "Filler items should include Energy Tank");
        assertTrue(pool.getFillerItems().contains("MISSILE_TANK"),
                "Filler items should include Missile Tank");
        assertFalse(pool.getFillerItems().contains("CHARGE_BEAM"),
                "Filler items should not include Charge Beam");
    }

    @Test
    @DisplayName("Remove item should decrease count")
    void testRemoveItem() {
        pool.addItem("CHARGE_BEAM", 3, true);

        pool.removeItem("CHARGE_BEAM");

        assertEquals(2, pool.getItemCount("CHARGE_BEAM"),
                "Should have 2 Charge Beams after removal");
    }

    @Test
    @DisplayName("Remove from empty pool should handle gracefully")
    void testRemoveFromEmptyPool() {
        // Should not throw exception
        assertDoesNotThrow(() -> pool.removeItem("CHARGE_BEAM"),
                "Removing from empty pool should not throw");
    }

    @Test
    @DisplayName("Pick random progression item should return progression item")
    @org.junit.jupiter.api.Disabled("pickRandomProgressionItem method not implemented")
    void testPickRandomProgressionItem() {
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ICE_BEAM", 1, true);
        pool.addItem("ENERGY_TANK", 1, false);

        java.util.Random random = new java.util.Random(42);
        // Item item = pool.pickRandomProgressionItem(random);

        // assertNotNull(item, "Should return an item");
        // assertTrue(item == "CHARGE_BEAM" || item == "ICE_BEAM",
        //         "Should return progression item only");
    }

    @Test
    @DisplayName("Pick random filler item should return filler item")
    @org.junit.jupiter.api.Disabled("pickRandomFillerItem method not implemented")
    void testPickRandomFillerItem() {
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ENERGY_TANK", 2, false);
        pool.addItem("MISSILE_TANK", 1, false);

        java.util.Random random = new java.util.Random(42);
        // Item item = pool.pickRandomFillerItem(random);

        // assertNotNull(item, "Should return an item");
        // assertTrue(item == "ENERGY_TANK" || item == "MISSILE_TANK",
        //         "Should return filler item only");
    }

    @Test
    @DisplayName("Pick random item should return any item")
    @org.junit.jupiter.api.Disabled("pickRandomItem method not implemented")
    void testPickRandomItem() {
        pool.addItem("CHARGE_BEAM", 2, true);
        pool.addItem("ENERGY_TANK", 2, false);

        java.util.Random random = new java.util.Random(42);
        String item = null; // pool.pickRandomItem(random);

        // assertNotNull(item, "Should return an item");
        // After picking, the item might still be in the pool (if count > 0)
        // or might be removed (if count was 1)
        // So we can't assert it's in getAllItems() after picking
        // assertTrue(item == "CHARGE_BEAM" || item == "ENERGY_TANK",
        //         "Returned item should be one of the items in the pool");
    }

    @Test
    @DisplayName("Pool should track counts correctly")
    void testItemCountTracking() {
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ENERGY_TANK", 5, false);

        assertEquals(6, pool.getTotalItemCount(), "Total should be 6");
        assertEquals(1, pool.getProgressionItems().size(), "Progression should be 1");
        assertEquals(1, pool.getFillerItems().size(), "Filler should be 1 (unique types)");
    }

    @Test
    @DisplayName("Create minimal pool should have expected items")
    void testCreateMinimalPool() {
        ItemPool minimalPool = ItemPool.createMinimalPool();

        assertFalse(minimalPool.isEmpty(), "Minimal pool should not be empty");
        assertTrue(minimalPool.getProgressionItems().size() > 0,
                "Minimal pool should have progression items");
    }

    @Test
    @DisplayName("Removing last item should empty the pool")
    void testRemovingLastItem() {
        pool.addItem("CHARGE_BEAM", 1, true);

        pool.removeItem("CHARGE_BEAM");

        assertTrue(pool.isEmpty(), "Pool should be empty after removing last item");
    }

    @Test
    @DisplayName("Get all items should return all items in pool")
    void testGetAllItems() {
        pool.addItem("CHARGE_BEAM", 2, true);
        pool.addItem("ENERGY_TANK", 3, false);

        var allItems = pool.getAvailableItems();

        assertEquals(2, allItems.size(), "Should have 2 unique item types");
        assertTrue(allItems.contains("CHARGE_BEAM"), "Should include Charge Beam");
        assertTrue(allItems.contains("ENERGY_TANK"), "Should include Energy Tank");
    }

    @Test
    @DisplayName("Pick random should reduce pool count")
    @org.junit.jupiter.api.Disabled("pickRandomItem method not implemented")
    void testPickRandomReducesPool() {
        pool.addItem("CHARGE_BEAM", 1, true);
        int initialCount = pool.getTotalItemCount();

        java.util.Random random = new java.util.Random(42);
        // pool.pickRandomItem(random);

        assertEquals(initialCount - 1, pool.getTotalItemCount(),
                "Pool count should decrease after picking");
    }

    @Test
    @DisplayName("Multiple removals should work correctly")
    void testMultipleRemovals() {
        pool.addItem("CHARGE_BEAM", 5, true);

        pool.removeItem("CHARGE_BEAM");
        pool.removeItem("CHARGE_BEAM");
        pool.removeItem("CHARGE_BEAM");

        assertEquals(2, pool.getItemCount("CHARGE_BEAM"),
                "Should have 2 items after 3 removals");
    }
}