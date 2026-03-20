package com.maprando.logic;

/**
 * Requirement that is always satisfied.
 * Matches Rust Requirement::Free.
 */
public final record FreeRequirement() implements Requirement {
    @Override
    public boolean isSatisfied(RequirementContext state) {
        return true;
    }

    @Override
    public String toString() {
        return "Free";
    }
}
