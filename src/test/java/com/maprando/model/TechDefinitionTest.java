package com.maprando.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests for TechDefinition class.
 */
class TechDefinitionTest {

    @Test
    void testBasicTechDefinition() {
        TechDefinition tech = new TechDefinition(
            "can_morph",
            "Can Morph",
            "Can roll into morph ball",
            0
        );

        assertEquals("can_morph", tech.getId());
        assertEquals("Can Morph", tech.getName());
        assertEquals("Can roll into morph ball", tech.getDescription());
        assertEquals(0, tech.getIndex());
        assertNull(tech.getRequires());
    }

    @Test
    void testTechDefinitionWithRequires() {
        List<String> requires = List.of("can_morph");
        TechDefinition tech = new TechDefinition(
            "can_place_bombs",
            "Can Place Bombs",
            "Can place bombs while in morph ball",
            2,
            requires
        );

        assertEquals("can_place_bombs", tech.getId());
        assertEquals(requires, tech.getRequires());
        assertEquals(1, tech.getRequires().size());
        assertTrue(tech.getRequires().contains("can_morph"));
    }

    @Test
    void testTechDefinitionWithMultipleRequires() {
        List<String> requires = List.of("can_morph", "can_bomb_weak_walls");
        TechDefinition tech = new TechDefinition(
            "can_use_power_bombs",
            "Can Use Power Bombs",
            "Can use power bombs in morph ball",
            4,
            requires
        );

        assertEquals(2, tech.getRequires().size());
        assertTrue(tech.getRequires().contains("can_morph"));
        assertTrue(tech.getRequires().contains("can_bomb_weak_walls"));
    }

    @Test
    void testToString() {
        TechDefinition tech = new TechDefinition(
            "can_morph",
            "Can Morph",
            "Can roll into morph ball",
            0
        );

        String str = tech.toString();
        assertTrue(str.contains("can_morph"));
        assertTrue(str.contains("Can Morph"));
        assertTrue(str.contains("0"));
    }

    @Test
    void testEquals() {
        TechDefinition tech1 = new TechDefinition("can_morph", "Can Morph", "Desc", 0);
        TechDefinition tech2 = new TechDefinition("can_morph", "Different Name", "Desc", 0);
        TechDefinition tech3 = new TechDefinition("can_shinespark", "Can Shinespark", "Desc", 1);

        assertEquals(tech1, tech2); // Same ID
        assertNotEquals(tech1, tech3); // Different ID
        assertEquals(tech1, tech1); // Same object
        assertNotEquals(tech1, null); // Not equal to null
        assertNotEquals(tech1, "can_morph"); // Not equal to different type
    }

    @Test
    void testHashCode() {
        TechDefinition tech1 = new TechDefinition("can_morph", "Can Morph", "Desc", 0);
        TechDefinition tech2 = new TechDefinition("can_morph", "Different Name", "Desc", 0);
        TechDefinition tech3 = new TechDefinition("can_shinespark", "Can Shinespark", "Desc", 1);

        assertEquals(tech1.hashCode(), tech2.hashCode()); // Same ID = same hash
        assertNotEquals(tech1.hashCode(), tech3.hashCode()); // Different ID = different hash
    }
}
