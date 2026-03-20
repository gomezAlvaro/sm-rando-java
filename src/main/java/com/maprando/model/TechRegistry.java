package com.maprando.model;

import java.util.*;

/**
 * Registry for all tech definitions from Rust tech_data.json.
 * Techs represent player techniques/skills (canHeatRun, canWalljump, etc.).
 * Aligned with Rust MapRandomizer tech system.
 */
public class TechRegistry {
    private final Map<Integer, TechDefinition> techByTechId;
    private final Map<String, TechDefinition> techByName;
    private int maxTechId;

    private static TechRegistry instance;

    /**
     * Creates a new tech registry.
     */
    public TechRegistry() {
        this.techByTechId = new HashMap<>();
        this.techByName = new HashMap<>();
        this.maxTechId = 0;
    }

    /**
     * Registers a tech definition.
     */
    public void registerTech(TechDefinition tech) {
        techByTechId.put(tech.getTechId(), tech);
        techByName.put(tech.getName(), tech);
        maxTechId = Math.max(maxTechId, tech.getTechId());
    }

    /**
     * Gets a tech definition by tech ID.
     */
    public TechDefinition getByTechId(int techId) {
        return techByTechId.get(techId);
    }

    /**
     * Gets a tech definition by name.
     */
    public TechDefinition getByName(String name) {
        return techByName.get(name);
    }

    /**
     * Gets a tech definition by ID (for compatibility - uses name).
     */
    public TechDefinition getById(String techId) {
        return techByName.get(techId);
    }

    /**
     * Gets a tech definition by index (for compatibility - maps to techId).
     */
    public TechDefinition getByIndex(int index) {
        return techByTechId.get(index);
    }

    /**
     * Checks if a tech is available in the inventory.
     */
    public boolean hasTech(boolean[] techArray, String techName) {
        TechDefinition tech = techByName.get(techName);
        if (tech == null) {
            return false;
        }
        return hasTech(techArray, tech.getTechId());
    }

    /**
     * Checks if a tech is available in the inventory by tech ID.
     */
    public boolean hasTech(boolean[] techArray, int techId) {
        if (techId < 0 || techId >= techArray.length) {
            return false;
        }
        return techArray[techId];
    }

    /**
     * Creates a boolean array for tracking techs.
     * Sized to max tech ID + 1 to accommodate sparse tech IDs.
     */
    public boolean[] createTechArray() {
        return new boolean[maxTechId + 1];
    }

    /**
     * Gets all tech definitions that have been enabled in the given array.
     */
    public List<TechDefinition> getEnabledTechs(boolean[] techArray) {
        List<TechDefinition> enabled = new ArrayList<>();
        for (Map.Entry<Integer, TechDefinition> entry : techByTechId.entrySet()) {
            int techId = entry.getKey();
            if (techId < techArray.length && techArray[techId]) {
                enabled.add(entry.getValue());
            }
        }
        return enabled;
    }

    /**
     * Gets all enabled tech names as a set.
     */
    public Set<String> getEnabledTechNames(boolean[] techArray) {
        Set<String> enabledNames = new HashSet<>();
        for (Map.Entry<Integer, TechDefinition> entry : techByTechId.entrySet()) {
            int techId = entry.getKey();
            if (techId < techArray.length && techArray[techId]) {
                enabledNames.add(entry.getValue().getName());
            }
        }
        return enabledNames;
    }

    /**
     * Gets all enabled tech IDs as a set (for compatibility).
     */
    public Set<String> getEnabledTechIds(boolean[] techArray) {
        return getEnabledTechNames(techArray);
    }

    /**
     * Gets the count of enabled techs.
     */
    public int getEnabledCount(boolean[] techArray) {
        int count = 0;
        for (int i = 0; i < Math.min(techArray.length, maxTechId + 1); i++) {
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
        return techByTechId.size();
    }

    /**
     * Gets the max tech ID (for array sizing).
     */
    public int getMaxTechId() {
        return maxTechId;
    }

    /**
     * Gets all registered tech definitions.
     */
    public Collection<TechDefinition> getAllTechs() {
        return Collections.unmodifiableCollection(techByTechId.values());
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
