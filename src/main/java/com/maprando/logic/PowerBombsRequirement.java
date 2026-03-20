package com.maprando.logic;

/**
 * Requirement for having at least a certain number of power bombs available.
 * Matches Rust Requirement::PowerBombs(Numeric).
 */
public final record PowerBombsRequirement(int count) implements Requirement {
    @Override
    public boolean isSatisfied(RequirementContext state) {
        return state.inventory().getResourceCapacity(
            com.maprando.model.ResourceType.POWER_BOMB
        ) >= count;
    }

    @Override
    public String toString() {
        return "PowerBombs(%d)".formatted(count);
    }
}
