package com.maprando.randomize;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a location in the game world where an item can be placed.
 * Locations are typically room/node pairs or specific item pedestal locations.
 */
public class Location {
    private final String id;
    private final String name;
    private final String region;
    private final Set<String> requirements;
    private String placedItemId;
    private boolean placed;

    /**
     * Creates a new location.
     *
     * @param id Unique identifier for this location
     * @param name Human-readable name
     * @param region The game region this location is in
     * @param requirements Set of requirement IDs needed to access this location
     */
    public Location(String id, String name, String region, Set<String> requirements) {
        this.id = Objects.requireNonNull(id, "Location ID cannot be null");
        this.name = Objects.requireNonNull(name, "Location name cannot be null");
        this.region = Objects.requireNonNull(region, "Region cannot be null");
        this.requirements = requirements != null ? Set.copyOf(requirements) : Set.of();
        this.placed = false;
        this.placedItemId = null;
    }

    /**
     * Creates a location with no requirements (freely accessible).
     */
    public Location(String id, String name, String region) {
        this(id, name, region, Set.of());
    }

    /**
     * Gets the unique identifier for this location.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the human-readable name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the region this location is in.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Gets the requirements needed to access this location.
     */
    public Set<String> getRequirements() {
        return Set.copyOf(requirements);
    }

    /**
     * Checks if this location has any access requirements.
     */
    public boolean hasRequirements() {
        return !requirements.isEmpty();
    }

    /**
     * Gets the item ID placed at this location.
     */
    public String getPlacedItemId() {
        return placedItemId;
    }

    /**
     * Places an item at this location.
     *
     * @return true if the item was successfully placed
     */
    public boolean placeItem(String itemId) {
        if (placed) {
            return false; // Already has an item
        }
        this.placedItemId = itemId;
        this.placed = true;
        return true;
    }

    /**
     * Checks if this location has an item placed at it.
     */
    public boolean isPlaced() {
        return placed;
    }

    /**
     * Clears any placed item from this location.
     */
    public void clear() {
        this.placedItemId = null;
        this.placed = false;
    }

    /**
     * Creates a copy of this location.
     */
    public Location copy() {
        Location copy = new Location(id, name, region, requirements);
        if (placed && placedItemId != null) {
            copy.placeItem(placedItemId);
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (").append(region).append(")");
        if (placed && placedItemId != null) {
            sb.append(" -> ").append(placedItemId);
        } else {
            sb.append(" -> [Empty]");
        }
        return sb.toString();
    }

    /**
     * Builder for creating Location objects.
     */
    public static class Builder {
        private String id;
        private String name;
        private String region;
        private Set<String> requirements = Set.of();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder requirements(Set<String> requirements) {
            this.requirements = requirements;
            return this;
        }

        public Location build() {
            return new Location(id, name, region, requirements);
        }
    }

    /**
     * Creates a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }
}
