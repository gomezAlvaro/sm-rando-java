package com.maprando.logic;

/**
 * Requirement that is never satisfied.
 * Matches Rust Requirement::Never.
 */
public final record NeverRequirement() implements Requirement {
    @Override
    public boolean isSatisfied(RequirementContext state) {
        return false;
    }

    @Override
    public String toString() {
        return "Never";
    }
}
