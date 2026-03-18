package com.maprando.logic;

import com.maprando.model.GameState;
import com.maprando.model.ResourceType;

import java.util.Set;

/**
 * Checks if the player's current inventory and state meet specific requirements.
 * This is used for determining if doors can be opened, areas can be accessed, etc.
 */
public class RequirementChecker {

    /**
     * Checks if the player has all specified items.
     */
    public static boolean hasItemIds(GameState state, Set<String> requiredItemIds) {
        if (state == null || requiredItemIds == null) {
            return false;
        }

        if (requiredItemIds.isEmpty()) {
            return true; // No requirements means always satisfied
        }

        return requiredItemIds.stream().allMatch(itemId -> state.getInventory().hasItem(itemId));
    }

    /**
     * Checks if the player has at least one of the specified items.
     */
    public static boolean hasAnyItemId(GameState state, Set<String> itemIds) {
        if (state == null || itemIds == null) {
            return false;
        }

        if (itemIds.isEmpty()) {
            return true; // No requirements means always satisfied
        }

        return itemIds.stream().anyMatch(itemId -> state.getInventory().hasItem(itemId));
    }

    /**
     * Checks if the player has a specific item.
     */
    public static boolean hasItem(GameState state, String itemId) {
        if (state == null || itemId == null) {
            return false;
        }
        return state.getInventory().hasItem(itemId);
    }

    /**
     * Checks if the player can morph into a ball.
     */
    public static boolean canMorph(GameState state) {
        return hasItem(state, "MORPH_BALL");
    }

    /**
     * Checks if the player can place bombs.
     */
    public static boolean canPlaceBombs(GameState state) {
        return canMorph(state) && hasItem(state, "BOMB");
    }

    /**
     * Checks if the player can use power bombs.
     */
    public static boolean canUsePowerBombs(GameState state) {
        return canMorph(state) && hasItem(state, "POWER_BOMB");
    }

    /**
     * Checks if the player can use speed booster (has it and is running).
     * For simplicity, this just checks for the item.
     */
    public static boolean canSpeedBoost(GameState state) {
        return hasItem(state, "SPEED_BOOSTER");
    }

    /**
     * Checks if the player can perform space jump.
     */
    public static boolean canSpaceJump(GameState state) {
        return hasItem(state, "SPACE_JUMP");
    }

    /**
     * Checks if the player can screw attack.
     */
    public static boolean canScrewAttack(GameState state) {
        return hasItem(state, "SCREW_ATTACK");
    }

    /**
     * Checks if the player has the ice beam (required for freezing enemies).
     */
    public static boolean hasIceBeam(GameState state) {
        return hasItem(state, "ICE_BEAM");
    }

    /**
     * Checks if the player can see hidden blocks (with X-Ray).
     */
    public static boolean canSeeHidden(GameState state) {
        return hasItem(state, "XRAY_SCOPE");
    }

    /**
     * Checks if the player can survive in lava (needs Gravity Suit).
     */
    public static boolean canSurviveLava(GameState state) {
        return hasItem(state, "GRAVITY_SUIT");
    }

    /**
     * Checks if the player has reduced damage (Varia or Gravity Suit).
     */
    public static boolean hasDamageReduction(GameState state) {
        return hasItem(state, "VARIA_SUIT") || hasItem(state, "GRAVITY_SUIT");
    }

    /**
     * Checks if the player can use a specific resource.
     */
    public static boolean canUseResource(GameState state, ResourceType type, int amount) {
        return ResourceManager.hasResource(state, type, amount);
    }

    /**
     * Checks if the player can shoot missiles.
     */
    public static boolean canShootMissiles(GameState state) {
        return ResourceManager.hasResource(state, ResourceType.MISSILE, 1);
    }

    /**
     * Checks if the player can shoot super missiles.
     */
    public static boolean canShootSuperMissiles(GameState state) {
        return ResourceManager.hasResource(state, ResourceType.SUPER_MISSILE, 1);
    }

    /**
     * Checks if the player can use power bombs.
     */
    public static boolean canUsePowerBombsResource(GameState state) {
        return ResourceManager.hasResource(state, ResourceType.POWER_BOMB, 1);
    }

    /**
     * Checks a complex requirement consisting of multiple conditions.
     * All conditions must be satisfied.
     */
    public static boolean meetsRequirements(GameState state, Requirement... requirements) {
        if (state == null || requirements == null) {
            return false;
        }

        for (Requirement req : requirements) {
            if (!req.isSatisfied(state)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Functional interface for defining custom requirements.
     */
    @FunctionalInterface
    public interface Requirement {
        boolean isSatisfied(GameState state);
    }

    /**
     * Common requirement builders.
     */
    public static class Requirements {
        public static Requirement hasItem(String itemId) {
            return state -> RequirementChecker.hasItem(state, itemId);
        }

        public static Requirement hasItemIds(Set<String> itemIds) {
            return state -> RequirementChecker.hasItemIds(state, itemIds);
        }

        public static Requirement canMorph() {
            return RequirementChecker::canMorph;
        }

        public static Requirement canPlaceBombs() {
            return RequirementChecker::canPlaceBombs;
        }

        public static Requirement hasIceBeam() {
            return RequirementChecker::hasIceBeam;
        }

        public static Requirement canSurviveLava() {
            return RequirementChecker::canSurviveLava;
        }

        public static Requirement hasResource(ResourceType type, int amount) {
            return state -> RequirementChecker.canUseResource(state, type, amount);
        }
    }
}
