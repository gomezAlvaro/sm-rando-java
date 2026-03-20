package com.maprando.logic;

import java.util.List;

/**
 * Logical OR of multiple requirements.
 * At least one sub-requirement must be satisfied.
 * Matches Rust Requirement::Or(Vec<Requirement>).
 */
public final record OrRequirement(List<Requirement> requirements) implements Requirement {
    public OrRequirement {
        // Defensive copy for immutability
        requirements = List.copyOf(requirements);
    }

    @Override
    public boolean isSatisfied(RequirementContext state) {
        return requirements.stream().anyMatch(req -> req.isSatisfied(state));
    }

    @Override
    public String toString() {
        return "Or(%s)".formatted(requirements);
    }
}
