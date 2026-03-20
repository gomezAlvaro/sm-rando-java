package com.maprando.logic;

import java.util.List;

/**
 * Logical AND of multiple requirements.
 * All sub-requirements must be satisfied.
 * Matches Rust Requirement::And(Vec<Requirement>).
 */
public final record AndRequirement(List<Requirement> requirements) implements Requirement {
    public AndRequirement {
        // Defensive copy for immutability
        requirements = List.copyOf(requirements);
    }

    @Override
    public boolean isSatisfied(RequirementContext state) {
        return requirements.stream().allMatch(req -> req.isSatisfied(state));
    }

    @Override
    public String toString() {
        return "And(%s)".formatted(requirements);
    }
}
