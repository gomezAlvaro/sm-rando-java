package com.maprando.logic;

import com.maprando.data.DataLoader;
import com.maprando.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DamageCalculator with JSON data.
 */
class DamageCalculatorIntegrationTest {
    private DataLoader dataLoader;
    private GameState gameState;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();
        gameState = new GameState();
    }

    @Test
    void testBaseShotDamage() {
        // No beams collected yet
        int damage = DamageCalculator.calculateShotDamage(gameState);
        assertEquals(10, damage); // BASE_SHOT_DAMAGE
    }

    @Test
    void testChargeBeamDamageMultiplier() {
        // From JSON: CHARGE_BEAM has damageMultiplier: 3.0
        gameState.getInventory().addItem("CHARGE_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);
        assertEquals(30, damage); // 10 * 3.0
    }

    @Test
    void testIceBeamDamageBonus() {
        // From JSON: ICE_BEAM has damageBonus: 5
        gameState.getInventory().addItem("ICE_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);
        assertEquals(15, damage); // 10 + 5
    }

    @Test
    void testWaveBeamDamageBonus() {
        // From JSON: WAVE_BEAM has damageBonus: 10
        gameState.getInventory().addItem("WAVE_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);
        assertEquals(20, damage); // 10 + 10
    }

    @Test
    void testSpazerBeamDamageMultiplier() {
        // From JSON: SPAZER_BEAM has damageMultiplier: 2.0
        gameState.getInventory().addItem("SPAZER_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);
        assertEquals(20, damage); // 10 * 2.0
    }

    @Test
    void testPlasmaBeamDamageMultiplier() {
        // From JSON: PLASMA_BEAM has damageMultiplier: 2.0
        gameState.getInventory().addItem("PLASMA_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);
        assertEquals(20, damage); // 10 * 2.0
    }

    @Test
    void testMultipleBeamsStackCorrectly() {
        // Charge (3.0x) + Ice (+5) + Wave (+10)
        gameState.getInventory().addItem("CHARGE_BEAM");
        gameState.getInventory().addItem("ICE_BEAM");
        gameState.getInventory().addItem("WAVE_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);
        assertEquals(45, damage); // 10 * 3.0 + 5 + 10
    }

    @Test
    void testPlasmaTakesPrecedenceOverSpazer() {
        // Collect both - Plasma should take precedence
        gameState.getInventory().addItem("SPAZER_BEAM");
        gameState.getInventory().addItem("PLASMA_BEAM");
        gameState.getInventory().addItem("CHARGE_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);
        assertEquals(60, damage); // 10 * 3.0 * 2.0 (Plasma, not Spazer)
    }

    @Test
    void testFullBeamSetup() {
        // Charge + Ice + Wave + Plasma
        gameState.getInventory().addItem("CHARGE_BEAM");
        gameState.getInventory().addItem("ICE_BEAM");
        gameState.getInventory().addItem("WAVE_BEAM");
        gameState.getInventory().addItem("PLASMA_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);
        assertEquals(90, damage); // ((10 * 3.0) + 5 + 10) * 2.0
    }

    @Test
    void testBaseDamageTaken() {
        // No suits
        int damage = DamageCalculator.calculateDamageTaken(gameState, 100);
        assertEquals(100, damage);
    }

    @Test
    void testVariaSuitDamageReduction() {
        // From JSON: VARIA_SUIT has damageReduction: 0.5
        gameState.getInventory().addItem("VARIA_SUIT");

        int damage = DamageCalculator.calculateDamageTaken(gameState, 100);
        assertEquals(50, damage); // 100 * 0.5
    }

    @Test
    void testGravitySuitDamageReduction() {
        // From JSON: GRAVITY_SUIT has damageReduction: 0.75
        gameState.getInventory().addItem("GRAVITY_SUIT");

        int damage = DamageCalculator.calculateDamageTaken(gameState, 100);
        assertEquals(25, damage); // 100 * (1.0 - 0.75) = 100 * 0.25
    }

    @Test
    void testGravitySuitTakesPrecedenceOverVaria() {
        // Both suits - Gravity should take precedence
        gameState.getInventory().addItem("VARIA_SUIT");
        gameState.getInventory().addItem("GRAVITY_SUIT");

        int damage = DamageCalculator.calculateDamageTaken(gameState, 100);
        assertEquals(25, damage); // 100 * 0.25 (Gravity, not Varia)
    }

    @Test
    void testCanSurviveStandardAttack() {
        // Standard enemy deals 20 damage
        // Base energy is 99, so should survive
        assertTrue(DamageCalculator.canSurviveStandardAttack(gameState));
    }

    @Test
    void testCanSurviveWithVariaSuit() {
        // 20 * 0.5 = 10 damage, should easily survive
        gameState.getInventory().addItem("VARIA_SUIT");
        assertTrue(DamageCalculator.canSurviveStandardAttack(gameState));
    }

    @Test
    void testGetDamageReductionPercentage() {
        // No suit = 0% reduction
        assertEquals(0.0, DamageCalculator.getDamageReduction(gameState));

        // Varia = 50% reduction
        gameState.getInventory().addItem("VARIA_SUIT");
        assertEquals(0.5, DamageCalculator.getDamageReduction(gameState));

        // Gravity = 75% reduction
        gameState.getInventory().removeItem("VARIA_SUIT");
        gameState.getInventory().addItem("GRAVITY_SUIT");
        assertEquals(0.75, DamageCalculator.getDamageReduction(gameState));
    }

    @Test
    void testMissileDamageUnchanged() {
        // Missile damage should be constant
        int damage = DamageCalculator.calculateMissileDamage(gameState);
        assertEquals(50, damage); // BASE_MISSILE_DAMAGE
    }

    @Test
    void testSuperMissileDamageUnchanged() {
        // Super missile damage should be constant
        int damage = DamageCalculator.calculateSuperMissileDamage(gameState);
        assertEquals(100, damage); // BASE_SUPER_MISSILE_DAMAGE
    }

    @Test
    void testPowerBombDamageUnchanged() {
        // Power bomb damage should be constant
        int damage = DamageCalculator.calculatePowerBombDamage(gameState);
        assertEquals(200, damage); // BASE_POWER_BOMB_DAMAGE
    }
}
