package com.maprando.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests for ItemDefinition class with enhanced JSON fields.
 */
class ItemDefinitionTest {

    @Test
    void testBasicItemDefinition() {
        ItemDefinition def = new ItemDefinition(
            "TEST_ITEM",
            "Test Item",
            "A test item",
            "beam",
            true,
            0
        );

        assertEquals("TEST_ITEM", def.getId());
        assertEquals("Test Item", def.getDisplayName());
        assertEquals("A test item", def.getDescription());
        assertEquals("beam", def.getCategory());
        assertTrue(def.isProgression());
        assertEquals(0, def.getIndex());
    }

    @Test
    void testItemDefinitionWithDamageMultiplier() {
        ItemDefinition def = new ItemDefinition(
            "CHARGE_BEAM",
            "Charge Beam",
            "Allows charge shots",
            "beam",
            true,
            0,
            Double.valueOf(3.0),
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertEquals(3.0, def.getDamageMultiplier());
        assertNull(def.getDamageBonus());
        assertNull(def.getDamageReduction());
        assertNull(def.getRequires());
        assertNull(def.getEnables());
    }

    @Test
    void testItemDefinitionWithDamageBonus() {
        ItemDefinition def = new ItemDefinition(
            "ICE_BEAM",
            "Ice Beam",
            "Freezes enemies",
            "beam",
            true,
            1,
            null,
            Integer.valueOf(5),
            null,
            null,
            null,
            null,
            null
        );

        assertNull(def.getDamageMultiplier());
        assertEquals(5, def.getDamageBonus());
        assertNull(def.getDamageReduction());
    }

    @Test
    void testItemDefinitionWithDamageReduction() {
        ItemDefinition def = new ItemDefinition(
            "VARIA_SUIT",
            "Varia Suit",
            "Reduces damage taken",
            "suit",
            true,
            13,
            null,
            null,
            Double.valueOf(0.5),
            null,
            null,
            null,
            null
        );

        assertEquals(0.5, def.getDamageReduction());
        assertNull(def.getDamageMultiplier());
        assertNull(def.getDamageBonus());
    }

    @Test
    void testItemDefinitionWithRequires() {
        List<String> requires = List.of("can_morph");
        ItemDefinition def = new ItemDefinition(
            "BOMB",
            "Bomb",
            "Morph ball bombs",
            "morph",
            true,
            6,
            null,
            null,
            null,
            requires,
            null,
            null,
            null
        );

        assertEquals(requires, def.getRequires());
        assertNull(def.getEnables());
    }

    @Test
    void testItemDefinitionWithEnables() {
        List<String> enables = List.of("can_morph", "can_fit_small_spaces");
        ItemDefinition def = new ItemDefinition(
            "MORPH_BALL",
            "Morph Ball",
            "Roll into a ball",
            "movement",
            true,
            5,
            null,
            null,
            null,
            null,
            enables,
            null,
            null
        );

        assertEquals(enables, def.getEnables());
        assertNull(def.getRequires());
    }

    @Test
    void testItemDefinitionWithTankProperties() {
        ItemDefinition def = new ItemDefinition(
            "MISSILE_TANK",
            "Missile Tank",
            "Increases missile capacity by 5",
            "tank",
            false,
            17,
            null,
            null,
            null,
            null,
            null,
            "MISSILE",
            Integer.valueOf(5)
        );

        assertEquals("MISSILE", def.getResourceType());
        assertEquals(5, def.getCapacityIncrease());
    }

    @Test
    void testGravitySuitWithAllProperties() {
        List<String> enables = List.of("can_swim_lava", "can_move_underwater");
        ItemDefinition def = new ItemDefinition(
            "GRAVITY_SUIT",
            "Gravity Suit",
            "Lava protection + damage reduction",
            "suit",
            true,
            14,
            null,
            null,
            Double.valueOf(0.75),
            null,
            enables,
            null,
            null
        );

        assertEquals(0.75, def.getDamageReduction());
        assertEquals(enables, def.getEnables());
        assertTrue(def.isSuit());
    }

    @Test
    void testIsBeam() {
        ItemDefinition beam = new ItemDefinition(
            "PLASMA_BEAM", "Plasma Beam", "", "beam", true, 4
        );
        assertTrue(beam.isBeam());

        ItemDefinition notBeam = new ItemDefinition(
            "MORPH_BALL", "Morph Ball", "", "movement", true, 5
        );
        assertFalse(notBeam.isBeam());
    }

    @Test
    void testIsTank() {
        ItemDefinition tank = new ItemDefinition(
            "MISSILE_TANK", "Missile Tank", "", "tank", false, 17,
            null, null, null, null, null, "MISSILE", 5
        );
        assertTrue(tank.isTank());

        ItemDefinition notTank = new ItemDefinition(
            "MORPH_BALL", "Morph Ball", "", "movement", true, 5
        );
        assertFalse(notTank.isTank());
    }

    @Test
    void testIsMorphBallAbility() {
        ItemDefinition morph = new ItemDefinition(
            "BOMB", "Bomb", "", "morph", true, 6
        );
        assertTrue(morph.isMorphBallAbility());

        ItemDefinition notMorph = new ItemDefinition(
            "MORPH_BALL", "Morph Ball", "", "movement", true, 5
        );
        assertFalse(notMorph.isMorphBallAbility());
    }

    @Test
    void testIsSuit() {
        ItemDefinition suit = new ItemDefinition(
            "GRAVITY_SUIT", "Gravity Suit", "", "suit", true, 14
        );
        assertTrue(suit.isSuit());

        ItemDefinition notSuit = new ItemDefinition(
            "MORPH_BALL", "Morph Ball", "", "movement", true, 5
        );
        assertFalse(notSuit.isSuit());
    }

    @Test
    void testAllFieldsCombined() {
        List<String> requires = List.of("can_morph");
        List<String> enables = List.of("can_place_bombs", "can_bomb_weak_walls");

        ItemDefinition def = new ItemDefinition(
            "POWER_BOMB",
            "Power Bomb",
            "Large morph ball explosions",
            "morph",
            false,
            8,
            null,
            null,
            null,
            requires,
            enables,
            null,
            null
        );

        assertEquals("POWER_BOMB", def.getId());
        assertEquals("Power Bomb", def.getDisplayName());
        assertFalse(def.isProgression());
        assertEquals(requires, def.getRequires());
        assertEquals(enables, def.getEnables());
        assertTrue(def.isMorphBallAbility());
    }
}
