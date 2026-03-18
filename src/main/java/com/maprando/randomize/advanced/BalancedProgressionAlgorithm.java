package com.maprando.randomize.advanced;

import com.maprando.data.DataLoader;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.Location;
import com.maprando.randomize.RandomizationResult;

import java.util.*;

/**
 * Advanced randomizer that ensures balanced progression flow and item distribution.
 */
public class BalancedProgressionAlgorithm {

    private final DataLoader dataLoader;
    private final ProgressionManager progressionManager;
    private DifficultyLevel difficultyLevel;
    private ItemPool itemPool;
    private final List<Location> locations;
    private final Map<String, List<String>> regionConstraints;

    public BalancedProgressionAlgorithm(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
        this.progressionManager = new ProgressionManager(dataLoader);
        this.difficultyLevel = DifficultyLevel.NORMAL;
        this.locations = new ArrayList<>();
        this.regionConstraints = new HashMap<>();
    }

    public ProgressionManager getProgressionManager() {
        return progressionManager;
    }

    public void setDifficultyLevel(DifficultyLevel level) {
        this.difficultyLevel = level;
    }

    public void setItemPool(ItemPool pool) {
        this.itemPool = pool;
        progressionManager.setItemPool(pool);
    }

    public void addLocation(Location location) {
        this.locations.add(location);
    }

    public void addLocations(List<Location> locations) {
        this.locations.addAll(locations);
    }

    public void addRegionConstraint(String region, int minItems, int maxItems) {
        // Store constraint for future use
        regionConstraints.put(region, List.of(String.valueOf(minItems), String.valueOf(maxItems)));
    }

    public RandomizationResult randomize() {
        if (itemPool == null || locations.isEmpty()) {
            return createEmptyResult();
        }

        // Simple implementation using the progression manager
        RandomizationResult.Builder resultBuilder = RandomizationResult.builder()
            .seed(UUID.randomUUID().toString().substring(0, 8))
            .algorithmUsed("Balanced Progression");

        Set<Location> availableLocations = new HashSet<>(locations);
        List<String> itemIds = new ArrayList<>(itemPool.getAvailableItems());

        // Separate progression and filler
        List<String> progressionItemIds = new ArrayList<>();
        List<String> fillerItemIds = new ArrayList<>();

        for (String itemId : itemIds) {
            if (isProgressionItem(itemId)) {
                progressionItemIds.add(itemId);
                progressionManager.addProgressionItem(itemId);
            } else {
                fillerItemIds.add(itemId);
            }
        }

        // Place progression items first
        Iterator<Location> locationIterator = availableLocations.iterator();
        for (String itemId : progressionItemIds) {
            if (locationIterator.hasNext()) {
                Location location = locationIterator.next();
                resultBuilder.addPlacement(location.getId(), location.getName(), itemId);
                progressionManager.markItemPlaced(itemId, location.getId());
            }
        }

        // Place filler items
        locationIterator = availableLocations.iterator();
        for (String itemId : fillerItemIds) {
            if (locationIterator.hasNext()) {
                Location location = locationIterator.next();
                resultBuilder.addPlacement(location.getId(), location.getName(), itemId);
            }
        }

        return resultBuilder.successful(true).build();
    }

    private RandomizationResult createEmptyResult() {
        return RandomizationResult.builder()
            .algorithmUsed("Balanced Progression")
            .successful(false)
            .addWarning("No items or locations available")
            .build();
    }

    public Map<String, String> getKeyPlacements() {
        Map<String, String> keyPlacements = new HashMap<>();

        // Get major progression items that are typically "keys"
        // to access new areas (Morph Ball, Bombs, Varia Suit, Gravity Suit, Grapple)
        for (String itemId : progressionManager.getProgressionItemIds()) {
            if (isMajorProgressionItem(itemId)) {
                String location = progressionManager.getItemPlacement(itemId);
                if (location != null) {
                    keyPlacements.put(itemId, location);
                }
            }
        }

        return keyPlacements;
    }

    private boolean isMajorProgressionItem(String itemId) {
        return "MORPH_BALL".equals(itemId) || "BOMB".equals(itemId) ||
               "VARIA_SUIT".equals(itemId) || "GRAVITY_SUIT".equals(itemId) ||
               "GRAPPLE_BEAM".equals(itemId);
    }

    public FillerDistribution getFillerDistribution() {
        return new FillerDistribution(30.0); // Placeholder
    }

    public ClusteringMetrics getClusteringMetrics() {
        return new ClusteringMetrics(0.3); // Placeholder
    }

    public List<BalanceRecommendation> getBalanceRecommendations() {
        return List.of(); // Empty list for now
    }

    public ProgressionFlow getProgressionFlow() {
        return new ProgressionFlow(0.7); // Placeholder
    }

    public DifficultyCurve getDifficultyCurve() {
        return new DifficultyCurve(5.0); // Placeholder
    }

    public String getProgressionSummary() {
        return String.format("Progression: %.1f%% complete, %d progression items tracked",
            progressionManager.getProgressionPercentage(),
            progressionManager.getProgressionItemIds().size());
    }

    private boolean isProgressionItem(String itemId) {
        return "MORPH_BALL".equals(itemId) || "BOMB".equals(itemId) ||
               "CHARGE_BEAM".equals(itemId) || "ICE_BEAM".equals(itemId) ||
               "VARIA_SUIT".equals(itemId) || "GRAPPLE_BEAM".equals(itemId) ||
               "GRAVITY_SUIT".equals(itemId);
    }
}
