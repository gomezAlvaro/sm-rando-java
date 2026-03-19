package com.maprando.randomize;

import java.util.*;

/**
 * Represents the pool of items available for placement during randomization.
 * Items are categorized into progression items (required to complete the game)
 * and filler items (optional upgrades).
 */
public class ItemPool {
    private final Map<String, Integer> availableItems;
    private final Set<String> progressionItems;
    private final Set<String> fillerItems;

    /**
     * Creates a new item pool.
     */
    public ItemPool() {
        this.availableItems = new HashMap<>();
        this.progressionItems = new HashSet<>();
        this.fillerItems = new HashSet<>();
    }

    /**
     * Creates a copy of an existing item pool.
     */
    public ItemPool(ItemPool other) {
        this.availableItems = new HashMap<>(other.availableItems);
        this.progressionItems = new HashSet<>(other.progressionItems);
        this.fillerItems = new HashSet<>(other.fillerItems);
    }

    /**
     * Adds an item to the pool.
     *
     * @param itemId The ID of the item to add
     * @param count How many of this item to add
     * @param isProgression Whether this is a progression item
     */
    public void addItem(String itemId, int count, boolean isProgression) {
        availableItems.merge(itemId, count, Integer::sum);

        if (isProgression) {
            progressionItems.add(itemId);
        } else {
            fillerItems.add(itemId);
        }
    }

    /**
     * Adds a single item to the pool.
     */
    public void addItem(String itemId, boolean isProgression) {
        addItem(itemId, 1, isProgression);
    }

    /**
     * Removes an item from the pool.
     *
     * @return true if the item was available and removed
     */
    public boolean removeItem(String itemId) {
        Integer count = availableItems.get(itemId);
        if (count == null || count <= 0) {
            return false;
        }

        if (count == 1) {
            availableItems.remove(itemId);
            progressionItems.remove(itemId);
            fillerItems.remove(itemId);
        } else {
            availableItems.put(itemId, count - 1);
        }

        return true;
    }

    /**
     * Checks if an item is available in the pool.
     */
    public boolean hasItem(String itemId) {
        Integer count = availableItems.get(itemId);
        return count != null && count > 0;
    }

    /**
     * Gets the count of a specific item in the pool.
     */
    public int getItemCount(String itemId) {
        return availableItems.getOrDefault(itemId, 0);
    }

    /**
     * Gets all available items.
     */
    public Set<String> getAvailableItems() {
        return Collections.unmodifiableSet(availableItems.keySet());
    }

    /**
     * Gets all progression items.
     */
    public Set<String> getProgressionItems() {
        return Collections.unmodifiableSet(progressionItems);
    }

    /**
     * Gets all filler items.
     */
    public Set<String> getFillerItems() {
        return Collections.unmodifiableSet(fillerItems);
    }

    /**
     * Gets the total number of items in the pool.
     */
    public int getTotalItemCount() {
        return availableItems.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Gets all items expanded with their counts.
     * For example, if there are 3 ENERGY_TANKs, this will return 3 entries of "ENERGY_TANK".
     */
    public List<String> getAllItemsExpanded() {
        List<String> expanded = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : availableItems.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                expanded.add(entry.getKey());
            }
        }
        return expanded;
    }

    /**
     * Gets the number of unique item types in the pool.
     */
    public int getUniqueItemCount() {
        return availableItems.size();
    }

    /**
     * Checks if the pool is empty.
     */
    public boolean isEmpty() {
        return availableItems.isEmpty();
    }

    /**
     * Clears all items from the pool.
     */
    public void clear() {
        availableItems.clear();
        progressionItems.clear();
        fillerItems.clear();
    }

    /**
     * Creates a standard item pool for a full game randomization.
     * This includes all major progression items and a reasonable number of tanks.
     */
    public static ItemPool createStandardPool() {
        ItemPool pool = new ItemPool();

        // Progression items (one each)
        pool.addItem("MORPH_BALL", 1, true);
        pool.addItem("BOMB", 1, true);
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ICE_BEAM", 1, true);
        pool.addItem("VARIA_SUIT", 1, true);
        pool.addItem("GRAVITY_SUIT", 1, true);
        pool.addItem("SPEED_BOOSTER", 1, true);
        pool.addItem("SPACE_JUMP", 1, true);
        pool.addItem("SCREW_ATTACK", 1, true);
        pool.addItem("GRAPPLE_BEAM", 1, true);

        // Beams (one of each for variety)
        pool.addItem("WAVE_BEAM", 1, false);
        pool.addItem("SPAZER_BEAM", 1, false);
        pool.addItem("PLASMA_BEAM", 1, false);

        // Morph ball upgrades
        pool.addItem("SPRING_BALL", 1, false);
        pool.addItem("POWER_BOMB", 1, false);

        // Utility
        pool.addItem("XRAY_SCOPE", 1, false);
        pool.addItem("HI_JUMP_BOOTS", 1, false);

        // Tanks (multiple for gameplay)
        pool.addItem("ENERGY_TANK", 8, false);
        pool.addItem("MISSILE_TANK", 10, false);
        pool.addItem("SUPER_MISSILE_TANK", 8, false);
        pool.addItem("POWER_BOMB_TANK", 6, false);

        return pool;
    }

    /**
     * Creates a minimal item pool for testing or beginner randomization.
     */
    public static ItemPool createMinimalPool() {
        ItemPool pool = new ItemPool();

        // Core progression items only
        pool.addItem("MORPH_BALL", 1, true);
        pool.addItem("BOMB", 1, true);
        pool.addItem("CHARGE_BEAM", 1, true);
        pool.addItem("ICE_BEAM", 1, true);
        pool.addItem("VARIA_SUIT", 1, true);
        pool.addItem("GRAVITY_SUIT", 1, true);
        pool.addItem("GRAPPLE_BEAM", 1, true);

        // Minimal tanks
        pool.addItem("ENERGY_TANK", 4, false);
        pool.addItem("MISSILE_TANK", 5, false);

        return pool;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ItemPool:\n");
        sb.append("  Total items: ").append(getTotalItemCount()).append("\n");
        sb.append("  Unique items: ").append(getUniqueItemCount()).append("\n");
        sb.append("  Progression items: ").append(progressionItems.size()).append("\n");
        sb.append("  Filler items: ").append(fillerItems.size()).append("\n");
        return sb.toString();
    }
}
