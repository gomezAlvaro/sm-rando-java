package com.maprando.logic;

import com.maprando.data.DataLoader;
import com.maprando.model.GameState;
import com.maprando.model.Inventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

/**
 * Unit tests for the ItemCollector class
 */
@DisplayName("ItemCollector Tests")
class ItemCollectorTest {

    private GameState gameState;
    private ItemCollector collector;

    @BeforeEach
    void setUp() throws IOException {
        // Load data first to initialize ItemRegistry singleton
        DataLoader dataLoader = new DataLoader();
        dataLoader.loadAllData();

        gameState = new GameState();
        collector = new ItemCollector();
    }

    @Test
    @DisplayName("Collecting item should add to inventory")
    void testCollectItemAddsToInventory() {
        assertFalse(gameState.getInventory().hasItem("CHARGE_BEAM"),
                "Should not have item initially");

        collector.collectItem(gameState, "CHARGE_BEAM");

        assertTrue(gameState.getInventory().hasItem("CHARGE_BEAM"),
                "Should have item after collection");
    }

    @Test
    @DisplayName("Collecting energy tank should heal player")
    void testCollectEnergyTankHeals() {
        gameState.setEnergy(50);
        int capacityBefore = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.ENERGY);

        collector.collectItem(gameState, "ENERGY_TANK");

        int capacityAfter = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.ENERGY);
        assertTrue(gameState.getEnergy() > 50,
                "Energy should increase after collecting Energy Tank");
        assertTrue(gameState.getEnergy() <= capacityAfter,
                "Energy should not exceed new capacity");
        assertEquals(capacityBefore + 100, capacityAfter,
                "Energy capacity should increase by 100");
    }

    @Test
    @DisplayName("Collecting missile tank should increase missile capacity")
    void testCollectMissileTankIncreasesCapacity() {
        int capacityBefore = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.MISSILE);

        collector.collectItem(gameState, "MISSILE");

        int capacityAfter = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.MISSILE);
        assertEquals(capacityBefore + 5, capacityAfter,
                "Missile capacity should increase by 5");
    }

    @Test
    @DisplayName("Collecting super missile tank should increase super missile capacity")
    void testCollectSuperMissileTankIncreasesCapacity() {
        int capacityBefore = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.SUPER_MISSILE);

        ItemCollector.collectItem(gameState, "SUPER_MISSILE");

        int capacityAfter = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.SUPER_MISSILE);
        assertEquals(capacityBefore + 5, capacityAfter,
                "Super Missile capacity should increase by 5");
    }

    @Test
    @DisplayName("Collecting power bomb tank should increase power bomb capacity")
    void testCollectPowerBombTankIncreasesCapacity() {
        int capacityBefore = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.POWER_BOMB);

        ItemCollector.collectItem(gameState, "POWER_BOMB");

        int capacityAfter = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.POWER_BOMB);
        assertEquals(capacityBefore + 5, capacityAfter,
                "Power Bomb capacity should increase by 5");
    }

    @Test
    @DisplayName("Can collect item should work for collectible items")
    void testCanCollectItem() {
        assertTrue(collector.canCollect(gameState, "CHARGE_BEAM"),
                "Should be able to collect Charge Beam");
        assertTrue(collector.canCollect(gameState, "MORPH_BALL"),
                "Should be able to collect Morph Ball");
    }

    @Test
    @DisplayName("Can collect tank should work when capacity allows")
    void testCanCollectTankWhenAllowed() {
        gameState.getInventory().setResourceCapacity(
                com.maprando.model.ResourceType.MISSILE, 240);

        assertTrue(ItemCollector.canCollectTank(gameState, "MISSILE"),
                "Should be able to collect Missile Tank when under capacity");
    }

    @Test
    @DisplayName("Can collect tank should fail when at capacity")
    void testCanCollectTankAtCapacity() {
        gameState.getInventory().setResourceCapacity(
                com.maprando.model.ResourceType.MISSILE, 250);

        assertFalse(ItemCollector.canCollectTank(gameState, "MISSILE"),
                "Should not be able to collect Missile Tank when at capacity");
    }

    @Test
    @DisplayName("Collect all should add multiple items")
    void testCollectAll() {
        var items = java.util.List.of("CHARGE_BEAM", "ICE_BEAM", "WAVE_BEAM");

        collector.collectAll(gameState, items);

        assertTrue(gameState.getInventory().hasItem("CHARGE_BEAM"),
                "Should have Charge Beam");
        assertTrue(gameState.getInventory().hasItem("ICE_BEAM"),
                "Should have Ice Beam");
        assertTrue(gameState.getInventory().hasItem("WAVE_BEAM"),
                "Should have Wave Beam");
    }

    @Test
    @DisplayName("Collecting duplicate item should not cause issues")
    void testCollectDuplicateItem() {
        collector.collectItem(gameState, "CHARGE_BEAM");
        int itemCountBefore = gameState.getInventory().getItemCount();

        collector.collectItem(gameState, "CHARGE_BEAM");

        assertEquals(itemCountBefore, gameState.getInventory().getItemCount(),
                "Duplicate item should not increase count");
    }

    @Test
    @DisplayName("Collecting same item multiple times should only apply once")
    void testCollectMultipleTanks() {
        int initialCapacity = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.MISSILE);

        // Collect same item 3 times
        collector.collectItem(gameState, "MISSILE");
        collector.collectItem(gameState, "MISSILE");
        collector.collectItem(gameState, "MISSILE");

        int finalCapacity = gameState.getInventory().getResourceCapacity(
                com.maprando.model.ResourceType.MISSILE);
        assertEquals(initialCapacity + 5, finalCapacity,
                "Same item collected multiple times should only apply once");
    }

    @Test
    @DisplayName("Energy tank should heal to full capacity")
    void testEnergyTankHealsToFull() {
        gameState.setEnergy(30);
        gameState.getInventory().setResourceCapacity(
                com.maprando.model.ResourceType.ENERGY, 200);

        collector.collectItem(gameState, "ENERGY_TANK");

        assertTrue(gameState.getEnergy() > 30,
                "Energy should increase");
        assertTrue(gameState.getEnergy() <= 300,
                "Energy should not exceed new capacity");
    }

    @Test
    @DisplayName("Energy tank at full capacity should heal to new capacity")
    void testEnergyTankAtFullCapacity() {
        gameState.getInventory().setResourceCapacity(
                com.maprando.model.ResourceType.ENERGY, 598);
        gameState.setEnergy(598);

        ItemCollector.collectItem(gameState, "ENERGY_TANK");

        // After collecting energy tank, capacity increases by 100 (not 299)
        // Energy is set to new capacity
        assertEquals(698, gameState.getEnergy(),
                "Energy should be set to new capacity of 698");
    }
}