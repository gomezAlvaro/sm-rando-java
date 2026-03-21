package com.maprando.randomize;

import com.maprando.data.DataLoader;
import com.maprando.data.model.DifficultyData;

import java.util.List;

/**
 * Factory for creating difficulty-adjusted item pools.
 * Applies difficulty preset settings to scale item quantities appropriately.
 */
public class ItemPoolFactory {

    private final DataLoader dataLoader;

    public ItemPoolFactory(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    /**
     * Creates a standard item pool adjusted for the given difficulty preset.
     *
     * @param difficultyId Difficulty preset ID (e.g., "casual", "normal", "hard")
     * @return Configured ItemPool
     */
    public ItemPool createPool(String difficultyId) {
        DifficultyData difficulty = dataLoader.getDifficultyPreset(difficultyId);
        if (difficulty == null) {
            // Default to normal if difficulty not found
            difficulty = dataLoader.getDifficultyPreset("normal");
        }

        if (difficulty == null) {
            // Fallback to unadjusted standard pool
            return ItemPool.createStandardPool();
        }

        return createPool(difficulty);
    }

    /**
     * Creates a standard item pool adjusted for the given difficulty preset.
     *
     * @param difficulty Difficulty preset configuration
     * @return Configured ItemPool
     */
    public ItemPool createPool(DifficultyData difficulty) {
        // Start with base standard pool
        ItemPool pool = ItemPool.createStandardPool();

        // Apply difficulty scaling
        applyDifficultyScaling(pool, difficulty);

        return pool;
    }

    /**
     * Applies difficulty scaling to an existing item pool.
     * This removes items proportionally based on difficulty settings.
     *
     * @param pool       The item pool to scale
     * @param difficulty Difficulty preset with scaling rates
     */
    private void applyDifficultyScaling(ItemPool pool, DifficultyData difficulty) {
        double progressionRate = difficulty.getItemPool().getProgressionRate();
        double fillerRate = difficulty.getItemPool().getFillerItemRate();

        // Scale progression items
        scaleProgressionItems(pool, progressionRate);

        // Scale filler items
        scaleFillerItems(pool, fillerRate);
    }

    /**
     * Scales progression items based on the given rate.
     * Removes items if rate < 1.0, ensures minimum of 1 for each progression item.
     *
     * @param pool The item pool
     * @param rate Scaling rate (0.5-1.0)
     */
    private void scaleProgressionItems(ItemPool pool, double rate) {
        if (rate >= 1.0) {
            return; // No scaling needed
        }

        // Get all progression items from the pool
        List<String> progressionItems = List.copyOf(pool.getProgressionItems());

        for (String itemId : progressionItems) {
            int currentCount = pool.getItemCount(itemId);
            if (currentCount <= 1) {
                // Always keep at least 1 of each progression item for beatability
                continue;
            }

            int newCount = (int) Math.max(1, Math.round(currentCount * rate));
            int itemsToRemove = currentCount - newCount;

            for (int i = 0; i < itemsToRemove; i++) {
                pool.removeItem(itemId);
            }
        }
    }

    /**
     * Scales filler items based on the given rate.
     * Can remove filler items entirely if rate is low.
     *
     * @param pool The item pool
     * @param rate Scaling rate (0.3-1.5)
     */
    private void scaleFillerItems(ItemPool pool, double rate) {
        if (rate >= 1.0) {
            return; // No scaling needed
        }

        // Get all filler items from the pool
        List<String> fillerItems = List.copyOf(pool.getFillerItems());

        for (String itemId : fillerItems) {
            int currentCount = pool.getItemCount(itemId);
            if (currentCount <= 0) {
                continue;
            }

            int newCount = (int) Math.max(0, Math.round(currentCount * rate));
            int itemsToRemove = currentCount - newCount;

            for (int i = 0; i < itemsToRemove; i++) {
                pool.removeItem(itemId);
            }
        }
    }

    /**
     * Creates an item pool with starting items pre-added.
     * These items are considered "already collected" and won't be placed in the world.
     *
     * @param difficultyId Difficulty preset ID
     * @return ItemPool with starting items noted (for documentation purposes)
     */
    public ItemPoolWithStartingItems createPoolWithStartingItems(String difficultyId) {
        DifficultyData difficulty = dataLoader.getDifficultyPreset(difficultyId);
        if (difficulty == null) {
            difficulty = dataLoader.getDifficultyPreset("normal");
        }

        ItemPool pool = createPool(difficulty);
        List<String> startingItems = difficulty.getStartingItems();

        return new ItemPoolWithStartingItems(pool, startingItems);
    }

    /**
     * Wrapper class for ItemPool that also tracks starting items.
     */
    public static class ItemPoolWithStartingItems {
        private final ItemPool pool;
        private final List<String> startingItems;

        public ItemPoolWithStartingItems(ItemPool pool, List<String> startingItems) {
            this.pool = pool;
            this.startingItems = startingItems;
        }

        public ItemPool getPool() {
            return pool;
        }

        public List<String> getStartingItems() {
            return startingItems;
        }
    }
}
