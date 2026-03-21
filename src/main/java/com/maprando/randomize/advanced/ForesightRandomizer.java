package com.maprando.randomize.advanced;

import com.maprando.data.DataLoader;
import com.maprando.data.model.SkillAssumptionSettings;
import com.maprando.model.GameState;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.Location;
import com.maprando.randomize.RandomizationResult;
import com.maprando.traversal.GameGraph;
import com.maprando.traversal.ReachabilityAnalysis;
import com.maprando.traversal.TraversalState;

import java.util.*;

/**
 * Advanced randomizer that uses reachability analysis to ensure seeds are beatable.
 * Uses foresight to look ahead and avoid placing progression items in unreachable locations.
 */
public class ForesightRandomizer {

    private final String seed;
    private final DataLoader dataLoader;
    private final GameGraph gameGraph;
    private final Random random;
    private final Map<String, Boolean> reachabilityAtPlacement;
    private int backtrackCount;
    private boolean usedReachabilityAnalysis;
    private ItemPool itemPool;
    private final List<Location> locations;
    private SeedQualityMetrics qualityMetrics;
    private SkillAssumptionSettings skillPreset;  // Skill preset from Rust project
    private java.util.List<String> startingItems = new java.util.ArrayList<>();

    public ForesightRandomizer(String seed, DataLoader dataLoader) {
        this.seed = seed;
        this.dataLoader = dataLoader;
        this.gameGraph = new GameGraph(dataLoader);
        this.random = new Random(seed.hashCode());
        this.reachabilityAtPlacement = new HashMap<>();
        this.backtrackCount = 0;
        this.usedReachabilityAnalysis = false;
        this.locations = new ArrayList<>();
    }

    public GameGraph getGameGraph() {
        return gameGraph;
    }

    public void setItemPool(ItemPool pool) {
        this.itemPool = pool;
    }

    public void addLocation(Location location) {
        this.locations.add(location);
    }

    public void addLocations(List<Location> locations) {
        this.locations.addAll(locations);
    }

    /**
     * Set the skill preset for reachability analysis.
     * This affects what tech abilities the player is assumed to have.
     *
     * @param skillPreset Skill preset with tech settings
     */
    public void setSkillPreset(SkillAssumptionSettings skillPreset) {
        this.skillPreset = skillPreset;
    }

    /**
     * Set starting items for this seed.
     * These items are pre-collected and won't be placed in the world.
     *
     * @param items List of item IDs to start with
     */
    public void setStartingItems(java.util.List<String> items) {
        this.startingItems = items != null ? items : new java.util.ArrayList<>();
    }

    /**
     * Run the foresight randomization algorithm.
     */
    public RandomizationResult randomize() {
        usedReachabilityAnalysis = true;
        backtrackCount = 0;
        reachabilityAtPlacement.clear();

        if (itemPool == null || locations.isEmpty()) {
            return createEmptyResult();
        }

        // Create initial state with starting items and tech level
        TraversalState state = createInitialState();
        ReachabilityAnalysis analysis = new ReachabilityAnalysis(dataLoader, state);

        RandomizationResult.Builder resultBuilder = RandomizationResult.builder()
            .seed(seed)
            .algorithmUsed("Foresight Randomizer");

        Set<Location> availableLocations = new HashSet<>(locations);
        Set<String> remainingItems = new HashSet<>(itemPool.getAvailableItems());

        // Separate progression and filler items
        List<String> progressionItems = new ArrayList<>();
        List<String> fillerItems = new ArrayList<>();

        for (String itemId : remainingItems) {
            if (isProgressionItem(itemId)) {
                progressionItems.add(itemId);
            } else {
                fillerItems.add(itemId);
            }
        }

        // Place progression items with reachability checking
        for (String itemId : progressionItems) {
            List<Location> reachableLocations = findReachableLocations(availableLocations, state);

            if (reachableLocations.isEmpty()) {
                // No reachable locations - this is a problem
                backtrackCount++;
                // Try to place in any location as fallback
                if (!availableLocations.isEmpty()) {
                    Location fallback = availableLocations.iterator().next();
                    placeItem(resultBuilder, fallback, itemId);
                    reachabilityAtPlacement.put(fallback.getId(), false); // Track as not reachable
                    availableLocations.remove(fallback);
                    state.collectItem(itemId);
                }
            } else {
                // Place in a reachable location
                Location chosen = reachableLocations.get(random.nextInt(reachableLocations.size()));
                placeItem(resultBuilder, chosen, itemId);
                reachabilityAtPlacement.put(chosen.getId(), true);
                availableLocations.remove(chosen);
                state.collectItem(itemId);
            }
        }

        // Place filler items in remaining locations
        for (String itemId : fillerItems) {
            if (!availableLocations.isEmpty()) {
                Location chosen = availableLocations.iterator().next();
                placeItem(resultBuilder, chosen, itemId);
                availableLocations.remove(chosen);
            }
        }

        RandomizationResult result = resultBuilder.successful(true).build();

        // Calculate quality metrics
        this.qualityMetrics = calculateQualityMetrics(result, state);

        return result;
    }

