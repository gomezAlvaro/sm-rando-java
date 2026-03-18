package com.maprando.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registry for all item definitions loaded from data.
 * This replaces the Item enum with a data-driven approach.
 *
 * Items are stored with indices for efficient boolean array tracking,
 * matching the original Rust MapRandomizer architecture.
 */
public class ItemRegistry {
    private final Map<String, ItemDefinition> itemsById;
    private final List<ItemDefinition> itemsByIndex;
    private int itemCount;

    // Singleton instance for convenience during migration
    private static ItemRegistry instance;

    /**
     * Gets the singleton registry instance.
     */
    public static ItemRegistry getInstance() {
        return instance;
    }

    /**
     * Sets the singleton registry instance.
     * Should be called by DataLoader after loading data.
     */
    public static void setInstance(ItemRegistry registry) {
        instance = registry;
    }

    /**
     * Creates a new empty registry.
     */
    public ItemRegistry() {
        this.itemsById = new HashMap<>();
        this.itemsByIndex = new ArrayList<>();
        this.itemCount = 0;
    }

    /**
     * Registers a new item definition.
     * @param definition The item definition to register
     * @throws IllegalArgumentException if an item with the same ID or index already exists
     */
    public void registerItem(ItemDefinition definition) {
        String id = definition.getId();
        int index = definition.getIndex();

        if (itemsById.containsKey(id)) {
            throw new IllegalArgumentException("Item with ID '" + id + "' already registered");
        }

        if (index >= itemsByIndex.size()) {
            // Expand list to accommodate this index
            while (itemsByIndex.size() <= index) {
                itemsByIndex.add(null);
            }
        } else if (itemsByIndex.get(index) != null) {
            throw new IllegalArgumentException("Item with index " + index + " already registered");
        }

        itemsById.put(id, definition);
        itemsByIndex.set(index, definition);
        itemCount = Math.max(itemCount, index + 1);
    }

    /**
     * Gets an item definition by its ID.
     * @return The item definition, or null if not found
     */
    public ItemDefinition getById(String id) {
        return itemsById.get(id);
    }

    /**
     * Gets an item definition by its index.
     * @return The item definition, or null if not found
     */
    public ItemDefinition getByIndex(int index) {
        if (index < 0 || index >= itemsByIndex.size()) {
            return null;
        }
        return itemsByIndex.get(index);
    }

    /**
     * Returns the total number of registered items.
     */
    public int getItemCount() {
        return itemCount;
    }

    /**
     * Returns all registered item definitions.
     */
    public Collection<ItemDefinition> getAllItems() {
        return Collections.unmodifiableCollection(itemsById.values());
    }

    /**
     * Creates a new boolean array for tracking collected items.
     * All values initialized to false (not collected).
     *
     * This matches the original Rust architecture:
     *   items: vec![false; game_data.item_isv.keys.len()]
     */
    public boolean[] createInventoryArray() {
        return new boolean[itemCount];
    }

    /**
     * Checks if an inventory array contains an item by index.
     */
    public boolean hasItem(boolean[] inventory, int index) {
        if (index < 0 || index >= inventory.length) {
            return false;
        }
        return inventory[index];
    }

    /**
     * Sets an item as collected in the inventory array.
     */
    public void collectItem(boolean[] inventory, int index) {
        if (index >= 0 && index < inventory.length) {
            inventory[index] = true;
        }
    }

    /**
     * Removes an item from the inventory array.
     */
    public void removeItem(boolean[] inventory, int index) {
        if (index >= 0 && index < inventory.length) {
            inventory[index] = false;
        }
    }

    /**
     * Checks if the inventory array contains an item by ID.
     */
    public boolean hasItemById(boolean[] inventory, String id) {
        ItemDefinition def = itemsById.get(id);
        if (def == null) {
            return false;
        }
        return hasItem(inventory, def.getIndex());
    }

    /**
     * Sets an item as collected in the inventory array by ID.
     */
    public void collectItemById(boolean[] inventory, String id) {
        ItemDefinition def = itemsById.get(id);
        if (def != null) {
            collectItem(inventory, def.getIndex());
        }
    }

    /**
     * Returns the number of items collected in the inventory array.
     */
    public int getCollectedCount(boolean[] inventory) {
        int count = 0;
        for (int i = 0; i < Math.min(inventory.length, itemCount); i++) {
            if (inventory[i]) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns all collected item definitions from an inventory array.
     */
    public List<ItemDefinition> getCollectedItems(boolean[] inventory) {
        List<ItemDefinition> collected = new ArrayList<>();
        for (int i = 0; i < Math.min(inventory.length, itemCount); i++) {
            if (inventory[i] && itemsByIndex.get(i) != null) {
                collected.add(itemsByIndex.get(i));
            }
        }
        return collected;
    }

    /**
     * Returns all collected item IDs as a set from an inventory array.
     */
    public Set<String> getCollectedItemIds(boolean[] inventory) {
        Set<String> collected = new HashSet<>();
        for (int i = 0; i < Math.min(inventory.length, itemCount); i++) {
            if (inventory[i] && itemsByIndex.get(i) != null) {
                collected.add(itemsByIndex.get(i).getId());
            }
        }
        return collected;
    }
}
