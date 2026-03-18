package com.maprando.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the complete state of the game at a point in time.
 * This includes inventory, current resource levels, and position information.
 *
 * GameState is designed to be cloneable for state restoration during traversal.
 */
public class GameState implements Cloneable {
    private final Inventory inventory;
    private final Map<ResourceType, ResourceLevel> resourceLevels;
    private final ItemRegistry itemRegistry;
    private String currentNode;
    private int energy;

    /**
     * Creates a new game state with default starting inventory using the singleton ItemRegistry.
     * Convenience constructor for migration period.
     */
    public GameState() {
        this(ItemRegistry.getInstance());
    }

    /**
     * Creates a new game state with default starting inventory.
     */
    public GameState(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        this.inventory = new Inventory(itemRegistry);
        this.resourceLevels = new EnumMap<>(ResourceType.class);
        this.energy = ResourceType.ENERGY.getBaseAmount();
        initializeResourceLevels();
    }

    /**
     * Creates a game state with the specified starting inventory.
     */
    public GameState(ItemRegistry itemRegistry, Inventory startingInventory) {
        this.itemRegistry = itemRegistry;
        this.inventory = new Inventory(startingInventory);
        this.resourceLevels = new EnumMap<>(ResourceType.class);
        this.energy = ResourceType.ENERGY.getBaseAmount();
        initializeResourceLevels();
    }

    private void initializeResourceLevels() {
        for (ResourceType type : ResourceType.values()) {
            resourceLevels.put(type, inventory.createResourceLevel(type));
        }
    }

    /**
     * Creates a copy of an existing game state.
     */
    private GameState(GameState other) {
        this.itemRegistry = other.itemRegistry;
        this.inventory = other.inventory.copy();
        this.resourceLevels = new EnumMap<>(ResourceType.class);
        for (Map.Entry<ResourceType, ResourceLevel> entry : other.resourceLevels.entrySet()) {
            this.resourceLevels.put(entry.getKey(), entry.getValue());
        }
        this.currentNode = other.currentNode;
        this.energy = other.energy;
    }

    /**
     * Gets the player's inventory.
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Gets the item registry used by this game state.
     */
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    /**
     * Gets the current resource level for a type.
     */
    public ResourceLevel getResourceLevel(ResourceType type) {
        return resourceLevels.get(type);
    }

    /**
     * Gets the player's current energy (health).
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Sets the player's current energy.
     */
    public void setEnergy(int energy) {
        int maxEnergy = inventory.getResourceCapacity(ResourceType.ENERGY);
        this.energy = Math.max(0, Math.min(maxEnergy, energy));
    }

    /**
     * Adds energy to the player (healing).
     */
    public void addEnergy(int amount) {
        setEnergy(energy + amount);
    }

    /**
     * Removes energy from the player (damage).
     * Returns true if the player survived (energy > 0).
     */
    public boolean takeDamage(int damage) {
        energy = Math.max(0, energy - damage);
        return energy > 0;
    }

    /**
     * Gets the current node/room the player is in.
     */
    public String getCurrentNode() {
        return currentNode;
    }

    /**
     * Sets the current node/room.
     */
    public void setCurrentNode(String node) {
        this.currentNode = node;
    }

    /**
     * Collects an item, updating inventory and resource capacities as needed.
     */
    public void collectItem(String itemId) {
        inventory.addItem(itemId);

        // Update resource capacities for relevant items
        switch (itemId) {
            case "MISSILE_TANK" -> inventory.increaseResourceCapacity(ResourceType.MISSILE, 5);
            case "SUPER_MISSILE_TANK" -> inventory.increaseResourceCapacity(ResourceType.SUPER_MISSILE, 5);
            case "POWER_BOMB_TANK" -> inventory.increaseResourceCapacity(ResourceType.POWER_BOMB, 5);
            case "ENERGY_TANK" -> inventory.increaseResourceCapacity(ResourceType.ENERGY, 100);
        }
    }

    /**
     * Checks if the player has enough of a resource available.
     */
    public boolean hasResource(ResourceType type, int amount) {
        ResourceLevel level = resourceLevels.get(type);
        return level != null && level.hasEnough(amount);
    }

    /**
     * Consumes a specified amount of a resource.
     * Returns true if the resource was available and consumed.
     */
    public boolean consumeResource(ResourceType type, int amount) {
        if (!hasResource(type, amount)) {
            return false;
        }
        ResourceLevel currentLevel = resourceLevels.get(type);
        resourceLevels.put(type, currentLevel.withConsumption(amount));
        return true;
    }

    /**
     * Creates a deep copy of this game state.
     */
    @Override
    public GameState clone() {
        return new GameState(this);
    }

    /**
     * Creates a new game state with the specified inventory items using the singleton ItemRegistry.
     * Convenience method for migration period.
     */
    public static GameState withItems(String... itemIds) {
        return withItems(ItemRegistry.getInstance(), itemIds);
    }

    /**
     * Creates a new game state with the specified inventory items.
     */
    public static GameState withItems(ItemRegistry registry, String... itemIds) {
        GameState state = new GameState(registry);
        for (String itemId : itemIds) {
            state.collectItem(itemId);
        }
        return state;
    }

    /**
     * Creates a new game state with starting resources (standard Super Metroid start).
     * Player starts with no major items - just base resources.
     * Convenience method using singleton ItemRegistry.
     */
    public static GameState standardStart() {
        return standardStart(ItemRegistry.getInstance());
    }

    /**
     * Creates a new game state with starting resources (standard Super Metroid start).
     * Player starts with no major items - just base resources.
     */
    public static GameState standardStart(ItemRegistry registry) {
        return new GameState(registry);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameState gameState = (GameState) o;
        return energy == gameState.energy &&
               Objects.equals(inventory, gameState.inventory) &&
               Objects.equals(currentNode, gameState.currentNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inventory, energy, currentNode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GameState:\n");
        sb.append("  Energy: ").append(energy).append("/").append(inventory.getResourceCapacity(ResourceType.ENERGY)).append("\n");
        sb.append("  Items: ").append(inventory.getItemCount()).append(" collected\n");
        sb.append("  Location: ").append(currentNode != null ? currentNode : "Unknown");
        return sb.toString();
    }
}
