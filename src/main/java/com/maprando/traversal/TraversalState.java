package com.maprando.traversal;

import com.maprando.model.GameState;
import com.maprando.model.ResourceType;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks player state during graph traversal analysis.
 * Unlike GameState which tracks actual gameplay, TraversalState
 * is used for analysis and simulation of reachability.
 */
public class TraversalState {

    private final GameState baseGameState;
    private final Set<String> collectedItemIds;
    private final Set<String> visitedLocations;
    private final Set<String> availableTech;  // Tech abilities available to player

    public TraversalState(GameState baseGameState) {
        this.baseGameState = baseGameState;
        this.collectedItemIds = new HashSet<>();
        this.visitedLocations = new HashSet<>();
        this.availableTech = new HashSet<>();
    }

    /**
     * Create a copy of this traversal state.
     */
    public TraversalState clone() {
        TraversalState cloned = new TraversalState(this.baseGameState);
        cloned.collectedItemIds.addAll(this.collectedItemIds);
        cloned.visitedLocations.addAll(this.visitedLocations);
        cloned.availableTech.addAll(this.availableTech);
        return cloned;
    }

    /**
     * Collect an item and update capabilities.
     */
    public void collectItem(String itemId) {
        collectedItemIds.add(itemId);
    }

    /**
     * Check if player can move (always true unless completely stuck).
     */
    public boolean canMove() {
        return true;
    }

    /**
     * Check if player can morph (has Morph Ball).
     */
    public boolean canMorph() {
        return collectedItemIds.contains("MORPH_BALL");
    }

    /**
     * Check if player can survive heat (has Varia Suit).
     */
    public boolean canSurviveHeat() {
        return collectedItemIds.contains("VARIA_SUIT");
    }

    /**
     * Check if player has Grapple Beam.
     */
    public boolean hasGrapple() {
        return collectedItemIds.contains("GRAPPLE_BEAM");
    }

    /**
     * Check if player can swim in water (has Gravity Suit).
     */
    public boolean canSwimWater() {
        return collectedItemIds.contains("GRAVITY_SUIT");
    }

    /**
     * Check if player can use Speed Booster.
     */
    public boolean canUseSpeedBooster() {
        return collectedItemIds.contains("SPEED_BOOSTER");
    }

    /**
     * Check if player can Space Jump.
     */
    public boolean canSpaceJump() {
        return collectedItemIds.contains("SPACE_JUMP");
    }

    /**
     * Check if player can Screw Attack.
     */
    public boolean canScrewAttack() {
        return collectedItemIds.contains("SCREW_ATTACK");
    }

    /**
     * Check if player can use Hi-Jump Boots.
     */
    public boolean canHiJump() {
        return collectedItemIds.contains("HI_JUMP_BOOTS");
    }

    /**
     * Check if player can shinespark.
     */
    public boolean canShinespark() {
        return collectedItemIds.contains("SPEED_BOOSTER");
    }

    /**
     * Check if player can place bombs (Morph + Bombs).
     */
    public boolean canPlaceBombs() {
        return canMorph() && collectedItemIds.contains("BOMB");
    }

    /**
     * Check if player can use power bombs (Morph + at least 1 PB).
     */
    public boolean canUsePowerBombs() {
        return canMorph() && baseGameState.getInventory().getResourceCapacity(
            com.maprando.model.ResourceType.POWER_BOMB) > 0;
    }

    /**
     * Check if player has bombs (Bomb collected).
     */
    public boolean hasBombs() {
        return collectedItemIds.contains("BOMB");
    }

    /**
     * Check if player has Ice Beam.
     */
    public boolean hasIceBeam() {
        return collectedItemIds.contains("ICE_BEAM");
    }

