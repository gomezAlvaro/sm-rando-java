package com.maprando.logic;

import java.util.List;

/**
 * Base interface for all requirements in the randomizer.
 * Matches Rust MapRandomizer Requirement enum architecture.
 *
 * Requirements can check:
 * - Items player has collected
 * - Techs player can perform
 * - Resources (energy, missiles, etc.)
 * - Complex logical compositions (And/Or)
 * - Environmental conditions (heat, lava, etc.)
 */
public sealed interface Requirement
        permits
        FreeRequirement,
        NeverRequirement,
        ItemRequirement,
        TechRequirement,
        AndRequirement,
        OrRequirement,
        HeatFramesRequirement,
        MissilesRequirement,
        SupersRequirement,
        PowerBombsRequirement,
        EnergyRequirement {

    /**
     * Check if this requirement is satisfied given the current game state.
     *
     * @param state The current game state
     * @return true if the requirement is satisfied
     */
    boolean isSatisfied(RequirementContext state);

    /**
     * Factory method to create an AND requirement.
     * Simplifies requirements: Never short-circuits to Never, Free is ignored.
     */
    static Requirement and(Requirement... requirements) {
        return and(List.of(requirements));
    }

    /**
     * Factory method to create an AND requirement.
     * Simplifies requirements: Never short-circuits to Never, Free is ignored.
     */
    static Requirement and(List<Requirement> requirements) {
        // Flatten nested And requirements
        List<Requirement> flattened = new java.util.ArrayList<>();
        for (Requirement req : requirements) {
            if (req instanceof NeverRequirement) {
                return new NeverRequirement(); // Never makes everything impossible
            } else if (req instanceof FreeRequirement) {
                // Skip Free requirements
                continue;
            } else if (req instanceof AndRequirement andReq) {
                flattened.addAll(andReq.requirements());
            } else {
                flattened.add(req);
            }
        }

        if (flattened.isEmpty()) {
            return new FreeRequirement();
        } else if (flattened.size() == 1) {
            return flattened.get(0);
        } else {
            return new AndRequirement(flattened);
        }
    }

    /**
     * Factory method to create an OR requirement.
     * Simplifies requirements: Free short-circuits to Free, Never is ignored.
     */
    static Requirement or(Requirement... requirements) {
        return or(List.of(requirements));
    }

    /**
     * Factory method to create an OR requirement.
     * Simplifies requirements: Free short-circuits to Free, Never is ignored.
     */
    static Requirement or(List<Requirement> requirements) {
        // Flatten nested Or requirements
        List<Requirement> flattened = new java.util.ArrayList<>();
        for (Requirement req : requirements) {
            if (req instanceof FreeRequirement) {
                return new FreeRequirement(); // Free makes everything possible
            } else if (req instanceof NeverRequirement) {
                // Skip Never requirements
                continue;
            } else if (req instanceof OrRequirement orReq) {
                flattened.addAll(orReq.requirements());
            } else {
                flattened.add(req);
            }
        }

        if (flattened.isEmpty()) {
            return new NeverRequirement();
        } else if (flattened.size() == 1) {
            return flattened.get(0);
        } else {
            return new OrRequirement(flattened);
        }
    }

    /**
     * Requirement that is always satisfied.
     */
    static Requirement free() {
        return new FreeRequirement();
    }

    /**
     * Requirement that is never satisfied.
     */
    static Requirement never() {
        return new NeverRequirement();
    }

    /**
     * Requirement that player has a specific item.
     */
    static Requirement item(String itemId) {
        return new ItemRequirement(itemId);
    }

    /**
     * Requirement that player can perform a specific tech.
     */
    static Requirement tech(String techId) {
        return new TechRequirement(techId);
    }

    /**
     * Requirement for surviving heat damage for a number of frames.
     */
    static Requirement heatFrames(int frames) {
        return new HeatFramesRequirement(frames);
    }

    /**
     * Requirement for having at least a certain number of missiles available.
     */
    static Requirement missiles(int count) {
        return new MissilesRequirement(count);
    }

    /**
     * Requirement for having at least a certain number of super missiles available.
     */
    static Requirement supers(int count) {
        return new SupersRequirement(count);
    }

    /**
     * Requirement for having at least a certain number of power bombs available.
     */
    static Requirement powerBombs(int count) {
        return new PowerBombsRequirement(count);
    }

    /**
     * Requirement for having at least a certain amount of energy available.
     */
    static Requirement energy(int amount) {
        return new EnergyRequirement(amount);
    }
}
