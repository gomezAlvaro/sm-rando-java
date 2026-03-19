package com.maprando.model;

import java.util.*;

/**
 * Registry for all tech definitions in the game.
 * Similar to ItemRegistry but for tech abilities.
 */
public class TechRegistry {
    private final Map<String, TechDefinition> techById;
    private final Map<Integer, TechDefinition> techByIndex;
    private final int techCount;

    private static TechRegistry instance;

    /**
     * Creates a new tech registry.
     */
    public TechRegistry() {
        this.techById = new HashMap<>();
        this.techByIndex = new HashMap<>();
        this.techCount = 0;
    }

    /**
     * Registers a tech definition.
     */
    public void registerTech(TechDefinition tech) {
        techById.put(tech.getId(), tech);
        techByIndex.put(tech.getIndex(), tech);
    }

    /**
     * Gets a tech definition by ID.
     */
    public TechDefinition getById(String techId) {
        return techById.get(techId);
    }

    /**
     * Gets a tech definition by index.
     */
    public TechDefinition getByIndex(int index) {
        return techByIndex.get(index);
    }

    /**
     * Checks if a tech is available in the inventory.
     */
    public boolean hasTech(boolean[] techArray, String techId) {
        TechDefinition tech = techById.get(techId);
        if (tech == null) {
            return false;
        }
        return hasTech(techArray, tech.getIndex());
    }

    /**
     * Checks if a tech is available in the inventory by index.
     */
    public boolean hasTech(boolean[] techArray, int index) {
        if (index < 0 || index >= techArray.length) {
            return false;
        }
        return techArray[index];
    }

    /**
     * Creates a boolean array for tracking techs.
     */
    public boolean[] createTechArray() {
        return new boolean[techById.size()];
    }

    /**
     * Gets all tech definitions that have been enabled in the given array.
     */
    public List<TechDefinition> getEnabledTechs(boolean[] techArray) {
        List<TechDefinition> enabled = new ArrayList<>();
        for (Map.Entry<Integer, TechDefinition> entry : techByIndex.entrySet()) {
            int index = entry.getKey();
            if (index < techArray.length && techArray[index]) {
                enabled.add(entry.getValue());
            }
        }
        return enabled;
    }

    /**
     * Gets all enabled tech IDs as a set.
     */
    public Set<String> getEnabledTechIds(boolean[] techArray) {
        Set<String> enabledIds = new HashSet<>();
        for (Map.Entry<Integer, TechDefinition> entry : techByIndex.entrySet()) {
            int index = entry.getKey();
            if (index < techArray.length && techArray[index]) {
                enabledIds.add(entry.getValue().getId());
            }
        }
        return enabledIds;
    }

    /**
     * Gets the count of enabled techs.
     */
    public int getEnabledCount(boolean[] techArray) {
        int count = 0;
        for (int i = 0; i < Math.min(techArray.length, techById.size()); i++) {
            if (techArray[i]) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the total number of techs registered.
     */
    public int getTechCount() {
        return techById.size();
    }

    /**
     * Gets all registered tech definitions.
     */
    public Collection<TechDefinition> getAllTechs() {
        return Collections.unmodifiableCollection(techById.values());
    }

    /**
     * Sets the singleton instance for convenience.
     */
    public static void setInstance(TechRegistry registry) {
        instance = registry;
    }

    /**
     * Gets the singleton instance.
     */
    public static TechRegistry getInstance() {
        return instance;
    }
}
