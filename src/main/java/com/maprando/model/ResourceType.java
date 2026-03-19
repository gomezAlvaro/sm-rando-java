package com.maprando.model;

/**
 * Represents the different types of resources Samus can collect and use.
 * Resources are consumable items that can be depleted during gameplay.
 */
public enum ResourceType {
    ENERGY("Energy", "Health points", 99, 2099, 299),
    MISSILE("Missile", "Missile ammunition", 0, 250, 5),
    SUPER_MISSILE("Super Missile", "Super missile ammunition", 0, 50, 5),
    POWER_BOMB("Power Bomb", "Power bomb ammunition", 0, 50, 5);

    private final String displayName;
    private final String description;
    private final int baseAmount;
    private final int maxCapacity;
    private final int incrementPerTank;

    ResourceType(String displayName, String description, int baseAmount,
                 int maxCapacity, int incrementPerTank) {
        this.displayName = displayName;
        this.description = description;
        this.baseAmount = baseAmount;
        this.maxCapacity = maxCapacity;
        this.incrementPerTank = incrementPerTank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * The starting amount of this resource without any expansions.
     */
    public int getBaseAmount() {
        return baseAmount;
    }

    /**
     * The maximum possible capacity of this resource with all expansions.
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * The amount added to max capacity per expansion tank collected.
     */
    public int getIncrementPerTank() {
        return incrementPerTank;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
