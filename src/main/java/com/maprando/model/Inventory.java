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
    private final boolean[] availableTechs;
    private final TechRegistry techRegistry;

    /**
     * Creates a new empty inventory using the singleton registries.
     * Convenience constructor for migration period.
     */
    public Inventory() {
        this(ItemRegistry.getInstance(), TechRegistry.getInstance());
    }

    /**
     * Creates a new empty inventory.
     */
    public Inventory(ItemRegistry registry) {
        this(registry, TechRegistry.getInstance());
    }

    /**
     * Creates a new empty inventory with both registries.
     */
    public Inventory(ItemRegistry registry, TechRegistry techReg) {
        this.itemRegistry = registry;
        this.techRegistry = techReg;
        this.collectedItems = registry.createInventoryArray();
        this.availableTechs = techReg != null ? techReg.createTechArray() : new boolean[0];
        this.resourceCapacities = new HashMap<>();
        initializeBaseResources();
    }

    /**
     * Creates a copy of an existing inventory.
     */
    public Inventory(Inventory other) {
        this.itemRegistry = other.itemRegistry;
        this.techRegistry = other.techRegistry;
        this.collectedItems = new boolean[other.collectedItems.length];
        System.arraycopy(other.collectedItems, 0, this.collectedItems, 0, other.collectedItems.length);
        this.availableTechs = new boolean[other.availableTechs.length];
        System.arraycopy(other.availableTechs, 0, this.availableTechs, 0, other.availableTechs.length);
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

        // Auto-enable techs from item's enables list
        ItemDefinition def = itemRegistry.getByIndex(index);
        if (def != null) {
            // Auto-enable techs
            if (def.getEnables() != null) {
                for (String techId : def.getEnables()) {
                    enableTech(techId);
                }
            }

            // Auto-increase resource capacity if this is a tank
            if (def.getResourceType() != null && def.getCapacityIncrease() != null) {
                ResourceType resourceType = ResourceType.fromString(def.getResourceType());
                if (resourceType != null) {
                    increaseResourceCapacity(resourceType, def.getCapacityIncrease());
                }
            }
        }

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
        return canMorph() && hasItem("BOMBS");
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
        sb.append("  Techs: ").append(getTechCount()).append("/").append(techRegistry != null ? techRegistry.getTechCount() : 0).append("\n");
        sb.append("  Energy: ").append(getResourceCapacity(ResourceType.ENERGY)).append("\n");
        sb.append("  Missiles: ").append(getResourceCapacity(ResourceType.MISSILE)).append("\n");
        sb.append("  Super Missiles: ").append(getResourceCapacity(ResourceType.SUPER_MISSILE)).append("\n");
        sb.append("  Power Bombs: ").append(getResourceCapacity(ResourceType.POWER_BOMB));
        return sb.toString();
    }

    // Tech-related methods

    /**
     * Enables a tech by ID.
     * @return true if the tech was enabled (wasn't already enabled)
     */
    public boolean enableTech(String techId) {
        if (techRegistry == null) {
            return false;
        }
        TechDefinition tech = techRegistry.getById(techId);
        if (tech == null) {
            return false;
        }
        return enableTech(tech.getIndex());
    }

    /**
     * Enables a tech by index.
     * @return true if the tech was enabled (wasn't already enabled)
     */
    public boolean enableTech(int index) {
        if (techRegistry == null || index < 0 || index >= availableTechs.length) {
            return false;
        }
        if (availableTechs[index]) {
            return false; // Already enabled
        }
        availableTechs[index] = true;
        return true;
    }

    /**
     * Checks if the player has a specific tech by ID.
     */
    public boolean hasTech(String techId) {
        if (techRegistry == null) {
            return false;
        }
        return techRegistry.hasTech(availableTechs, techId);
    }

    /**
     * Checks if the player has a specific tech by index.
     */
    public boolean hasTech(int index) {
        if (techRegistry == null) {
            return false;
        }
        return techRegistry.hasTech(availableTechs, index);
    }

    /**
     * Gets the number of enabled techs.
     */
    public int getTechCount() {
        if (techRegistry == null) {
            return 0;
        }
        return techRegistry.getEnabledCount(availableTechs);
    }

    /**
     * Gets all enabled tech definitions.
     */
    public List<TechDefinition> getEnabledTechs() {
        if (techRegistry == null) {
            return List.of();
        }
        return techRegistry.getEnabledTechs(availableTechs);
    }

    /**
     * Gets all enabled tech IDs as a set.
     */
    public Set<String> getEnabledTechIds() {
        if (techRegistry == null) {
            return Set.of();
        }
        return techRegistry.getEnabledTechIds(availableTechs);
    }

    /**
     * Gets the tech registry used by this inventory.
     */
    public TechRegistry getTechRegistry() {
        return techRegistry;
    }

    /**
     * Gets the raw tech boolean array for direct access.
     * Use with caution - this is for performance-critical code.
     */
    public boolean[] getRawTechArray() {
        return availableTechs;
    }
}
