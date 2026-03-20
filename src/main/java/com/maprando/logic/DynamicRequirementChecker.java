package com.maprando.logic;

import com.maprando.data.DataLoader;
import com.maprando.model.GameState;
import com.maprando.model.ItemDefinition;
import com.maprando.model.TechDefinition;

import java.util.List;
import java.util.Set;

/**
 * Checks dynamic requirements loaded from JSON data.
 * This system validates item requirements based on the data-driven architecture.
 */
public class DynamicRequirementChecker {
    private final DataLoader dataLoader;

    public DynamicRequirementChecker(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    /**
     * Checks if all requirements for an item are satisfied.
     *
     * @param itemId The ID of the item to check
     * @param state The current game state
     * @return true if all requirements are met or item has no requirements
     */
    public boolean checkRequirements(String itemId, GameState state) {
        ItemDefinition itemDef = dataLoader.getItemRegistry().getById(itemId);
        if (itemDef == null) {
            return false; // Invalid item ID
        }

        List<String> requirements = itemDef.getRequires();
        if (requirements == null || requirements.isEmpty()) {
            return true; // No requirements
        }

        // Check each requirement
        for (String requirement : requirements) {
            if (!checkSingleRequirement(requirement, state)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks a single requirement.
     * Requirements can be:
     * - Tech IDs (e.g., "can_morph")
     * - Item IDs (e.g., "MORPH_BALL")
     */
    private boolean checkSingleRequirement(String requirement, GameState state) {
        // First check if it's a tech
        if (dataLoader.getTechRegistry().getById(requirement) != null) {
            return state.getInventory().hasTech(requirement);
        }

        // Then check if it's an item
        if (dataLoader.getItemRegistry().getById(requirement) != null) {
            return state.getInventory().hasItem(requirement);
        }

        // Unknown requirement - treat as not satisfied
        return false;
    }

    /**
     * Gets the list of tech IDs enabled by collecting an item.
     *
     * @param itemId The ID of the item
     * @return List of tech IDs, or null if item enables no techs
     */
    public List<String> getEnabledTechs(String itemId) {
        ItemDefinition itemDef = dataLoader.getItemRegistry().getById(itemId);
        if (itemDef == null) {
            return null;
        }
        return itemDef.getEnables();
    }

    /**
     * Checks if an item can be collected.
     * This combines requirement checking with current inventory state.
     *
     * @param itemId The ID of the item to check
     * @param state The current game state
     * @return true if the item can be collected
     */
    public boolean canCollectItem(String itemId, GameState state) {
        // Check if already collected
        if (state.getInventory().hasItem(itemId)) {
            return false;
        }

        // Check requirements
        return checkRequirements(itemId, state);
    }

    /**
     * Gets all available tech IDs based on current state.
     * This includes techs that have been enabled by collected items.
     *
     * @param state The current game state
     * @return Set of available tech IDs
     */
    public Set<String> getAvailableTechs(GameState state) {
        return state.getInventory().getEnabledTechIds();
    }
}
