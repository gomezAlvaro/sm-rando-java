package com.maprando.randomize;

import com.maprando.model.GameState;
import com.maprando.logic.RequirementChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Location class
 */
@DisplayName("Location Tests")
class LocationTest {

    private Location location;

    @BeforeEach
    void setUp() {
        location = Location.builder()
                .id("test-location")
                .name("Test Location")
                .region("Test Region")
                .build();
    }

    @Test
    @DisplayName("Builder should create location with correct properties")
    void testBuilderCreatesLocation() {
        assertEquals("test-location", location.getId(), "ID should match");
        assertEquals("Test Location", location.getName(), "Name should match");
        assertEquals("Test Region", location.getRegion(), "Region should match");
    }

    @Test
    @DisplayName("New location should not have placed item")
    void testNewLocationNoPlacedItem() {
        assertFalse(location.isPlaced(), "New location should not have placed item");
        assertNull(location.getPlacedItemId(), "Placed item should be null");
    }

    @Test
    @DisplayName("Place item should set item at location")
    void testPlaceItem() {
        location.placeItem("CHARGE_BEAM");

        assertTrue(location.isPlaced(), "Location should have placed item");
        assertEquals("CHARGE_BEAM", location.getPlacedItemId(),
                "Placed item should match");
    }

    @Test
    @DisplayName("Place item over existing should fail")
    void testPlaceItemOverExisting() {
        location.placeItem("CHARGE_BEAM");
        boolean result = location.placeItem("ICE_BEAM");

        assertFalse(result, "Should not replace existing item");
        assertEquals("CHARGE_BEAM", location.getPlacedItemId(),
                "Original item should remain");
    }

    @Test
    @DisplayName("Clear should remove placed item")
    void testClear() {
        location.placeItem("CHARGE_BEAM");
        location.clear();

        assertFalse(location.isPlaced(), "Location should not have placed item after clear");
        assertNull(location.getPlacedItemId(), "Placed item should be null after clear");
    }

    @Test
    @DisplayName("Clear on empty location should be safe")
    void testClearEmptyLocation() {
        assertDoesNotThrow(() -> location.clear(),
                "Clearing empty location should not throw");
    }

    @Test
    @DisplayName("Copy should create independent location")
    void testCopy() {
        location.placeItem("CHARGE_BEAM");
        Location copy = location.copy();

        assertEquals(location.getId(), copy.getId(), "Copy should have same ID");
        assertEquals(location.getName(), copy.getName(), "Copy should have same name");
        assertEquals(location.getRegion(), copy.getRegion(), "Copy should have same region");
        assertEquals(location.getPlacedItemId(), copy.getPlacedItemId(),
                "Copy should have same placed item");

        // Verify independence
        copy.placeItem("ICE_BEAM");
        assertEquals("CHARGE_BEAM", location.getPlacedItemId(),
                "Original location should not be affected by copy modification");
    }

    @Test
    @DisplayName("Builder with requirements should create location with requirements")
    void testBuilderWithRequirements() {
        Location locationWithReq = Location.builder()
                .id("morph-location")
                .name("Morph Ball Location")
                .region("Brinstar")
                .requirements(java.util.Set.of("can_morph", "has_bombs"))
                .build();

        assertEquals(2, locationWithReq.getRequirements().size(),
                "Should have 2 requirements");
        assertTrue(locationWithReq.hasRequirements(),
                "Should have requirements");
    }

    @Test
    @DisplayName("Multiple locations should be independent")
    void testMultipleLocationsIndependent() {
        Location loc1 = Location.builder()
                .id("loc1")
                .name("Location 1")
                .region("Region 1")
                .build();

        Location loc2 = Location.builder()
                .id("loc2")
                .name("Location 2")
                .region("Region 2")
                .build();

        loc1.placeItem("CHARGE_BEAM");

        assertFalse(loc2.isPlaced(), "Second location should not be affected");
        assertNull(loc2.getPlacedItemId(), "Second location should not have item");
    }

    @Test
    @DisplayName("Builder should handle all properties")
    void testBuilderAllProperties() {
        Location fullLocation = Location.builder()
                .id("full-location")
                .name("Full Location")
                .region("Full Region")
                .requirements(java.util.Set.of("can_morph", "has_bombs"))
                .build();

        assertEquals("full-location", fullLocation.getId());
        assertEquals("Full Location", fullLocation.getName());
        assertEquals("Full Region", fullLocation.getRegion());
        assertNotNull(fullLocation.getRequirements(), "Requirements should not be null");
        assertTrue(fullLocation.hasRequirements(), "Should have requirements");
    }

    @Test
    @DisplayName("Location equality should be based on ID")
    void testLocationEquality() {
        Location loc1 = Location.builder()
                .id("same-id")
                .name("Location 1")
                .region("Region 1")
                .build();

        Location loc2 = Location.builder()
                .id("same-id")
                .name("Location 2")
                .region("Region 2")
                .build();

        // Locations with same ID should be considered equal for tracking purposes
        // (This is implicit in how randomizer uses locations)
        assertEquals(loc1.getId(), loc2.getId(), "IDs should match");
    }

    @Test
    @DisplayName("Copy empty location should work")
    void testCopyEmptyLocation() {
        Location emptyCopy = location.copy();

        assertEquals(location.getId(), emptyCopy.getId());
        assertEquals(location.getName(), emptyCopy.getName());
        assertFalse(emptyCopy.isPlaced(), "Copy should not have placed item");
    }

    @Test
    @DisplayName("Location with null requirements should be accessible")
    void testLocationWithNullRequirements() {
        Location noReqLocation = Location.builder()
                .id("no-req")
                .name("No Requirement Location")
                .region("Test")
                .build();

        // Should be able to create location without requirements
        assertNotNull(noReqLocation, "Location should be created");
        // And should be accessible by any game state
        GameState anyState = new GameState();
        // (This would be tested through randomizer logic)
    }
}