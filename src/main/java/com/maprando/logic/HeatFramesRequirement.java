package com.maprando.logic;

/**
 * Requirement for surviving heat damage for a number of frames.
 * Used to check if player can survive heated rooms.
 * Matches Rust Requirement::HeatFrames(Numeric).
 */
public final record HeatFramesRequirement(int frames) implements Requirement {
    @Override
    public boolean isSatisfied(RequirementContext state) {
        // Heat does 1 damage per frame (3 damage per frame without Varia suit)
        // Player must have enough energy to survive
        // TODO: Implement proper heat damage calculation
        return state.inventory().getResourceCapacity(
            com.maprando.model.ResourceType.ENERGY
        ) > frames / 3; // Simplified check
    }

    @Override
    public String toString() {
        return "HeatFrames(%d)".formatted(frames);
    }
}
