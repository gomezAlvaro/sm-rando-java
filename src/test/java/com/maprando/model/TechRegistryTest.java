package com.maprando.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Tests for TechRegistry class aligned with Rust tech_data.json format.
 */
class TechRegistryTest {
    private TechRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TechRegistry();
        // Register techs with Rust format: (techId, name, difficulty, videoId)
        registry.registerTech(new TechDefinition(1, "can_morph", "Basic", null));
        registry.registerTech(new TechDefinition(32, "canMidAirMorph", "Advanced", null));
        registry.registerTech(new TechDefinition(76, "canWalljump", "Basic", null));
        registry.registerTech(new TechDefinition(132, "canShinespark", "Advanced", null));
    }

    @Test
    void testRegisterAndGetByName() {
        TechDefinition tech = registry.getByName("can_morph");
        assertNotNull(tech);
        assertEquals("can_morph", tech.getName());
        assertEquals(1, tech.getTechId());
    }

    @Test
    void testGetByTechId() {
        TechDefinition tech = registry.getByTechId(76);
        assertNotNull(tech);
        assertEquals("canWalljump", tech.getName());
        assertEquals(76, tech.getTechId());
    }

    @Test
    void testGetByIdCompatibility() {
        TechDefinition tech = registry.getById("can_morph");
        assertNotNull(tech);
        assertEquals("can_morph", tech.getName());
    }

    @Test
    void testGetByIndexCompatibility() {
        TechDefinition tech = registry.getByIndex(76);
        assertNotNull(tech);
        assertEquals("canWalljump", tech.getName());
    }

    @Test
    void testGetByNameNotFound() {
        TechDefinition tech = registry.getByName("nonexistent");
        assertNull(tech);
    }

    @Test
    void testGetByTechIdNotFound() {
        TechDefinition tech = registry.getByTechId(999);
        assertNull(tech);
    }

    @Test
    void testHasTechByName() {
        boolean[] techArray = registry.createTechArray();
        techArray[1] = true; // Enable can_morph (tech_id = 1)

        assertTrue(registry.hasTech(techArray, "can_morph"));
        assertFalse(registry.hasTech(techArray, "canWalljump"));
    }

    @Test
    void testHasTechByTechId() {
        boolean[] techArray = registry.createTechArray();
        techArray[76] = true; // Enable canWalljump

        assertTrue(registry.hasTech(techArray, 76));
        assertFalse(registry.hasTech(techArray, 1));
    }

    @Test
    void testHasTechInvalidIndex() {
        boolean[] techArray = registry.createTechArray();
        assertFalse(registry.hasTech(techArray, -1));
        assertFalse(registry.hasTech(techArray, 999));
    }

    @Test
    void testCreateTechArray() {
        boolean[] techArray = registry.createTechArray();
        // Array should be sized to max tech_id + 1 = 132 + 1 = 133
        assertEquals(133, techArray.length);
        assertFalse(techArray[0]);
        assertFalse(techArray[1]);
        assertFalse(techArray[76]);
        assertFalse(techArray[132]);
    }

    @Test
    void testGetEnabledTechs() {
        boolean[] techArray = registry.createTechArray();
        techArray[1] = true;   // can_morph
        techArray[76] = true;  // canWalljump

        List<TechDefinition> enabled = registry.getEnabledTechs(techArray);
        assertEquals(2, enabled.size());
        assertTrue(enabled.stream().anyMatch(t -> t.getName().equals("can_morph")));
        assertTrue(enabled.stream().anyMatch(t -> t.getName().equals("canWalljump")));
    }

    @Test
    void testGetEnabledTechNames() {
        boolean[] techArray = registry.createTechArray();
        techArray[1] = true;   // can_morph
        techArray[76] = true;  // canWalljump

        Set<String> enabledNames = registry.getEnabledTechNames(techArray);
        assertEquals(2, enabledNames.size());
        assertTrue(enabledNames.contains("can_morph"));
        assertTrue(enabledNames.contains("canWalljump"));
        assertFalse(enabledNames.contains("canShinespark"));
    }

    @Test
    void testGetEnabledTechIdsCompatibility() {
        boolean[] techArray = registry.createTechArray();
        techArray[1] = true;   // can_morph
        techArray[76] = true;  // canWalljump

        Set<String> enabledIds = registry.getEnabledTechIds(techArray);
        assertEquals(2, enabledIds.size());
        assertTrue(enabledIds.contains("can_morph"));
        assertTrue(enabledIds.contains("canWalljump"));
    }

    @Test
    void testGetEnabledCount() {
        boolean[] techArray = registry.createTechArray();
        assertEquals(0, registry.getEnabledCount(techArray));

        techArray[1] = true;
        assertEquals(1, registry.getEnabledCount(techArray));

        techArray[76] = true;
        techArray[132] = true;
        assertEquals(3, registry.getEnabledCount(techArray));
    }

    @Test
    void testGetTechCount() {
        assertEquals(4, registry.getTechCount());
    }

    @Test
    void testGetMaxTechId() {
        assertEquals(132, registry.getMaxTechId());
    }

    @Test
    void testGetAllTechs() {
        Collection<TechDefinition> allTechs = registry.getAllTechs();
        assertEquals(4, allTechs.size());
        assertTrue(allTechs.stream().anyMatch(t -> t.getName().equals("can_morph")));
        assertTrue(allTechs.stream().anyMatch(t -> t.getName().equals("canMidAirMorph")));
        assertTrue(allTechs.stream().anyMatch(t -> t.getName().equals("canWalljump")));
        assertTrue(allTechs.stream().anyMatch(t -> t.getName().equals("canShinespark")));
    }

    @Test
    void testGetAllTechsIsUnmodifiable() {
        Collection<TechDefinition> allTechs = registry.getAllTechs();
        assertThrows(UnsupportedOperationException.class, () -> allTechs.clear());
    }
}
