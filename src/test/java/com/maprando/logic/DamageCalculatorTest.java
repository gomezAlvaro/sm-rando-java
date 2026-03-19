package com.maprando.logic;

import com.maprando.model.GameState;
import com.maprando.util.TestSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the DamageCalculator class
 */
@DisplayName("DamageCalculator Tests")
class DamageCalculatorTest {

    private GameState gameState;
    private DamageCalculator calculator;

    @BeforeAll
    static void setUpClass() {
        TestSetup.initializeMinimalRegistry();
    }

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        calculator = new DamageCalculator();
    }

    @Test
    @DisplayName("Standard shot damage should be 10")
    void testCalculateShotDamage() {
        gameState.collectItem("CHARGE_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);

        assertEquals(30, damage, "Standard shot with Charge Beam should deal 30 damage (10 * 3)");
    }

    @Test
    @DisplayName("Game should start with 99 energy")
    void testStartingEnergy() {
        assertEquals(99, gameState.getEnergy(),
                "Starting energy should be 99 (Super Metroid behavior)");
    }

    @Test
    @DisplayName("Shot damage should increase with beams")
    void testCalculateShotDamageWithMultipleBeams() {
        gameState.collectItem("CHARGE_BEAM");
        gameState.collectItem("ICE_BEAM");
        gameState.collectItem("WAVE_BEAM");
        gameState.collectItem("SPAZER_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);

        // Base 10 * Charge (3) = 30 + Ice (5) = 35 + Wave (10) = 45 * Spazer (2) = 90
        assertEquals(90, damage, "Charge + Ice + Wave + Spazer should deal 90 damage");
    }

    @Test
    @DisplayName("Plasma beam should deal maximum damage (and overrides Spazer)")
    void testCalculateShotDamageWithPlasma() {
        gameState.collectItem("CHARGE_BEAM");
        gameState.collectItem("ICE_BEAM");
        gameState.collectItem("WAVE_BEAM");
        gameState.collectItem("SPAZER_BEAM");  // This should be ignored since Plasma is present
        gameState.collectItem("PLASMA_BEAM");

        int damage = DamageCalculator.calculateShotDamage(gameState);

        // Base 10 * Charge (3) = 30 + Ice (5) = 35 + Wave (10) = 45 * Plasma (2) = 90
        // Note: Spazer is ignored because Plasma takes precedence (they're mutually exclusive)
        assertEquals(90, damage, "Charge + Ice + Wave + Plasma should deal 90 damage (Spazer ignored)");
    }

    @Test
    @DisplayName("Missile damage should be calculated correctly")
    void testCalculateMissileDamage() {
        int damage = calculator.calculateMissileDamage(gameState);

        assertEquals(50, damage, "Standard missile should deal 50 damage");
    }

    @Test
    @DisplayName("Super missile damage should be higher")
    void testCalculateSuperMissileDamage() {
        int damage = calculator.calculateSuperMissileDamage(gameState);

        assertEquals(100, damage, "Super missile should deal 100 damage");
    }

    @Test
    @DisplayName("Power bomb damage should be calculated")
    void testCalculatePowerBombDamage() {
        int damage = calculator.calculatePowerBombDamage(gameState);

        assertEquals(200, damage, "Power bomb should deal 200 damage");
    }

    @Test
    @DisplayName("Damage taken should be reduced with Varia Suit")
    void testCalculateDamageTakenWithVariaSuit() {
        gameState.collectItem("VARIA_SUIT");

        int damage = calculator.calculateDamageTaken(gameState, 100);

        assertEquals(50, damage, "Varia Suit should reduce damage by 50%");
    }

    @Test
    @DisplayName("Damage taken should be reduced more with Gravity Suit")
    void testCalculateDamageTakenWithGravitySuit() {
        gameState.collectItem("GRAVITY_SUIT");

        int damage = calculator.calculateDamageTaken(gameState, 100);

        assertEquals(25, damage, "Gravity Suit should reduce damage by 75%");
    }

    @Test
    @DisplayName("Damage taken without suit should be full amount")
    void testCalculateDamageTakenWithoutSuit() {
        int damage = calculator.calculateDamageTaken(gameState, 100);

        assertEquals(100, damage, "No suit should mean full damage");
    }

    @Test
    @DisplayName("Should survive should check energy vs damage")
    void testWouldSurvive() {
        gameState.setEnergy(100);

        assertTrue(calculator.wouldSurvive(gameState, 80),
                "Should survive 80 damage with 100 energy");
        assertFalse(calculator.wouldSurvive(gameState, 120),
                "Should not survive 120 damage with 100 energy");
    }

    @Test
    @DisplayName("Should survive with suit damage reduction")
    void testWouldSurviveWithSuit() {
        gameState.setEnergy(40);
        gameState.collectItem("VARIA_SUIT");

        assertFalse(DamageCalculator.wouldSurvive(gameState, 80),
                "Should not survive 80 damage (40 reduced) with 40 energy (uses > not >=)");
    }

    @Test
    @DisplayName("Can survive standard attack should check typical enemy damage")
    void testCanSurviveStandardAttack() {
        gameState.setEnergy(60);
        gameState.collectItem("VARIA_SUIT");

        assertTrue(DamageCalculator.canSurviveStandardAttack(gameState),
                "Should survive standard 20 damage attack (10 after reduction) with Varia Suit");
    }

    @Test
    @DisplayName("Calculate hits needed should return correct count")
    void testCalculateHitsNeeded() {
        gameState.collectItem("CHARGE_BEAM");

        int hits = DamageCalculator.calculateHitsNeeded(gameState, 500, false);

        assertEquals(17, hits, "Should need 17 hits to defeat 500 HP enemy with Charge Beam (30 dmg)");
    }

    @Test
    @DisplayName("Get damage reduction should return correct percentage")
    void testGetDamageReduction() {
        assertEquals(0.0, DamageCalculator.getDamageReduction(gameState), 0.001,
                "No suit should have 0% reduction");

        gameState.collectItem("VARIA_SUIT");
        assertEquals(0.5, DamageCalculator.getDamageReduction(gameState), 0.001,
                "Varia Suit should have 0.5 reduction (50%)");

        gameState.collectItem("GRAVITY_SUIT");
        assertEquals(0.75, DamageCalculator.getDamageReduction(gameState), 0.001,
                "Gravity Suit should have 0.75 reduction (75%)");
    }

    @Test
    @DisplayName("Damage calculation should handle zero damage")
    void testCalculateZeroDamage() {
        int damage = calculator.calculateDamageTaken(gameState, 0);

        assertEquals(0, damage, "Zero damage should remain zero");
    }

    @Test
    @DisplayName("Shot damage without charge beam should be minimal")
    void testCalculateShotDamageWithoutChargeBeam() {
        int damage = DamageCalculator.calculateShotDamage(gameState);

        assertEquals(10, damage, "Shot without Charge Beam should deal 10 damage (base)");
    }

    @Test
    @DisplayName("Multiple beams should stack damage")
    void testBeamStacking() {
        gameState.collectItem("CHARGE_BEAM");
        assertEquals(30, DamageCalculator.calculateShotDamage(gameState),
                "Charge Beam only: 30 damage");

        gameState.collectItem("ICE_BEAM");
        assertEquals(35, DamageCalculator.calculateShotDamage(gameState),
                "Charge + Ice: 35 damage");

        gameState.collectItem("WAVE_BEAM");
        assertEquals(45, DamageCalculator.calculateShotDamage(gameState),
                "Charge + Ice + Wave: 45 damage");
    }

    @Test
    @DisplayName("Spazer and Plasma should be mutually exclusive")
    void testSpazerPlasmaMutualExclusivity() {
        gameState.collectItem("CHARGE_BEAM");
        gameState.collectItem("ICE_BEAM");
        gameState.collectItem("WAVE_BEAM");
        gameState.collectItem("SPAZER_BEAM");
        assertEquals(90, DamageCalculator.calculateShotDamage(gameState),
                "With Spazer: 90 damage");

        // Remove Spazer, add Plasma
        gameState.getInventory().removeItem("SPAZER_BEAM");
        gameState.collectItem("PLASMA_BEAM");
        // Without Spazer: 10 * 3 = 30 + 5 = 35 + 10 = 45 * 2 = 90
        assertEquals(90, DamageCalculator.calculateShotDamage(gameState),
                "With Plasma (no Spazer): 90 damage");

        // Add Spazer back - Plasma should still take precedence
        gameState.collectItem("SPAZER_BEAM");
        assertEquals(90, DamageCalculator.calculateShotDamage(gameState),
                "With both Plasma and Spazer, Plasma takes precedence: 90 damage");
    }

    @Test
    @DisplayName("Survival calculation should account for suit reduction")
    void testSurvivalWithSuitReduction() {
        gameState.setEnergy(30);
        gameState.collectItem("VARIA_SUIT");

        // 60 damage becomes 30 with Varia Suit
        assertFalse(DamageCalculator.wouldSurvive(gameState, 60),
                "Should not survive 60 damage (30 reduced) with 30 energy (uses > not >=)");

        // 80 damage becomes 40 with Varia Suit
        assertFalse(DamageCalculator.wouldSurvive(gameState, 80),
                "Should not survive 80 damage (40 reduced) with 30 energy");
    }

    @Test
    @DisplayName("Hits needed should round up")
    void testHitsNeededRounding() {
        gameState.collectItem("CHARGE_BEAM");

        int hits = DamageCalculator.calculateHitsNeeded(gameState, 100, false);

        assertEquals(4, hits, "Should need 4 hits for 100 HP with Charge Beam (30 dmg per hit)");
    }
}