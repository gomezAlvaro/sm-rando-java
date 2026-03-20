package com.maprando.logic;

/**
 * Requirement that player has collected a specific item.
 * Matches Rust Requirement::Item(ItemId).
 */
public final record ItemRequirement(String itemId) implements Requirement {
    @Override
    public boolean isSatisfied(RequirementContext state) {
        return state.inventory().hasItem(itemId);
    }

    @Override
    public String toString() {
        return "Item(%s)".formatted(itemId);
    }
}
