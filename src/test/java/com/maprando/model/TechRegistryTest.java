package com.maprando.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Tests for TechRegistry class.
 */
class TechRegistryTest {
    private TechRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TechRegistry();
        registry.registerTech(new TechDefinition("can_morph", "Can Morph", "Roll into ball", 0));
        registry.registerTech(new TechDefinition("can_fit_small_spaces", "Fit Small", "Fit through small", 1));
        registry.registerTech(new TechDefinition("can_place_bombs", "Place Bombs", "Place bombs", 2, List.of("can_morph")));
        registry.registerTech(new TechDefinition("can_shinespark", "Shinespark", "Execute shinespark", 3, List.of("can_speed_booster")));
    }

    @Test
    void testRegisterAndGetById() {
        TechDefinition tech = registry.getById("can_morph");
        assertNotNull(tech);
        assertEquals("can_morph", tech.getId());
        assertEquals(0, tech.getIndex());
    }

    @Test
    void testGetByIndex() {
        TechDefinition tech = registry.getByIndex(0);
        assertNotNull(tech);
        assertEquals("can_morph", tech.getId());
    }

    @Test
    void testGetByIdNotFound() {
        TechDefinition tech = registry.getById("nonexistent");
        assertNull(tech);
    }

    @Test
    void testGetByIndexNotFound() {
        TechDefinition tech = registry.getByIndex(999);
        assertNull(tech);
    }

    @Test
    void testHasTechById() {
        boolean[] techArray = registry.createTechArray();
        techArray[0] = true; // Enable can_morph

        assertTrue(registry.hasTech(techArray, "can_morph"));
        assertFalse(registry.hasTech(techArray, "can_fit_small_spaces"));
    }

    @Test
    void testHasTechByIndex() {
        boolean[] techArray = registry.createTechArray();
        techArray[0] = true;

        assertTrue(registry.hasTech(techArray, 0));
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
        assertEquals(4, techArray.length);
        assertFalse(techArray[0]);
        assertFalse(techArray[1]);
        assertFalse(techArray[2]);
        assertFalse(techArray[3]);
    }

    @Test
    void testGetEnabledTechs() {
        boolean[] techArray = registry.createTechArray();
        techArray[0] = true;
        techArray[2] = true;

        List<TechDefinition> enabled = registry.getEnabledTechs(techArray);
        assertEquals(2, enabled.size());
        assertTrue(enabled.stream().anyMatch(t -> t.getId().equals("can_morph")));
        assertTrue(enabled.stream().anyMatch(t -> t.getId().equals("can_place_bombs")));
    }

    @Test
    void testGetEnabledTechIds() {
        boolean[] techArray = registry.createTechArray();
        techArray[0] = true;
        techArray[2] = true;

        Set<String> enabledIds = registry.getEnabledTechIds(techArray);
        assertEquals(2, enabledIds.size());
        assertTrue(enabledIds.contains("can_morph"));
        assertTrue(enabledIds.contains("can_place_bombs"));
        assertFalse(enabledIds.contains("can_fit_small_spaces"));
    }

    @Test
    void testGetEnabledCount() {
        boolean[] techArray = registry.createTechArray();
        assertEquals(0, registry.getEnabledCount(techArray));

        techArray[0] = true;
        assertEquals(1, registry.getEnabledCount(techArray));

        techArray[1] = true;
        techArray[2] = true;
        assertEquals(3, registry.getEnabledCount(techArray));
    }

    @Test
    void testGetTechCount() {
        assertEquals(4, registry.getTechCount());
    }

    @Test
    void testGetAllTechs() {
        Collection<TechDefinition> allTechs = registry.getAllTechs();
        assertEquals(4, allTechs.size());
        assertTrue(allTechs.stream().anyMatch(t -> t.getId().equals("can_morph")));
        assertTrue(allTechs.stream().anyMatch(t -> t.getId().equals("can_fit_small_spaces")));
        assertTrue(allTechs.stream().anyMatch(t -> t.getId().equals("can_place_bombs")));
        assertTrue(allTechs.stream().anyMatch(t -> t.getId().equals("can_shinespark")));
    }

    @Test
    void testGetAllTechsIsUnmodifiable() {
        Collection<TechDefinition> allTechs = registry.getAllTechs();
        assertThrows(UnsupportedOperationException.class, () -> allTechs.clear());
    }
}
