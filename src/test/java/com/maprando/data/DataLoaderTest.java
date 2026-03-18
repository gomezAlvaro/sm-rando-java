package com.maprando.data;

import com.maprando.data.model.ItemData;
import com.maprando.data.model.LocationData;
import com.maprando.model.ItemDefinition;
import com.maprando.model.ResourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * Unit tests for the DataLoader class.
 */
@DisplayName("DataLoader Tests")
class DataLoaderTest {

    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        dataLoader = new DataLoader();
    }

    @Test
    @DisplayName("DataLoader should be created successfully")
    void testDataLoaderCreation() {
        assertNotNull(dataLoader, "DataLoader should be created");
    }

    @Test
    @DisplayName("Load all data should successfully load all JSON files")
    void testLoadAllData() throws IOException {
        dataLoader.loadAllData();

        assertNotNull(dataLoader.getItemData(), "Item data should be loaded");
        assertNotNull(dataLoader.getLocationData(), "Location data should be loaded");
        assertEquals(21, dataLoader.getItemData().getItems().size(),
                "Should load 21 items");
        assertEquals(15, dataLoader.getLocationData().getLocations().size(),
                "Should load 15 locations");
    }

    @Test
    @DisplayName("Get item definition should return correct item")
    void testGetItemDefinition() throws IOException {
        dataLoader.loadAllData();

        ItemData.ItemDefinition chargeBeam = dataLoader.getItemDefinition("CHARGE_BEAM");

        assertNotNull(chargeBeam, "Charge Beam definition should be found");
        assertEquals("CHARGE_BEAM", chargeBeam.getId(), "ID should match");
        assertEquals("Charge Beam", chargeBeam.getDisplayName(), "Display name should match");
        assertEquals("beam", chargeBeam.getCategory(), "Category should be beam");
        assertTrue(chargeBeam.isProgression(), "Charge Beam should be progression");
    }

    @Test
    @DisplayName("Get location definition should return correct location")
    void testGetLocationDefinition() throws IOException {
        dataLoader.loadAllData();

        LocationData.LocationDefinition morphRoom = dataLoader.getLocationDefinition("brinstar_morph_ball_room");

        assertNotNull(morphRoom, "Morph Ball Room definition should be found");
        assertEquals("brinstar_morph_ball_room", morphRoom.getId(), "ID should match");
        assertEquals("Morph Ball Room", morphRoom.getName(), "Name should match");
        assertEquals("Brinstar", morphRoom.getRegion(), "Region should be Brinstar");
        assertTrue(morphRoom.isEarlyGame(), "Should be early game location");
    }

    @Test
    @DisplayName("Get non-existent item should return null")
    void testGetNonExistentItem() throws IOException {
        dataLoader.loadAllData();

        ItemData.ItemDefinition fakeItem = dataLoader.getItemDefinition("FAKE_ITEM");

        assertNull(fakeItem, "Non-existent item should return null");
    }

    @Test
    @DisplayName("Get non-existent location should return null")
    void testGetNonExistentLocation() throws IOException {
        dataLoader.loadAllData();

        LocationData.LocationDefinition fakeLocation = dataLoader.getLocationDefinition("fake_location");

        assertNull(fakeLocation, "Non-existent location should return null");
    }

    @Test
    @DisplayName("JSON ID to ItemDefinition conversion should work for valid IDs")
    void testJsonIdToItemValid() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition chargeBeam = dataLoader.getItemRegistry().getById("CHARGE_BEAM");
        ItemDefinition morphBall = dataLoader.getItemRegistry().getById("MORPH_BALL");

        assertNotNull(chargeBeam, "Should find CHARGE_BEAM");
        assertEquals("CHARGE_BEAM", chargeBeam.getId(), "Should convert CHARGE_BEAM ID");
        assertNotNull(morphBall, "Should find MORPH_BALL");
        assertEquals("MORPH_BALL", morphBall.getId(), "Should convert MORPH_BALL ID");
    }

    @Test
    @DisplayName("JSON ID to ItemDefinition conversion should return null for invalid IDs")
    void testJsonIdToItemInvalid() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition fakeItem = dataLoader.getItemRegistry().getById("FAKE_ITEM");

        assertNull(fakeItem, "Invalid ID should return null");
    }

    @Test
    @DisplayName("JSON string to ResourceType conversion should work for valid types")
    void testJsonStringToResourceTypeValid() throws IOException {
        dataLoader.loadAllData();

        ResourceType energy = dataLoader.jsonStringToResourceType("ENERGY");
        ResourceType missile = dataLoader.jsonStringToResourceType("MISSILE");

        assertEquals(ResourceType.ENERGY, energy, "Should convert ENERGY string");
        assertEquals(ResourceType.MISSILE, missile, "Should convert MISSILE string");
    }

    @Test
    @DisplayName("JSON string to ResourceType conversion should return null for invalid types")
    void testJsonStringToResourceTypeInvalid() throws IOException {
        dataLoader.loadAllData();

        ResourceType fake = dataLoader.jsonStringToResourceType("FAKE_RESOURCE");

        assertNull(fake, "Invalid resource type should return null");
    }

    @Test
    @DisplayName("Validate data should pass for valid JSON files")
    void testValidateDataValid() throws IOException {
        dataLoader.loadAllData();

        assertTrue(dataLoader.validateData(), "Valid data should pass validation");
    }

    @Test
    @DisplayName("Get all item definitions should return complete list")
    void testGetAllItemDefinitions() throws IOException {
        dataLoader.loadAllData();

        assertEquals(21, dataLoader.getItemData().getItems().size(),
                "Should have 21 item definitions");
    }

    @Test
    @DisplayName("Get all location definitions should return complete list")
    void testGetAllLocationDefinitions() throws IOException {
        dataLoader.loadAllData();

        assertEquals(15, dataLoader.getLocationData().getLocations().size(),
                "Should have 15 location definitions");
    }
}
