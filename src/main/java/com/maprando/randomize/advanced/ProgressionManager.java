package com.maprando.randomize.advanced;

import com.maprando.data.DataLoader;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.Location;

import java.util.*;

/**
 * Tracks progression item distribution across the game.
 */
public class ProgressionManager {

    private final DataLoader dataLoader;
    private final Set<String> progressionItemIds;
    private ItemPool itemPool;
    private final Map<String, String> itemPlacements; // itemId -> locationId

    public ProgressionManager(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
        this.progressionItemIds = new HashSet<>();
        this.itemPlacements = new HashMap<>();
    }

    public Set<String> getProgressionItemIds() {
        return new HashSet<>(progressionItemIds);
    }

    public void addProgressionItem(String itemId) {
        progressionItemIds.add(itemId);
    }

    public ItemPool getItemPool() {
        return itemPool;
    }

    public void setItemPool(ItemPool pool) {
        this.itemPool = pool;
    }

    public void markItemPlaced(String itemId, String locationId) {
        itemPlacements.put(itemId, locationId);
    }

    public String getItemPlacement(String itemId) {
        return itemPlacements.get(itemId);
    }

    public Set<String> getUnplacedItemIds() {
        Set<String> unplaced = new HashSet<>(progressionItemIds);
        unplaced.removeAll(itemPlacements.keySet());
        return unplaced;
    }

    public double getProgressionPercentage() {
        if (progressionItemIds.isEmpty()) {
            return 0.0;
        }
        return ((double) itemPlacements.size() / progressionItemIds.size()) * 100.0;
    }

    public ProgressionDistribution distributeProgression(List<Location> locations) {
        ProgressionDistribution distribution = new ProgressionDistribution(
            getProgressionPercentage(),
            getProgressionPercentage() * 0.8,
            getProgressionPercentage() * 0.6
        );

        // Populate placements map with actual item placements
        for (Map.Entry<String, String> entry : itemPlacements.entrySet()) {
            distribution.addPlacement(entry.getKey(), entry.getValue());
        }

        return distribution;
    }

    public void rollbackPlacement(String itemId) {
        itemPlacements.remove(itemId);
    }

    public void reset() {
        progressionItemIds.clear();
        itemPlacements.clear();
        itemPool = null;
    }
}
