package com.maprando.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TechDefinition class aligned with Rust tech_data.json format.
 */
class TechDefinitionTest {

    @Test
    void testBasicTechDefinition() {
        TechDefinition tech = new TechDefinition(
            1,  // tech_id
            "can_morph",  // name
            "Basic",  // difficulty
            null  // video_id (optional)
        );

        assertEquals(1, tech.getTechId());
        assertEquals("can_morph", tech.getName());
        assertEquals("Basic", tech.getDifficulty());
        assertNull(tech.getVideoId());
    }

    @Test
    void testTechDefinitionWithVideo() {
        TechDefinition tech = new TechDefinition(
            6,  // tech_id
            "canHeatRun",  // name
            "Basic",  // difficulty
            899  // video_id
        );

        assertEquals(6, tech.getTechId());
        assertEquals("canHeatRun", tech.getName());
        assertEquals("Basic", tech.getDifficulty());
        assertEquals(899, tech.getVideoId());
    }

    @Test
    void testCompatibilityMethods() {
        TechDefinition tech = new TechDefinition(
            76,  // tech_id for canWalljump
            "canWalljump",
            "Basic",
            null
        );

        assertEquals(76, tech.getIndex());  // techId maps to index
        assertEquals("canWalljump", tech.getId());  // name maps to ID
        assertTrue(tech.getDescription().contains("Basic"));
    }

    @Test
    void testToString() {
        TechDefinition tech = new TechDefinition(
            6,
            "canHeatRun",
            "Basic",
            899
        );

        String str = tech.toString();
        assertTrue(str.contains("canHeatRun"));
        assertTrue(str.contains("6"));
        assertTrue(str.contains("Basic"));
        assertTrue(str.contains("899"));
    }

    @Test
    void testEquals() {
        TechDefinition tech1 = new TechDefinition(1, "can_morph", "Basic", null);
        TechDefinition tech2 = new TechDefinition(1, "different_name", "Hard", 100);
        TechDefinition tech3 = new TechDefinition(2, "can_shinespark", "Advanced", null);

        assertEquals(tech1, tech2);  // Same techId
        assertNotEquals(tech1, tech3);  // Different techId
        assertEquals(tech1, tech1);  // Same object
        assertNotEquals(tech1, null);  // Not equal to null
        assertNotEquals(tech1, "can_morph");  // Not equal to different type
    }

    @Test
    void testHashCode() {
        TechDefinition tech1 = new TechDefinition(1, "can_morph", "Basic", null);
        TechDefinition tech2 = new TechDefinition(1, "different", "Hard", 100);
        TechDefinition tech3 = new TechDefinition(2, "can_shinespark", "Advanced", null);

        assertEquals(tech1.hashCode(), tech2.hashCode());  // Same techId = same hash
        assertNotEquals(tech1.hashCode(), tech3.hashCode());  // Different techId = different hash
    }
}
