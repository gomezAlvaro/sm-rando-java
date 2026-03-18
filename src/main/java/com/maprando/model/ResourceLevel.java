package com.maprando.model;

/**
 * Tracks the current state of a resource, including how much has been consumed
 * and how much remains available.
 *
 * This is a record class, making it immutable by design.
 */
public record ResourceLevel(ResourceType type, int maxCapacity, int consumed) {

    /**
     * Creates a new ResourceLevel with the given type and maximum capacity.
     * Initially, nothing is consumed.
     */
    public ResourceLevel(ResourceType type, int maxCapacity) {
        this(type, maxCapacity, 0);
    }

    /**
     * Returns the amount of this resource that is currently available.
     */
    public int getRemaining() {
        return Math.max(0, maxCapacity - consumed);
    }

    /**
     * Returns the percentage of this resource that has been consumed.
     */
    public double getConsumptionPercentage() {
        if (maxCapacity == 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) consumed / maxCapacity);
    }

    /**
     * Returns true if there is at least the specified amount remaining.
     */
    public boolean hasEnough(int amount) {
        return getRemaining() >= amount;
    }

    /**
     * Creates a new ResourceLevel with additional consumed amount.
     */
    public ResourceLevel withConsumption(int additionalConsumption) {
        return new ResourceLevel(type, maxCapacity, consumed + additionalConsumption);
    }

    /**
     * Creates a new ResourceLevel with the specified max capacity.
     */
    public ResourceLevel withCapacity(int newCapacity) {
        return new ResourceLevel(type, newCapacity, consumed);
    }

    /**
     * Creates a ResourceLevel representing a fresh start with no consumption.
     */
    public static ResourceLevel fresh(ResourceType type, int capacity) {
        return new ResourceLevel(type, capacity, 0);
    }

    @Override
    public String toString() {
        return String.format("%s: %d/%d remaining", type.getDisplayName(),
                           getRemaining(), maxCapacity);
    }
}
