package com.maprando.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data-driven inventory system using boolean arrays.
 * This replaces the enum-based inventory to align with the original Rust MapRandomizer.
 *
 * Architecture matches original Rust:
 *   items: vec![false; game_data.item_isv.keys.len()]
 */
public class Inventory {
    private final boolean[] collectedItems;
    private final Map<ResourceType, Integer> resourceCapacities;
    private final ItemRegistry itemRegistry;

    /**
     * Creates a new empty inventory using the singleton ItemRegistry.
     * Convenience constructor for migration period.
     */
    public Inventory() {
        this(ItemRegistry.getInstance());
    }

    /**
     * Creates a new empty inventory.
     */
    public Inventory(ItemRegistry registry) {
        this.itemRegistry = registry;
        this.collectedItems = registry.createInventoryArray();
        this.resourceCapacities = new HashMap<>();
        initializeBaseResources();
    }

    /**
     * Creates a copy of an existing inventory.
     */
    public Inventory(Inventory other) {
        this.itemRegistry = other.itemRegistry;
        this.collectedItems = new boolean[other.collectedItems.length];
        System.arraycopy(other.collectedItems, 0, this.collectedItems, 0, other.collectedItems.length);
        this.resourceCapacities = new HashMap<>(other.resourceCapacities);
    }

    private void initializeBaseResources() {
        for (ResourceType type : ResourceType.values()) {
            resourceCapacities.put(type, type.getBaseAmount());
        }
    }

    /**
     * Adds an item by ID.
     * @return true if the item was added (wasn't already collected)
     */
    public boolean addItem(String itemId) {
        ItemDefinition def = itemRegistry.getById(itemId);
        if (def == null) {
            return false;
        }
        return addItem(def.getIndex());
    }

    /**
     * Adds an item by index.
     * @return true if the item was added (wasn't already collected)
     */
    public boolean addItem(int index) {
        if (index < 0 || index >= collectedItems.length) {
            return false;
        }
        if (collectedItems[index]) {
            return false; // Already collected
        }
        collectedItems[index] = true;
        return true;
    }

    /**
     * Removes an item by ID.
     * @return true if the item was removed (was present)
     */
    public boolean removeItem(String itemId) {
        ItemDefinition def = itemRegistry.getById(itemId);
        if (def == null) {
            return false;
        }
        return removeItem(def.getIndex());
    }

    /**
     * Removes an item by index.
     * @return true if the item was removed (was present)
     */
    public boolean removeItem(int index) {
        if (index < 0 || index >= collectedItems.length) {
            return false;
        }
        if (!collectedItems[index]) {
            return false; // Not collected
        }
        collectedItems[index] = false;
        return true;
    }

    /**
     * Checks if the player has collected a specific item by ID.
     */
    public boolean hasItem(String itemId) {
        return itemRegistry.hasItemById(collectedItems, itemId);
    }

    /**
     * Checks if the player has collected a specific item by index.
     */
    public boolean hasItem(int index) {
        return itemRegistry.hasItem(collectedItems, index);
    }

    /**
     * Returns all collected item definitions.
     */
    public List<ItemDefinition> getCollectedItems() {
        return Collections.unmodifiableList(itemRegistry.getCollectedItems(collectedItems));
    }

    /**
     * Returns all collected item IDs as an unmodifiable set.
     */
    public Set<String> getCollectedItemIds() {
        return Collections.unmodifiableSet(itemRegistry.getCollectedItemIds(collectedItems));
    }

    /**
     * Returns the number of items collected.
     */
    public int getItemCount() {
        return itemRegistry.getCollectedCount(collectedItems);
    }

    /**
     * Increases the maximum capacity of a resource type.
     */
    public void increaseResourceCapacity(ResourceType type, int amount) {
        int currentCapacity = resourceCapacities.getOrDefault(type, type.getBaseAmount());
        int newCapacity = Math.min(type.getMaxCapacity(), currentCapacity + amount);
        resourceCapacities.put(type, newCapacity);
    }

    /**
     * Sets the maximum capacity of a resource type.
     */
    public void setResourceCapacity(ResourceType type, int capacity) {
        int clampedCapacity = Math.min(type.getMaxCapacity(),
                                     Math.max(type.getBaseAmount(), capacity));
        resourceCapacities.put(type, clampedCapacity);
    }

    /**
     * Gets the current maximum capacity for a resource type.
     */
    public int getResourceCapacity(ResourceType type) {
        return resourceCapacities.getOrDefault(type, type.getBaseAmount());
    }

    /**
     * Creates a ResourceLevel for the given resource type.
     */
    public ResourceLevel createResourceLevel(ResourceType type) {
        return new ResourceLevel(type, getResourceCapacity(type));
    }

    /**
     * Returns true if the player has morph ball capability.
     */
    public boolean canMorph() {
        return hasItem("MORPH_BALL");
    }

    /**
     * Returns true if the player can place bombs.
     */
    public boolean canPlaceBombs() {
        return canMorph() && hasItem("BOMB");
    }

    /**
     * Returns true if the player can use power bombs.
     */
    public boolean canUsePowerBombs() {
        return canMorph() && hasItem("POWER_BOMB");
    }

    /**
     * Returns true if the player can survive heat (has Varia or Gravity Suit).
     */
    public boolean canSurviveHeat() {
        return hasItem("VARIA_SUIT") || hasItem("GRAVITY_SUIT");
    }

    /**
     * Returns true if the player can survive lava (has Gravity Suit).
     */
    public boolean canSurviveLava() {
        return hasItem("GRAVITY_SUIT");
    }

    /**
     * Returns true if the player can grapple.
     */
    public boolean canGrapple() {
        return hasItem("GRAPPLE_BEAM");
    }

    /**
     * Returns true if the player has the Ice Beam.
     */
    public boolean hasIceBeam() {
        return hasItem("ICE_BEAM");
    }

    /**
     * Returns true if the player has the Speed Booster.
     */
    public boolean hasSpeedBooster() {
        return hasItem("SPEED_BOOSTER");
    }

    /**
     * Returns true if the player has the Space Jump.
     */
    public boolean hasSpaceJump() {
        return hasItem("SPACE_JUMP");
    }

    /**
     * Returns true if the player has the Screw Attack.
     */
    public boolean hasScrewAttack() {
        return hasItem("SCREW_ATTACK");
    }

    /**
     * Returns the item registry used by this inventory.
     */
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    /**
     * Creates a copy of this inventory.
     */
    public Inventory copy() {
        return new Inventory(this);
    }

    /**
     * Gets the raw boolean array for direct access.
     * Use with caution - this is for performance-critical code.
     */
    public boolean[] getRawArray() {
        return collectedItems;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Inventory:\n");
        sb.append("  Items: ").append(getItemCount()).append("/").append(itemRegistry.getItemCount()).append("\n");
        sb.append("  Energy: ").append(getResourceCapacity(ResourceType.ENERGY)).append("\n");
        sb.append("  Missiles: ").append(getResourceCapacity(ResourceType.MISSILE)).append("\n");
        sb.append("  Super Missiles: ").append(getResourceCapacity(ResourceType.SUPER_MISSILE)).append("\n");
        sb.append("  Power Bombs: ").append(getResourceCapacity(ResourceType.POWER_BOMB));
        return sb.toString();
    }
}
