package com.maprando.logic;

import com.maprando.model.GameState;
import com.maprando.model.ResourceType;

/**
 * Handles the logic for collecting items and updating game state accordingly.
 */
public class ItemCollector {

    /**
     * Processes the collection of an item, updating the game state appropriately.
     * This includes adding the item to inventory and updating resource capacities.
     *
     * @param state The current game state
     * @param itemId The ID of the item being collected
     * @return true if the item was successfully collected
     */
    public static boolean collectItem(GameState state, String itemId) {
        if (state == null || itemId == null) {
            return false;
        }

        // Add item to inventory
        state.collectItem(itemId);

        // Handle special cases for item collection
        handleSpecialItemEffects(state, itemId);

        return true;
    }

    /**
     * Handles special effects that occur when certain items are collected.
     */
    private static void handleSpecialItemEffects(GameState state, String itemId) {
        switch (itemId) {
            case "VARIA_SUIT", "GRAVITY_SUIT" -> {
                // Suits reduce damage taken - handled by DamageCalculator
            }
            case "ENERGY_TANK" -> {
                // Fully heal on energy tank pickup
                state.setEnergy(state.getInventory().getResourceCapacity(ResourceType.ENERGY));
            }
            case "MORPH_BALL" -> {
                // Grant morph ball capability
            }
            default -> {
                // No special effects for other items
            }
        }
    }

    /**
     * Checks if an item can be collected (not already in inventory).
     */
    public static boolean canCollect(GameState state, String itemId) {
        if (state == null || itemId == null) {
            return false;
        }

        // Most items can only be collected once
        return !state.getInventory().hasItem(itemId);
    }

    /**
     * Checks if a tank item (missile, super missile, power bomb, energy) can be collected.
     * Tanks can be collected multiple times up to maximum capacity.
     */
    public static boolean canCollectTank(GameState state, String tankItemId) {
        if (state == null || tankItemId == null) {
            return false;
        }

        // Check if it's a tank item by ID pattern
        if (!tankItemId.endsWith("_TANK")) {
            return false;
        }

        ResourceType resourceType = switch (tankItemId) {
            case "MISSILE_TANK" -> ResourceType.MISSILE;
            case "SUPER_MISSILE_TANK" -> ResourceType.SUPER_MISSILE;
            case "POWER_BOMB_TANK" -> ResourceType.POWER_BOMB;
            case "ENERGY_TANK" -> ResourceType.ENERGY;
            default -> throw new IllegalArgumentException("Not a tank item: " + tankItemId);
        };

        int currentCapacity = state.getInventory().getResourceCapacity(resourceType);
        int maxCapacity = resourceType.getMaxCapacity();

        return currentCapacity < maxCapacity;
    }

    /**
     * Simulates collecting a set of items in sequence.
     */
    public static void collectAll(GameState state, Iterable<String> itemIds) {
        for (String itemId : itemIds) {
            collectItem(state, itemId);
        }
    }
}
