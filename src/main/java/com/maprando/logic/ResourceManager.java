package com.maprando.logic;

import com.maprando.model.GameState;
import com.maprando.model.ResourceLevel;
import com.maprando.model.ResourceType;

/**
 * Manages resource availability and consumption for the game state.
 */
public class ResourceManager {

    /**
     * Checks if the player has enough of a resource available.
     *
     * @param state The current game state
     * @param type The resource type to check
     * @param amount The amount required
     * @return true if the player has at least the required amount
     */
    public static boolean hasResource(GameState state, ResourceType type, int amount) {
        if (state == null || type == null || amount < 0) {
            return false;
        }

        if (amount == 0) {
            return true; // Zero resources always available
        }

        return state.hasResource(type, amount);
    }

    /**
     * Attempts to consume a specified amount of a resource.
     *
     * @param state The current game state
     * @param type The resource type to consume
     * @param amount The amount to consume
     * @return true if the resource was successfully consumed
     */
    public static boolean consumeResource(GameState state, ResourceType type, int amount) {
        if (state == null || type == null || amount < 0) {
            return false;
        }

        if (amount == 0) {
            return true; // Consuming zero resources always succeeds
        }

        return state.consumeResource(type, amount);
    }

    /**
     * Gets the current resource level for a type.
     */
    public static ResourceLevel getResourceLevel(GameState state, ResourceType type) {
        if (state == null || type == null) {
            return null;
        }
        return state.getResourceLevel(type);
    }

    /**
     * Gets the amount of a resource that is currently available.
     */
    public static int getAvailableAmount(GameState state, ResourceType type) {
        ResourceLevel level = getResourceLevel(state, type);
        return level != null ? level.getRemaining() : 0;
    }

    /**
     * Checks if the player has enough energy to survive taking a specified amount of damage.
     */
    public static boolean canSurviveDamage(GameState state, int damage) {
        if (state == null || damage < 0) {
            return false;
        }
        return state.getEnergy() > damage;
    }

    /**
     * Checks if the player can perform an action that requires a specific resource.
     * This is a convenience method combining resource and damage checks.
     */
    public static boolean canPerformAction(GameState state, ResourceType type, int amount, int potentialDamage) {
        return hasResource(state, type, amount) && canSurviveDamage(state, potentialDamage);
    }

    /**
     * Calculates the percentage of resources remaining across all types.
     */
    public static double calculateOverallResourcePercentage(GameState state) {
        if (state == null) {
            return 0.0;
        }

        double totalPercentage = 0.0;
        int count = 0;

        for (ResourceType type : ResourceType.values()) {
            ResourceLevel level = state.getResourceLevel(type);
            if (level != null) {
                totalPercentage += (1.0 - level.getConsumptionPercentage());
                count++;
            }
        }

        return count > 0 ? totalPercentage / count : 0.0;
    }

    /**
     * Checks if the player is critically low on resources (less than 25% remaining).
     */
    public static boolean isCriticallyLow(GameState state, ResourceType type) {
        ResourceLevel level = getResourceLevel(state, type);
        if (level == null) {
            return true; // No resource level means critical
        }

        return level.getConsumptionPercentage() > 0.75;
    }

    /**
     * Checks if the player has any critical resources (less than 25% remaining).
     */
    public static boolean hasAnyCriticalResources(GameState state) {
        if (state == null) {
            return true;
        }

        for (ResourceType type : ResourceType.values()) {
            if (isCriticallyLow(state, type)) {
                return true;
            }
        }

        return false;
    }
}