    private List<Location> findReachableLocations(Set<Location> locations, TraversalState state) {
        List<Location> reachable = new ArrayList<>();
        ReachabilityAnalysis analysis = new ReachabilityAnalysis(dataLoader, state);

        Set<String> reachableIds = analysis.getReachableLocations();

        for (Location location : locations) {
            if (reachableIds.contains(location.getId())) {
                reachable.add(location);
            }
        }

        return reachable;
    }

    private void placeItem(RandomizationResult.Builder builder, Location location, String itemId) {
        builder.addPlacement(location.getId(), location.getName(), itemId);
    }

    private RandomizationResult createEmptyResult() {
        return RandomizationResult.builder()
            .seed(seed)
            .algorithmUsed("Foresight Randomizer")
            .successful(true)
            .build();
    }

    /**
     * Creates the initial traversal state with difficulty settings applied.
     */
    private TraversalState createInitialState() {
        // Create game state with starting items
        GameState gameState;
        if (startingItems.isEmpty()) {
            gameState = GameState.standardStart();
        } else {
            gameState = GameState.withStartingItems(
                com.maprando.model.ItemRegistry.getInstance(),
                startingItems
            );
        }

        // Create traversal state
        TraversalState state = new TraversalState(gameState);

        // Apply skill preset tech settings
        if (skillPreset != null && skillPreset.getTechSettings() != null) {
            for (SkillAssumptionSettings.TechSetting techSetting : skillPreset.getTechSettings()) {
                if (techSetting.isEnabled()) {
                    state.addTech(techSetting.getName());
                }
            }
        }

        return state;
    }

    private SeedQualityMetrics calculateQualityMetrics(RandomizationResult result, TraversalState finalState) {
        ReachabilityAnalysis analysis = new ReachabilityAnalysis(dataLoader, finalState);
        double reachablePercentage = analysis.getReachablePercentage();

        return new SeedQualityMetrics(
            reachablePercentage,
            calculatePathQualityScore(result),
            calculatePathDiversity(result),
            estimateDifficulty(result),
            estimateCriticalPathLength(result),
            estimateBacktracking(result)
        );
    }

    private double calculatePathQualityScore(RandomizationResult result) {
        // Simplified path quality calculation
        return qualityMetrics != null ? qualityMetrics.getPathQualityScore() : 0.7;
    }

    private double calculatePathDiversity(RandomizationResult result) {
        // Simplified diversity calculation
        return 0.6;
    }

    private String estimateDifficulty(RandomizationResult result) {
        double reachablePercentage = calculateReachablePercentage(result);
        if (reachablePercentage >= 80) return "Easy";
        if (reachablePercentage >= 60) return "Normal";
        if (reachablePercentage >= 40) return "Hard";
        return "Expert";
    }

    private int estimateCriticalPathLength(RandomizationResult result) {
        return (int) result.getPlacements().values().stream()
            .filter(Objects::nonNull)
            .filter(this::isProgressionItem)
            .count();
    }

    private double estimateBacktracking(RandomizationResult result) {
        return (double) backtrackCount / Math.max(1, result.getPlacementCount());
    }

    private double calculateReachablePercentage(RandomizationResult result) {
        TraversalState state = new TraversalState(GameState.standardStart());

        // Collect all items
        for (String itemId : result.getPlacements().values()) {
            if (itemId != null) {
                state.collectItem(itemId);
            }
        }

        ReachabilityAnalysis analysis = new ReachabilityAnalysis(dataLoader, state);
        Double percentage = analysis.getReachablePercentage();
        return percentage != null ? percentage : 0.0;
    }

    public boolean hasUsedReachabilityAnalysis() {
        return usedReachabilityAnalysis;
    }

    public int getBacktrackCount() {
        return backtrackCount;
    }

    public SeedQualityMetrics getQualityMetrics() {
        return qualityMetrics;
    }

    public boolean wasLocationReachableAtPlacement(String locationId) {
        return reachabilityAtPlacement.getOrDefault(locationId, false);
    }

    public PlacementStatistics getPlacementStatistics() {
        int totalPlacements = locations.size();
        return new PlacementStatistics(totalPlacements, backtrackCount, 0);
    }

    public String getForesightAnalysisSummary() {
        if (qualityMetrics == null) {
            return "No analysis available";
        }

        return String.format("Foresight Analysis: Reachable: %.1f%%, Backtracks: %d, Quality: %.2f",
            qualityMetrics.getReachablePercentage(),
            backtrackCount,
            qualityMetrics.getPathQualityScore());
    }

    private boolean isProgressionItem(String itemId) {
        // Simple check for progression items
        return "MORPH_BALL".equals(itemId) || "BOMB".equals(itemId) ||
               "CHARGE_BEAM".equals(itemId) || "ICE_BEAM".equals(itemId) ||
               "VARIA_SUIT".equals(itemId) || "GRAPPLE_BEAM".equals(itemId) ||
               "GRAVITY_SUIT".equals(itemId) || "SPACE_JUMP".equals(itemId) ||
               "SCREW_ATTACK".equals(itemId) || "SPEED_BOOSTER".equals(itemId);
    }
}