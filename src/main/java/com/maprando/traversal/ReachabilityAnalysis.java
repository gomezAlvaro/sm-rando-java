package com.maprando.traversal;

import com.maprando.data.DataLoader;
import com.maprando.data.model.LocationData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes which locations are reachable given current player items and capabilities.
 * Uses the game graph and traversal state to determine accessibility.
 */
public class ReachabilityAnalysis {

    private final DataLoader dataLoader;
    private final TraversalState currentState;
    private final GameGraph gameGraph;

    public ReachabilityAnalysis(DataLoader dataLoader, TraversalState initialState) {
        this.dataLoader = dataLoader;
        this.currentState = initialState;
        this.gameGraph = new GameGraph(dataLoader);
    }

    public TraversalState getCurrentState() {
        return currentState;
    }

    /**
     * Get all locations that are currently reachable.
     */
    public Set<String> getReachableLocations() {
        Set<String> reachable = new HashSet<>();
        LocationData locationData = dataLoader.getLocationData();

        for (LocationData.LocationDefinition locationDef : locationData.getLocations()) {
            if (isLocationReachable(locationDef)) {
                reachable.add(locationDef.getId());
            }
        }

        return reachable;
    }

    /**
     * Get all locations that are currently NOT reachable.
     */
    public Set<String> getUnreachableLocations() {
        Set<String> allLocations = dataLoader.getLocationData().getLocations().stream()
                .map(LocationData.LocationDefinition::getId)
                .collect(Collectors.toSet());

        Set<String> reachable = getReachableLocations();
        allLocations.removeAll(reachable);

        return allLocations;
    }

    /**
     * Check if a specific location is reachable.
     */
    public boolean isLocationReachable(LocationData.LocationDefinition locationDef) {
        // Check if location has requirements
        List<String> requirements = locationDef.getRequirements();
        if (requirements == null || requirements.isEmpty()) {
            return true; // No requirements means always reachable
        }

        // Check if all requirements are satisfied
        for (String requirement : requirements) {
            if (!currentState.canSatisfyRequirement(requirement)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the percentage of locations that are reachable.
     */
    public Double getReachablePercentage() {
        int totalLocations = dataLoader.getLocationData().getLocations().size();
        int reachableCount = getReachableLocations().size();

        if (totalLocations == 0) {
            return 0.0;
        }

        return (double) reachableCount / totalLocations * 100.0;
    }

    /**
     * Get a summary of reachable locations.
     */
    public String getReachabilitySummary() {
        Set<String> reachable = getReachableLocations();
        Set<String> unreachable = getUnreachableLocations();

        return String.format("Reachable: %d/%d locations (%.1f%%), Unreachable: %d",
                reachable.size(),
                dataLoader.getLocationData().getLocations().size(),
                getReachablePercentage(),
                unreachable.size());
    }

    /**
     * Get locations that became newly reachable compared to previous state.
     */
    public Set<String> getNewlyReachableLocations(Set<String> previouslyReachable) {
        Set<String> currentlyReachable = getReachableLocations();
        currentlyReachable.removeAll(previouslyReachable);
        return currentlyReachable;
    }

    /**
     * Check if all locations are reachable.
     */
    public boolean areAllLocationsReachable() {
        return getUnreachableLocations().isEmpty();
    }

    /**
     * Get reachable locations by region.
     */
    public Map<String, Set<String>> getReachableLocationsByRegion() {
        Map<String, Set<String>> byRegion = new HashMap<>();
        LocationData locationData = dataLoader.getLocationData();

        for (LocationData.LocationDefinition locationDef : locationData.getLocations()) {
            if (isLocationReachable(locationDef)) {
                String region = locationDef.getRegion();
                byRegion.computeIfAbsent(region, k -> new HashSet<>())
                        .add(locationDef.getId());
            }
        }

        return byRegion;
    }

    /**
     * Get early game locations that are reachable.
     */
    public Set<String> getReachableEarlyGameLocations() {
        return dataLoader.getLocationData().getLocations().stream()
                .filter(LocationData.LocationDefinition::isEarlyGame)
                .map(LocationData.LocationDefinition::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Get boss locations that are reachable.
     */
    public Set<String> getReachableBossLocations() {
        return dataLoader.getLocationData().getLocations().stream()
                .filter(LocationData.LocationDefinition::isBoss)
                .filter(this::isLocationReachable)
                .map(LocationData.LocationDefinition::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Analyze which items would unlock new locations.
     */
    public Map<String, Set<String>> analyzeItemUnlockPotential() {
        Map<String, Set<String>> unlockMap = new HashMap<>();

        // This is a simplified analysis
        // A full implementation would simulate collecting each item
        // and checking which new locations become reachable

        return unlockMap;
    }

    /**
     * Get locations that require specific items.
     */
    public Set<String> getLocationsRequiringItem(String itemRequirement) {
        return dataLoader.getLocationData().getLocations().stream()
                .filter(loc -> loc.getRequirements() != null &&
                        loc.getRequirements().contains(itemRequirement))
                .filter(this::isLocationReachable)
                .map(LocationData.LocationDefinition::getId)
                .collect(Collectors.toSet());
    }
}