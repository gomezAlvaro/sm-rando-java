package com.maprando.logic;

/**
 * Requirement for having at least a certain number of super missiles available.
 * Matches Rust Requirement::Supers(Numeric).
 */
public final record SupersRequirement(int count) implements Requirement {
    @Override
    public boolean isSatisfied(RequirementContext state) {
        return state.inventory().getResourceCapacity(
            com.maprando.model.ResourceType.SUPER_MISSILE
        ) >= count;
    }

    @Override
    public String toString() {
        return "Supers(%d)".formatted(count);
    }
}
