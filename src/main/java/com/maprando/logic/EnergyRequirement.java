package com.maprando.logic;

/**
 * Requirement for having at least a certain amount of energy available.
 * Matches Rust Requirement::Energy(Numeric).
 */
public final record EnergyRequirement(int amount) implements Requirement {
    @Override
    public boolean isSatisfied(RequirementContext state) {
        return state.inventory().getResourceCapacity(
            com.maprando.model.ResourceType.ENERGY
        ) >= amount;
    }

    @Override
    public String toString() {
        return "Energy(%d)".formatted(amount);
    }
}
