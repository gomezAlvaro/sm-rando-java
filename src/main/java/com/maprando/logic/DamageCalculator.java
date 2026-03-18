package com.maprando.logic;

import com.maprando.model.GameState;

/**
 * Calculates damage dealt to enemies and damage taken from enemies.
 * This is a simplified system for the proof-of-concept.
 */
public class DamageCalculator {

    // Base damage values
    private static final int BASE_ENEMY_DAMAGE = 20;
    private static final int BASE_SHOT_DAMAGE = 10;
    private static final int BASE_MISSILE_DAMAGE = 50;
    private static final int BASE_SUPER_MISSILE_DAMAGE = 100;
    private static final int BASE_POWER_BOMB_DAMAGE = 200;

    // Damage reduction multipliers
    private static final double VARIA_SUIT_MULTIPLIER = 0.5; // 50% reduction
    private static final double GRAVITY_SUIT_MULTIPLIER = 0.25; // 75% reduction

    /**
     * Calculates the damage dealt by a standard shot.
     */
    public static int calculateShotDamage(GameState state) {
        int damage = BASE_SHOT_DAMAGE;

        // Charge beam deals more damage
        if (state.getInventory().hasItem("CHARGE_BEAM")) {
            damage *= 3;
        }

        // Ice beam adds extra damage
        if (state.getInventory().hasItem("ICE_BEAM")) {
            damage += 5;
        }

        // Wave beam pierces
        if (state.getInventory().hasItem("WAVE_BEAM")) {
            damage += 10;
        }

        // Spazer beam multiplies
        if (state.getInventory().hasItem("SPAZER_BEAM")) {
            damage *= 2;
        }

        // Plasma beam pierces and deals extra damage
        if (state.getInventory().hasItem("PLASMA_BEAM")) {
            damage *= 2;
        }

        return damage;
    }

    /**
     * Calculates the damage dealt by a missile.
     */
    public static int calculateMissileDamage(GameState state) {
        // Base missile damage - super missiles are a separate ammunition type
        return BASE_MISSILE_DAMAGE;
    }

    /**
     * Calculates the damage dealt by a super missile.
     */
    public static int calculateSuperMissileDamage(GameState state) {
        return BASE_SUPER_MISSILE_DAMAGE;
    }

    /**
     * Calculates the damage dealt by a power bomb.
     */
    public static int calculatePowerBombDamage(GameState state) {
        return BASE_POWER_BOMB_DAMAGE;
    }

    /**
     * Calculates the damage taken from an enemy attack.
     *
     * @param state The current game state
     * @param baseDamage The base damage of the enemy attack
     * @return The actual damage after reduction
     */
    public static int calculateDamageTaken(GameState state, int baseDamage) {
        double multiplier = 1.0;

        // Check for damage reduction suits
        if (state.getInventory().hasItem("GRAVITY_SUIT")) {
            multiplier = GRAVITY_SUIT_MULTIPLIER;
        } else if (state.getInventory().hasItem("VARIA_SUIT")) {
            multiplier = VARIA_SUIT_MULTIPLIER;
        }

        return (int) Math.ceil(baseDamage * multiplier);
    }

    /**
     * Calculates the damage taken from a standard enemy attack.
     */
    public static int calculateStandardDamageTaken(GameState state) {
        return calculateDamageTaken(state, BASE_ENEMY_DAMAGE);
    }

    /**
     * Checks if the player would survive taking a specified amount of damage.
     */
    public static boolean wouldSurvive(GameState state, int baseDamage) {
        int actualDamage = calculateDamageTaken(state, baseDamage);
        return state.getEnergy() > actualDamage;
    }

    /**
     * Checks if the player can survive a standard enemy attack.
     */
    public static boolean canSurviveStandardAttack(GameState state) {
        return wouldSurvive(state, BASE_ENEMY_DAMAGE);
    }

    /**
     * Calculates the number of hits needed to defeat an enemy with the current equipment.
     *
     * @param state The current game state
     * @param enemyHealth The enemy's health
     * @param useMissiles Whether to use missiles (false = use shots)
     * @return The number of hits required
     */
    public static int calculateHitsNeeded(GameState state, int enemyHealth, boolean useMissiles) {
        int damagePerHit;
        if (useMissiles) {
            damagePerHit = calculateMissileDamage(state);
        } else {
            damagePerHit = calculateShotDamage(state);
        }

        if (damagePerHit <= 0) {
            return Integer.MAX_VALUE; // Can't damage
        }

        return (int) Math.ceil((double) enemyHealth / damagePerHit);
    }

    /**
     * Calculates the effective damage reduction percentage (0.0 to 1.0).
     */
    public static double getDamageReduction(GameState state) {
        if (state.getInventory().hasItem("GRAVITY_SUIT")) {
            return 1.0 - GRAVITY_SUIT_MULTIPLIER; // 0.75 (75%)
        } else if (state.getInventory().hasItem("VARIA_SUIT")) {
            return 1.0 - VARIA_SUIT_MULTIPLIER; // 0.5 (50%)
        }
        return 0.0; // No reduction
    }
}
