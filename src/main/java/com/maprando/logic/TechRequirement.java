package com.maprando.logic;

/**
 * Requirement that player can perform a specific tech.
 * Matches Rust Requirement::Tech(TechIdx).
 */
public final record TechRequirement(String techId) implements Requirement {
    @Override
    public boolean isSatisfied(RequirementContext state) {
        return state.inventory().hasTech(techId);
    }

    @Override
    public String toString() {
        return "Tech(%s)".formatted(techId);
    }
}
