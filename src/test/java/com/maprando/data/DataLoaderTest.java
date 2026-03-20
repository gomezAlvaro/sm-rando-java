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

/**
 * Unit tests for the DataLoader class.
 * Aligned with Rust MapRandomizer architecture.
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
        assertEquals(22, dataLoader.getItemData().getItems().size(),
                "Should load 22 items (matching Rust MapRandomizer)");
        assertEquals(100, dataLoader.getLocationData().getLocations().size(),
                "Should load 100 locations from Rust room_geometry.json");
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

        LocationData.LocationDefinition moatRoom = dataLoader.getLocationDefinition("crateria_the_moat");

        assertNotNull(moatRoom, "The Moat definition should be found");
        assertEquals("crateria_the_moat", moatRoom.getId(), "ID should match");
        assertEquals("The Moat", moatRoom.getName(), "Name should match");
        assertEquals("Crateria", moatRoom.getRegion(), "Region should be Crateria");
        assertTrue(moatRoom.isEarlyGame(), "Should be early game location");
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

        assertEquals(22, dataLoader.getItemData().getItems().size(),
                "Should have 22 item definitions (matching Rust)");
    }

    @Test
    @DisplayName("Get all location definitions should return complete list")
    void testGetAllLocationDefinitions() throws IOException {
        dataLoader.loadAllData();

        assertEquals(100, dataLoader.getLocationData().getLocations().size(),
                "Should have 100 location definitions from Rust room_geometry.json");
    }

    // Enhanced field tests

    @Test
    @DisplayName("Charge Beam should have damage multiplier from JSON")
    void testChargeBeamHasDamageMultiplier() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition chargeBeam = dataLoader.getItemRegistry().getById("CHARGE_BEAM");
        assertNotNull(chargeBeam);
        assertEquals(3.0, chargeBeam.getDamageMultiplier());
        assertTrue(chargeBeam.isBeam());
        assertTrue(chargeBeam.isProgression());
    }

    @Test
    @DisplayName("Ice Beam should have damage bonus from JSON")
    void testIceBeamHasDamageBonus() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition iceBeam = dataLoader.getItemRegistry().getById("ICE_BEAM");
        assertNotNull(iceBeam);
        assertEquals(5, iceBeam.getDamageBonus());
        assertTrue(iceBeam.isBeam());
    }

    @Test
    @DisplayName("Spazer Beam should have damage multiplier from JSON")
    void testSpazerBeamHasDamageMultiplier() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition spazerBeam = dataLoader.getItemRegistry().getById("SPAZER_BEAM");
        assertNotNull(spazerBeam);
        assertEquals(2.0, spazerBeam.getDamageMultiplier());
        assertTrue(spazerBeam.isBeam());
        assertTrue(spazerBeam.isProgression());
    }

    @Test
    @DisplayName("Plasma Beam should have damage multiplier from JSON")
    void testPlasmaBeamHasDamageMultiplier() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition plasmaBeam = dataLoader.getItemRegistry().getById("PLASMA_BEAM");
        assertNotNull(plasmaBeam);
        assertEquals(4.0, plasmaBeam.getDamageMultiplier());
        assertTrue(plasmaBeam.isBeam());
        assertTrue(plasmaBeam.isProgression());
    }

    @Test
    @DisplayName("Wave Beam should have damage bonus from JSON")
    void testWaveBeamHasDamageBonus() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition waveBeam = dataLoader.getItemRegistry().getById("WAVE_BEAM");
        assertNotNull(waveBeam);
        assertEquals(3, waveBeam.getDamageBonus());
        assertTrue(waveBeam.isBeam());
    }

    @Test
    @DisplayName("Varia Suit should have damage reduction from JSON")
    void testVariaSuitHasDamageReduction() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition variaSuit = dataLoader.getItemRegistry().getById("VARIA_SUIT");
        assertNotNull(variaSuit);
        assertEquals(0.5, variaSuit.getDamageReduction());
        assertTrue(variaSuit.isSuit());
        assertTrue(variaSuit.isProgression());
    }

    @Test
    @DisplayName("Gravity Suit should have damage reduction from JSON")
    void testGravitySuitHasDamageReduction() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition gravitySuit = dataLoader.getItemRegistry().getById("GRAVITY_SUIT");
        assertNotNull(gravitySuit);
        assertEquals(0.0, gravitySuit.getDamageReduction());
        assertTrue(gravitySuit.isSuit());
        assertTrue(gravitySuit.isProgression());
    }

    @Test
    @DisplayName("Missile should have resource properties from JSON")
    void testMissileTankHasResourceProperties() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition missileTank = dataLoader.getItemRegistry().getById("MISSILE");
        assertNotNull(missileTank);
        assertTrue(missileTank.isTank());
        assertFalse(missileTank.isProgression());

        assertEquals("MISSILE", missileTank.getResourceType());
        assertEquals(5, missileTank.getCapacityIncrease());
    }

    @Test
    @DisplayName("Super Missile should have resource properties from JSON")
    void testSuperMissileTankHasResourceProperties() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition superMissileTank = dataLoader.getItemRegistry().getById("SUPER_MISSILE");
        assertNotNull(superMissileTank);
        assertTrue(superMissileTank.isTank());

        assertEquals("SUPER_MISSILE", superMissileTank.getResourceType());
        assertEquals(5, superMissileTank.getCapacityIncrease());
    }

    @Test
    @DisplayName("Power Bomb should have resource properties from JSON")
    void testPowerBombTankHasResourceProperties() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition powerBombTank = dataLoader.getItemRegistry().getById("POWER_BOMB");
        assertNotNull(powerBombTank);
        assertTrue(powerBombTank.isTank());

        assertEquals("POWER_BOMB", powerBombTank.getResourceType());
        assertEquals(5, powerBombTank.getCapacityIncrease());
    }

    @Test
    @DisplayName("Energy Tank should have resource properties from JSON")
    void testEnergyTankHasResourceProperties() throws IOException {
        dataLoader.loadAllData();

        ItemDefinition energyTank = dataLoader.getItemRegistry().getById("ENERGY_TANK");
        assertNotNull(energyTank);
        assertTrue(energyTank.isTank());

        assertEquals("ENERGY", energyTank.getResourceType());
        assertEquals(100, energyTank.getCapacityIncrease());
    }
}
