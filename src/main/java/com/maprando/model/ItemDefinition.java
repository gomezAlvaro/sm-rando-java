package com.maprando.model;

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

    public ItemDefinition(String id, String displayName, String description,
                        String category, boolean isProgression, int index) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.isProgression = isProgression;
        this.index = index;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public boolean isProgression() { return isProgression; }
    public int getIndex() { return index; }

    public boolean isBeam() {
        return "beam".equals(category);
    }

    public boolean isTank() {
        return "tank".equals(category);
    }

    public boolean isMorphBallAbility() {
        return "morph".equals(category);
    }
}
