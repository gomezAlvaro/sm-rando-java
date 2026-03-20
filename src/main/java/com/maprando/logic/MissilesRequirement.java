package com.maprando.logic;

/**
 * Requirement for having at least a certain number of missiles available.
 * Matches Rust Requirement::Missiles(Numeric).
 */
public final record MissilesRequirement(int count) implements Requirement {
    @Override
    public boolean isSatisfied(RequirementContext state) {
        return state.inventory().getResourceCapacity(
            com.maprando.model.ResourceType.MISSILE
        ) >= count;
    }

    @Override
    public String toString() {
        return "Missiles(%d)".formatted(count);
    }
}
