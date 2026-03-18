package com.maprando.data.model;

import com.maprando.data.model.LocationData.LocationDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

/**
 * Unit tests for the LocationData model class.
 */
@DisplayName("LocationData Model Tests")
class LocationDataModelTest {

    private LocationData locationData;

    @BeforeEach
    void setUp() {
        locationData = new LocationData();
    }

    @Test
    @DisplayName("LocationData should be created successfully")
    void testLocationDataCreation() {
        assertNotNull(locationData, "LocationData should be created");
        // Locations list is null until loaded from JSON or set manually
        // This is expected behavior for JSON deserialization
    }

    @Test
    @DisplayName("Set and get locations should work correctly")
    void testSetGetLocations() {
        List<LocationDefinition> locations = List.of(
                createTestLocation("loc1", "Location 1", "Brinstar", false),
                createTestLocation("loc2", "Location 2", "Norfair", true)
        );

        locationData.setLocations(locations);

        assertEquals(2, locationData.getLocations().size(), "Should have 2 locations");
        assertEquals("loc1", locationData.getLocations().get(0).getId(), "First location ID should match");
    }

    @Test
    @DisplayName("LocationDefinition should store all properties correctly")
    void testLocationDefinitionProperties() {
        LocationDefinition location = createTestLocation("test_id", "Test Location", "Maridia", false);

        assertEquals("test_id", location.getId(), "ID should match");
        assertEquals("Test Location", location.getName(), "Name should match");
        assertEquals("Maridia", location.getRegion(), "Region should match");
        assertEquals("Maridia", location.getArea(), "Area should match");
        assertFalse(location.isEarlyGame(), "Early game flag should match");
        assertFalse(location.isBoss(), "Boss flag should match");
    }

    @Test
    @DisplayName("LocationDefinition with empty requirements should work")
    void testLocationDefinitionWithEmptyRequirements() {
        LocationDefinition location = new LocationDefinition();
        location.setId("test_loc");
        location.setName("Test Location");
        location.setRegion("Brinstar");
        location.setArea("Brinstar");
        location.setRequirements(List.of());
        location.setEarlyGame(true);
        location.setBoss(false);

        assertNotNull(location.getRequirements(), "Requirements list should be initialized");
        assertTrue(location.getRequirements().isEmpty(), "Requirements should be empty");
        assertTrue(location.isEarlyGame(), "Should be early game");
    }

    @Test
    @DisplayName("LocationDefinition with requirements should store correctly")
    void testLocationDefinitionWithRequirements() {
        LocationDefinition location = new LocationDefinition();
        location.setId("test_loc");
        location.setName("Test Location");
        location.setRegion("Norfair");
        location.setArea("Norfair");
        location.setRequirements(List.of("can_morph", "has_bombs"));
        location.setEarlyGame(false);
        location.setBoss(true);

        assertEquals(2, location.getRequirements().size(), "Should have 2 requirements");
        assertTrue(location.getRequirements().contains("can_morph"), "Should contain can_morph");
        assertTrue(location.getRequirements().contains("has_bombs"), "Should contain has_bombs");
        assertFalse(location.isEarlyGame(), "Should not be early game");
        assertTrue(location.isBoss(), "Should be boss location");
    }

    @Test
    @DisplayName("Multiple LocationDefinitions should be stored independently")
    void testMultipleLocationDefinitions() {
        List<LocationDefinition> locations = List.of(
                createTestLocation("loc1", "Location 1", "Brinstar", true),
                createTestLocation("loc2", "Location 2", "Norfair", false),
                createTestLocation("loc3", "Location 3", "Maridia", true)
        );

        locationData.setLocations(locations);

        assertEquals(3, locationData.getLocations().size(), "Should have 3 locations");
        assertEquals("loc1", locationData.getLocations().get(0).getId());
        assertEquals("loc2", locationData.getLocations().get(1).getId());
        assertEquals("loc3", locationData.getLocations().get(2).getId());
    }

    @Test
    @DisplayName("LocationDefinition with all properties set should work")
    void testLocationDefinitionWithAllProperties() {
        LocationDefinition location = new LocationDefinition();
        location.setId("full_test");
        location.setName("Full Test Location");
        location.setRegion("Wrecked Ship");
        location.setArea("Wrecked Ship");
        location.setRequirements(List.of("can_grapple"));
        location.setEarlyGame(false);
        location.setBoss(false);

        assertEquals("full_test", location.getId());
        assertEquals("Full Test Location", location.getName());
        assertEquals("Wrecked Ship", location.getRegion());
        assertEquals("Wrecked Ship", location.getArea());
        assertEquals(1, location.getRequirements().size());
        assertEquals("can_grapple", location.getRequirements().get(0));
    }

    @Test
    @DisplayName("LocationDefinition should handle null requirements")
    void testLocationDefinitionNullRequirements() {
        LocationDefinition location = new LocationDefinition();
        location.setId("null_req_test");
        location.setName("Null Requirements Test");
        location.setRegion("Test");
        location.setArea("Test");
        location.setRequirements(null);
        location.setEarlyGame(true);
        location.setBoss(false);

        // Should handle null requirements gracefully
        assertNull(location.getRequirements(), "Requirements should be null when set to null");
    }

    @Test
    @DisplayName("LocationDefinition with boss flag should store correctly")
    void testLocationDefinitionBossFlag() {
        LocationDefinition location = new LocationDefinition();
        location.setId("boss_loc");
        location.setName("Boss Room");
        location.setRegion("Norfair");
        location.setArea("Norfair");
        location.setRequirements(List.of());
        location.setEarlyGame(false);
        location.setBoss(true);

        assertTrue(location.isBoss(), "Should be marked as boss location");
    }

    // Helper method to create test locations
    private LocationDefinition createTestLocation(String id, String name, String region, boolean isEarlyGame) {
        LocationDefinition location = new LocationDefinition();
        location.setId(id);
        location.setName(name);
        location.setRegion(region);
        location.setArea(region); // Area defaults to region for test
        location.setRequirements(List.of());
        location.setEarlyGame(isEarlyGame);
        location.setBoss(false);
        return location;
    }
}
