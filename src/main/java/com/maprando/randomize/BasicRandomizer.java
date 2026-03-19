package com.maprando.randomize;

import com.maprando.model.GameState;
import com.maprando.logic.RequirementChecker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A basic item randomizer that implements a simple progression-based placement algorithm.
 * This is a proof-of-concept implementation demonstrating core randomization concepts.
 */
public class BasicRandomizer {
    private final Random random;
    private final String seed;
    private final List<Location> locations;
    private final ItemPool itemPool;
    private final List<String> warnings;

    /**
     * Creates a new randomizer with the specified seed.
     */
    public BasicRandomizer(String seed) {
        this.seed = seed != null ? seed : UUID.randomUUID().toString();
        this.random = new Random(this.seed.hashCode());
        this.locations = new ArrayList<>();
        this.itemPool = new ItemPool();
        this.warnings = new ArrayList<>();
    }

    /**
     * Adds a location to the randomization.
     */
    public void addLocation(Location location) {
        locations.add(location);
    }

    /**
     * Sets the item pool to use for randomization.
     */
    public void setItemPool(ItemPool pool) {
        // Clear existing items by removing all
        for (String itemId : itemPool.getAvailableItems()) {
            int count = itemPool.getItemCount(itemId);
            for (int i = 0; i < count; i++) {
                itemPool.removeItem(itemId);
            }
        }

        // Copy items from pool
        for (String itemId : pool.getAvailableItems()) {
            int count = pool.getItemCount(itemId);
            boolean isProgression = pool.getProgressionItems().contains(itemId);
            for (int i = 0; i < count; i++) {
                itemPool.addItem(itemId, isProgression);
            }
        }
    }

    /**
     * Runs the randomization algorithm.
     *
     * @return The randomization result
     */
    public RandomizationResult randomize() {
        warnings.clear();

        RandomizationResult.Builder resultBuilder = RandomizationResult.builder()
                .seed(seed)
                .algorithmUsed("Basic Progression Randomizer");

        // Shuffle locations for randomness
        List<Location> shuffledLocations = new ArrayList<>(locations);
        Collections.shuffle(shuffledLocations, random);

        // Separate locations into early (no requirements) and late (has requirements)
        List<Location> earlyLocations = shuffledLocations.stream()
                .filter(loc -> !loc.hasRequirements())
                .collect(Collectors.toList());

        List<Location> lateLocations = shuffledLocations.stream()
                .filter(Location::hasRequirements)
                .collect(Collectors.toList());

        // Place progression items in early locations first
        List<String> progressionItems = new ArrayList<>();
        for (String itemId : itemPool.getProgressionItems()) {
            int count = itemPool.getItemCount(itemId);
            for (int i = 0; i < count; i++) {
                progressionItems.add(itemId);
            }
        }
        Collections.shuffle(progressionItems, random);

        // Track which locations we've already placed items in
        Set<String> placedLocationIds = new HashSet<>();

        for (Location location : earlyLocations) {
            if (progressionItems.isEmpty()) break;

            String itemId = progressionItems.remove(0);
            resultBuilder.addPlacement(location.getId(), location.getName(), itemId);
            placedLocationIds.add(location.getId()); // Track location during first loop
            itemPool.removeItem(itemId);
        }

        // Place remaining items (progression and filler) in remaining locations
        List<String> allItemIds = itemPool.getAllItemsExpanded();
        Collections.shuffle(allItemIds, random);

        List<Location> remainingLocations = new ArrayList<>();
        remainingLocations.addAll(lateLocations);
        remainingLocations.addAll(earlyLocations.stream()
                .filter(loc -> !placedLocationIds.contains(loc.getId()))
                .collect(Collectors.toList()));

        for (Location location : remainingLocations) {
            if (allItemIds.isEmpty()) break;

            String itemId = allItemIds.remove(0);
            resultBuilder.addPlacement(location.getId(), location.getName(), itemId);
        }

        // Add warnings if items remain
        if (!allItemIds.isEmpty()) {
            warnings.add(allItemIds.size() + " items could not be placed (not enough locations)");
        }

        return resultBuilder
                .warnings(warnings)
                .successful(allItemIds.isEmpty())
                .build();
    }

    /**
     * Validates that the randomization is beatable.
     * A simplified check for the proof-of-concept.
     */
    public boolean validateBeatable(RandomizationResult result) {
        // For a real implementation, this would perform reachability analysis
        // For now, just check that we have at least some progression items placed
        Set<String> placedItems = result.getPlacedItemIds();
        long progressionCount = placedItems.stream()
                .filter(itemPool.getProgressionItems()::contains)
                .count();

        return progressionCount >= 3; // Need at least 3 progression items
    }

    /**
     * Gets a simplified quality score for the randomization.
     */
    public double getQualityScore(RandomizationResult result) {
        int placementCount = result.getPlacementCount();
        int locationCount = locations.size();

        if (locationCount == 0) return 0.0;

        double placementRatio = (double) placementCount / locationCount;
        double warningPenalty = result.getWarnings().size() * 0.1;

        return Math.max(0.0, placementRatio - warningPenalty);
    }

    /**
     * Gets the random seed used.
     */
    public String getSeed() {
        return seed;
    }

    /**
     * Gets all warnings.
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
}
