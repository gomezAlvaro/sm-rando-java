package com.maprando.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for RunSpeed calculations.
 * Ported from maprando/src/randomize/run_speed.rs tests
 */
public class RunSpeedTest {

    @Test
    public void testGetMaxExtraRunSpeed() {
        // Test cases from Rust implementation
        float result1 = RunSpeed.getMaxExtraRunSpeed(6.5625f);
        assertEquals(2.0f, result1, 0.01f, "Max extra run speed for 6.5625 tiles should be 2.0");

        float result2 = RunSpeed.getMaxExtraRunSpeed(45.0f);
        assertEquals(6.9375f, result2, 0.01f, "Max extra run speed for 45 tiles should be 6.9375");

        // Edge cases
        float result3 = RunSpeed.getMaxExtraRunSpeed(0.0f);
        assertEquals(0.0f, result3, 0.01f, "Max extra run speed for 0 tiles should be 0.0");

        // Large runway - capped at table size
        float result4 = RunSpeed.getMaxExtraRunSpeed(100.0f);
        assertTrue(result4 >= 6.9f, "Large runway should provide speed near max");
    }

    @Test
    public void testGetExtraRunSpeedTiles() {
        // Test inverse calculations
        float result1 = RunSpeed.getExtraRunSpeedTiles(2.0f);
        assertEquals(0x68D / 256.0f, result1, 0.1f,
            "Extra run speed 2.0 should convert to correct tiles");

        float result2 = RunSpeed.getExtraRunSpeedTiles(7.0f);
        assertEquals(0x2AF5 / 256.0f, result2, 0.1f,
            "Extra run speed 7.0 should convert to correct tiles");

        // Below minimum
        float result3 = RunSpeed.getExtraRunSpeedTiles(0.01f);
        assertEquals(0.0f, result3, 0.001f,
            "Very low extra run speed should return 0 tiles");
    }

    @Test
    public void testGetShortchargeMinExtraRunSpeed() {
        // Test various skill levels
        float result1 = RunSpeed.getShortchargeMinExtraRunSpeed(11.0f);
        assertTrue(result1 > 0.0f, "Shortcharge skill 11 should provide some speed");

        float result2 = RunSpeed.getShortchargeMinExtraRunSpeed(25.0f);
        assertTrue(result2 > result1, "Higher skill should provide more speed");

        float result3 = RunSpeed.getShortchargeMinExtraRunSpeed(30.0f);
        assertTrue(result3 >= result2, "Skill 30 should provide at least as much as 25");

        // Below minimum
        float result4 = RunSpeed.getShortchargeMinExtraRunSpeed(10.0f);
        // Should clamp to minimum table value
        assertTrue(result4 > 0.0f, "Below minimum should still return some value");
    }

    @Test
    public void testGetShortchargeMaxExtraRunSpeed() {
        // Normal case
        Float result1 = RunSpeed.getShortchargeMaxExtraRunSpeed(15.0f, 20.0f);
        assertNotNull(result1, "Valid skill and runway should return a value");
        assertTrue(result1 > 0.0f, "Max extra run speed should be positive");

        // Skill exceeds runway
        Float result2 = RunSpeed.getShortchargeMaxExtraRunSpeed(25.0f, 20.0f);
        assertNull(result2, "Skill exceeding runway should return null");

        // Long runway (>= 30 tiles)
        Float result3 = RunSpeed.getShortchargeMaxExtraRunSpeed(15.0f, 35.0f);
        assertNotNull(result3, "Long runway should return a value");
        float expectedMax = RunSpeed.getMaxExtraRunSpeed(35.0f);
        assertEquals(expectedMax, result3, 0.01f,
            "Long runway should match max extra run speed");

        // Various skill levels
        Float result4 = RunSpeed.getShortchargeMaxExtraRunSpeed(20.0f, 25.0f);
        assertNotNull(result4, "Skill 20 with 25 tiles should return a value");

        Float result5 = RunSpeed.getShortchargeMaxExtraRunSpeed(14.0f, 20.0f);
        assertNotNull(result5, "Skill 14 with 20 tiles should return a value");
    }

