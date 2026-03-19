package com.maprando.model;

import java.util.List;

/**
 * Definition of an item loaded from data.
 * This replaces the Item enum with a data-driven approach.
 */
public class ItemDefinition {
    private final String id;
    private final String displayName;
    private final String description;
    private final String category;
    private final boolean isProgression;
    private final int index; // Index for boolean array tracking

    // Enhanced fields from JSON
    private final Double damageMultiplier;
    private final Integer damageBonus;
    private final Double damageReduction;
    private final List<String> requires;
    private final List<String> enables;
    private final String resourceType;
    private final Integer capacityIncrease;

    /**
     * Basic constructor for items without enhanced properties.
     */
    public ItemDefinition(String id, String displayName, String description,
                        String category, boolean isProgression, int index) {
        this(id, displayName, description, category, isProgression, index,
             null, null, null, null, null, null, null);
    }

    /**
     * Constructor with a single enhanced property.
     */
    public ItemDefinition(String id, String displayName, String description,
                        String category, boolean isProgression, int index,
                        Double damageMultiplier, Integer damageBonus, Double damageReduction,
                        List<String> requires, List<String> enables,
                        String resourceType, Integer capacityIncrease) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.isProgression = isProgression;
        this.index = index;
        this.damageMultiplier = damageMultiplier;
        this.damageBonus = damageBonus;
        this.damageReduction = damageReduction;
        this.requires = requires;
        this.enables = enables;
        this.resourceType = resourceType;
        this.capacityIncrease = capacityIncrease;
    }

    // Basic getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public boolean isProgression() { return isProgression; }
    public int getIndex() { return index; }

    // Enhanced field getters
    public Double getDamageMultiplier() { return damageMultiplier; }
    public Integer getDamageBonus() { return damageBonus; }
    public Double getDamageReduction() { return damageReduction; }
    public List<String> getRequires() { return requires; }
    public List<String> getEnables() { return enables; }
    public String getResourceType() { return resourceType; }
    public Integer getCapacityIncrease() { return capacityIncrease; }

    // Category checks
    public boolean isBeam() {
        return "beam".equals(category);
    }

    public boolean isTank() {
        return "tank".equals(category);
    }

    public boolean isMorphBallAbility() {
        return "morph".equals(category);
    }

    public boolean isSuit() {
        return "suit".equals(category);
    }
}
