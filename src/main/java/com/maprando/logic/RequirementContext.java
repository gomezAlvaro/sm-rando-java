package com.maprando.logic;

import com.maprando.model.GameState;
import com.maprando.model.Inventory;

/**
 * Context for evaluating requirements.
 * Provides access to game state and inventory for requirement checking.
 *
 * This separates the requirement logic from the game state implementation,
 * allowing requirements to be evaluated in different contexts.
 */
public record RequirementContext(Inventory inventory, GameState gameState) {
    /**
     * Create a context from just an inventory.
     * Convenience method for requirements that don't need full game state.
     */
    public static RequirementContext fromInventory(Inventory inventory) {
        return new RequirementContext(inventory, null);
    }

    /**
     * Create a context from a game state.
     * Extracts inventory from the game state.
     */
    public static RequirementContext fromGameState(GameState gameState) {
        return new RequirementContext(gameState.getInventory(), gameState);
    }
}