    @Test
    public void testRunwayDistanceConsistency() {
        // Test that the conversions are consistent
        float runwayTiles = 15.0f;
        float maxSpeed = RunSpeed.getMaxExtraRunSpeed(runwayTiles);
        float convertedTiles = RunSpeed.getExtraRunSpeedTiles(maxSpeed);

        // Should be approximately the same (may be slightly different due to table lookup)
        assertTrue(Math.abs(runwayTiles - convertedTiles) < 1.0f,
            "Speed-tiles conversion should be consistent");
    }

    @Test
    public void testShortchargeRange() {
        // Test that min <= max for valid inputs
        float skill = 15.0f;
        float runway = 20.0f;

        float minSpeed = RunSpeed.getShortchargeMinExtraRunSpeed(skill);
        Float maxSpeed = RunSpeed.getShortchargeMaxExtraRunSpeed(skill, runway);

        assertNotNull(maxSpeed, "Valid inputs should return max speed");
        assertTrue(minSpeed <= maxSpeed,
            "Min speed should be <= max speed");
    }

    @Test
    public void testIncreasingSkillIncreasesSpeed() {
        float runway = 20.0f;

        Float speed14 = RunSpeed.getShortchargeMaxExtraRunSpeed(14.0f, runway);
        Float speed15 = RunSpeed.getShortchargeMaxExtraRunSpeed(15.0f, runway);
        Float speed16 = RunSpeed.getShortchargeMaxExtraRunSpeed(16.0f, runway);

        assertNotNull(speed14, "Skill 14 should return value");
        assertNotNull(speed15, "Skill 15 should return value");
        assertNotNull(speed16, "Skill 16 should return value");

        // Higher skill should generally provide >= speed
        assertTrue(speed15 >= speed14 * 0.9f, "Higher skill should not reduce speed significantly");
        assertTrue(speed16 >= speed15 * 0.9f, "Higher skill should not reduce speed significantly");
    }

    @Test
    public void testLongerRunwayIncreasesSpeed() {
        float skill = 15.0f;

        Float speed15 = RunSpeed.getShortchargeMaxExtraRunSpeed(skill, 15.0f);
        Float speed20 = RunSpeed.getShortchargeMaxExtraRunSpeed(skill, 20.0f);
        Float speed25 = RunSpeed.getShortchargeMaxExtraRunSpeed(skill, 25.0f);

        assertNotNull(speed15, "15 tiles should return value");
        assertNotNull(speed20, "20 tiles should return value");
        assertNotNull(speed25, "25 tiles should return value");

        // Longer runway should provide more speed
        assertTrue(speed20 > speed15, "Longer runway should provide more speed");
        assertTrue(speed25 > speed20, "Longer runway should provide more speed");
    }

    @Test
    public void testTableEdgeCases() {
        // Test minimum table values
        Float minSkill = RunSpeed.getShortchargeMaxExtraRunSpeed(11.0f, 11.0f);
        assertNotNull(minSkill, "Minimum skill/runway should return value");
        assertTrue(minSkill > 0.0f, "Minimum should still provide speed");

        // Test maximum table values
        Float maxSkill = RunSpeed.getShortchargeMaxExtraRunSpeed(30.0f, 30.0f);
        assertNotNull(maxSkill, "Maximum skill/runway should return value");
    }

    @Test
    public void testCommonRunwayLengths() {
        // Test common runway lengths used in randomizer logic
        float[] commonLengths = {6.5625f, 10.0f, 15.0f, 20.0f, 25.0f, 30.0f, 45.0f};

        for (float length : commonLengths) {
            float speed = RunSpeed.getMaxExtraRunSpeed(length);
            assertTrue(speed >= 0.0f,
                "Common runway length " + length + " should return valid speed");
            assertTrue(speed <= 10.0f,
                "Speed should be reasonable for length " + length);
        }
    }
}
