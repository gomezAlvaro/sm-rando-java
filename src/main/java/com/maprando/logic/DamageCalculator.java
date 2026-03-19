package com.maprando.logic;

import com.maprando.model.GameState;
import com.maprando.model.ItemDefinition;
import com.maprando.model.ItemRegistry;

import java.util.List;

/**
 * Calculates damage dealt to enemies and damage taken from enemies.
 * This uses JSON data for damage multipliers and reductions.
 */
public class DamageCalculator {

    // Base damage values
    private static final int BASE_ENEMY_DAMAGE = 20;
    private static final int BASE_SHOT_DAMAGE = 10;
    private static final int BASE_MISSILE_DAMAGE = 50;
    private static final int BASE_SUPER_MISSILE_DAMAGE = 100;
    private static final int BASE_POWER_BOMB_DAMAGE = 200;

    /**
     * Gets the total beam damage multiplier from all collected beams.
     * Multipliers stack multiplicatively.
     * Falls back to hardcoded values if JSON data is not available.
     */
    public static double getBeamDamageMultiplier(GameState state) {
        double multiplier = 1.0;
        boolean hasPlasma = state.getInventory().hasItem("PLASMA_BEAM");
        boolean hasSpazer = state.getInventory().hasItem("SPAZER_BEAM");

        for (ItemDefinition item : state.getInventory().getCollectedItems()) {
            if (item.isBeam()) {
                Double jsonMultiplier = item.getDamageMultiplier();

                // Fallback to hardcoded values if JSON data is not available
                if (jsonMultiplier != null) {
                    // Spazer and Plasma are mutually exclusive
                    // Plasma takes precedence if both are present
                    if (item.getId().equals("SPAZER_BEAM") && hasPlasma) {
                        // Skip Spazer if Plasma is present
                        continue;
                    }
                    multiplier *= jsonMultiplier;
                } else {
                    // Hardcoded fallback for minimal registry
                    if (item.getId().equals("CHARGE_BEAM")) {
                        multiplier *= 3.0;
                    } else if (item.getId().equals("SPAZER_BEAM") && !hasPlasma) {
                        multiplier *= 2.0;
                    } else if (item.getId().equals("PLASMA_BEAM")) {
                        multiplier *= 2.0;
                    }
                }
            }
        }

        return multiplier;
    }

    /**
     * Gets the total beam damage bonus from all collected beams.
     * Bonuses stack additively.
     * Falls back to hardcoded values if JSON data is not available.
     */
    public static int getBeamDamageBonus(GameState state) {
        int bonus = 0;

        for (ItemDefinition item : state.getInventory().getCollectedItems()) {
            if (item.isBeam()) {
                Integer jsonBonus = item.getDamageBonus();

                if (jsonBonus != null) {
                    bonus += jsonBonus;
                } else {
                    // Hardcoded fallback for minimal registry
                    if (item.getId().equals("ICE_BEAM")) {
                        bonus += 5;
                    } else if (item.getId().equals("WAVE_BEAM")) {
                        bonus += 10;
                    }
                }
            }
        }

        return bonus;
    }

    /**
     * Gets the suit damage reduction as a multiplier (0.0 to 1.0).
     * Lower is better - 0.25 means 75% damage reduction.
     * Gravity Suit takes precedence over Varia Suit.
     * Falls back to hardcoded values if JSON data is not available.
     */
    public static double getSuitDamageReduction(GameState state) {
        if (state.getInventory().hasItem("GRAVITY_SUIT")) {
            ItemDefinition gravitySuit = state.getInventory().getItemRegistry().getById("GRAVITY_SUIT");
            if (gravitySuit != null && gravitySuit.getDamageReduction() != null) {
                // damageReduction is the percentage reduced (e.g., 0.75 = 75%)
                // So we take 1.0 - damageReduction to get the multiplier
                return 1.0 - gravitySuit.getDamageReduction();
            }
            // Fallback to hardcoded value
            return 0.25; // 75% reduction
        } else if (state.getInventory().hasItem("VARIA_SUIT")) {
            ItemDefinition variaSuit = state.getInventory().getItemRegistry().getById("VARIA_SUIT");
            if (variaSuit != null && variaSuit.getDamageReduction() != null) {
                return 1.0 - variaSuit.getDamageReduction();
            }
            // Fallback to hardcoded value
            return 0.5; // 50% reduction
        }
        return 1.0; // No reduction
    }

    /**
     * Calculates the damage dealt by a standard shot.
     *
     * Super Metroid beam mechanics:
     * - Charge, Ice, Wave can all be equipped together
     * - Spazer and Plasma are mutually exclusive (only one can be equipped at a time)
     * - If both Spazer and Plasma are present, Plasma takes precedence (it's the stronger beam)
     * - Damage calculation: base * early_multipliers + bonuses * late_multiplier
     */
    public static int calculateShotDamage(GameState state) {
        int damage = BASE_SHOT_DAMAGE;

        // Apply early multipliers (Charge)
        if (state.getInventory().hasItem("CHARGE_BEAM")) {
            ItemDefinition charge = state.getInventory().getItemRegistry().getById("CHARGE_BEAM");
            if (charge != null && charge.getDamageMultiplier() != null) {
                damage = (int) Math.round(damage * charge.getDamageMultiplier());
            } else {
                damage *= 3; // Fallback
            }
        }

        // Add damage bonuses (Ice, Wave)
        for (ItemDefinition item : state.getInventory().getCollectedItems()) {
            if (item.isBeam() && item.getDamageBonus() != null) {
                damage += item.getDamageBonus();
            } else if (item.isBeam()) {
                // Fallback for minimal registry
                if (item.getId().equals("ICE_BEAM")) {
                    damage += 5;
                } else if (item.getId().equals("WAVE_BEAM")) {
                    damage += 10;
                }
            }
        }

        // Apply late multipliers (Spazer/Plasma - mutually exclusive)
        if (state.getInventory().hasItem("PLASMA_BEAM")) {
            ItemDefinition plasma = state.getInventory().getItemRegistry().getById("PLASMA_BEAM");
            if (plasma != null && plasma.getDamageMultiplier() != null) {
                damage = (int) Math.round(damage * plasma.getDamageMultiplier());
            } else {
                damage *= 2; // Fallback
            }
        } else if (state.getInventory().hasItem("SPAZER_BEAM")) {
            ItemDefinition spazer = state.getInventory().getItemRegistry().getById("SPAZER_BEAM");
            if (spazer != null && spazer.getDamageMultiplier() != null) {
                damage = (int) Math.round(damage * spazer.getDamageMultiplier());
            } else {
                damage *= 2; // Fallback
            }
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
        double multiplier = getSuitDamageReduction(state);
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
     * This uses JSON data for suit damage reduction values.
     */
    public static double getDamageReduction(GameState state) {
        double multiplier = getSuitDamageReduction(state);
        return 1.0 - multiplier; // Convert multiplier to reduction percentage
    }
}