    /**
     * Check if player can satisfy a specific requirement.
     */
    public boolean canSatisfyRequirement(String requirement) {
        return switch (requirement) {
            case "can_morph" -> canMorph();
            case "can_survive_heat" -> canSurviveHeat();
            case "has_grapple" -> hasGrapple();
            case "can_grapple" -> hasGrapple();
            case "has_bombs" -> hasBombs();
            case "can_swim_water", "can_swim_lava" -> canSwimWater();
            case "can_survive_lava" -> canSwimWater();
            case "can_speed_booster" -> canUseSpeedBooster();
            case "can_hijump" -> canHiJump();
            case "can_space_jump" -> canSpaceJump();
            case "can_shinespark" -> canShinespark();
            case "can_screw_attack" -> canScrewAttack();
            case "can_place_bombs" -> canPlaceBombs();
            case "can_use_power_bombs" -> canUsePowerBombs();
            case "has_ice_beam" -> hasIceBeam();
            // Tech-based requirements
            case "can_walljump" -> hasTech("can_walljump");
            case "can_mid_air_morph" -> hasTech("can_mid_air_morph");
            case "can_horizontal_shinespark" -> hasTech("can_horizontal_shinespark");
            case "can_suitless_lava_dive" -> hasTech("can_suitless_lava_dive");
            case "can_kago" -> hasTech("can_kago");
            case "can_moonfall" -> hasTech("can_moonfall");
            case "can_off_screen_super_shot" -> hasTech("can_off_screen_super_shot");
            case "can_hyper_gate_shot" -> hasTech("can_hyper_gate_shot");
            case "can_hero_shot" -> hasTech("can_hero_shot");
            default -> false;
        };
    }

    /**
     * Check if player can satisfy all specified requirements.
     */
    public boolean canSatisfyAllRequirements(String... requirements) {
        for (String requirement : requirements) {
            if (!canSatisfyRequirement(requirement)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Mark a location as visited.
     */
    public void markLocationVisited(String locationId) {
        visitedLocations.add(locationId);
    }

    /**
     * Check if a location has been visited.
     */
    public boolean hasVisitedLocation(String locationId) {
        return visitedLocations.contains(locationId);
    }

    /**
     * Get all visited locations.
     */
    public Set<String> getVisitedLocations() {
        return new HashSet<>(visitedLocations);
    }

    /**
     * Check if player has a specific resource amount.
     */
    public boolean hasResource(ResourceType resourceType, int amount) {
        return baseGameState.getResourceLevel(resourceType).getRemaining() >= amount;
    }

    /**
     * Get the base game state.
     */
    public GameState getGameState() {
        return baseGameState;
    }

    /**
     * Get collected item IDs.
     */
    public Set<String> getCollectedItemIds() {
        return new HashSet<>(collectedItemIds);
    }

    /**
     * Get a capability summary string.
     */
    public String getCapabilitySummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Capabilities: ");

        if (canMorph()) summary.append("Morph ");
        if (canSurviveHeat()) summary.append("Heat-Protection ");
        if (hasGrapple()) summary.append("Grapple ");
        if (hasBombs()) summary.append("Bombs ");
        if (canSwimWater()) summary.append("Swim ");
        if (canUseSpeedBooster()) summary.append("Speed-Boost ");

        if (summary.length() == "Capabilities: ".length()) {
            summary.append("Basic movement only");
        }

        return summary.toString();
    }

    /**
     * Check if player has a specific tech ability.
     *
     * @param techId Tech identifier (e.g., "can_walljump", "can_shinespark")
     * @return true if tech is available
     */
    public boolean hasTech(String techId) {
        return availableTech.contains(techId);
    }

    /**
     * Add a tech ability to the player's available tech.
     *
     * @param techId Tech identifier
     */
    public void addTech(String techId) {
        availableTech.add(techId);
    }

    /**
     * Set tech abilities from a difficulty config.
     * Uses the existing DifficultyConfig.fromPreset() system.
     *
     * @param techAssumptions Tech level from difficulty preset
     *                         (beginner, intermediate, advanced, expert, nightmare)
     */
    public void setDifficultyTechLevel(String techAssumptions) {
        com.maprando.model.DifficultyConfig config =
            com.maprando.model.DifficultyConfig.fromPreset(techAssumptions);

        // Add all tech from the config
        for (String techId : config.tech) {
            addTech(techId);
        }
    }

    /**
     * Get all available tech abilities.
     */
    public Set<String> getAvailableTech() {
        return new HashSet<>(availableTech);
    }
}
