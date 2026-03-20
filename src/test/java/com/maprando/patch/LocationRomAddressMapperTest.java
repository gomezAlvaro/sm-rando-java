package com.maprando.patch;

import com.maprando.data.DataLoader;
import com.maprando.data.model.LocationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for LocationRomAddressMapper.
 * Tests ROM address lookup, SNES to PC address conversion, and validation.
 */
class LocationRomAddressMapperTest {

    private LocationRomAddressMapper mapper;
    private DataLoader dataLoader;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();
        mapper = new LocationRomAddressMapper(dataLoader);
    }

    @Test
    void testGetRomAddress_ExistingLocation() {
        String address = mapper.getRomAddress("brinstar_morph_ball_room");
        assertNotNull(address);
        assertEquals("0x8282F5", address);
    }

    @Test
    void testGetRomAddress_NonExistentLocation() {
        String address = mapper.getRomAddress("nonexistent_location");
        assertNull(address);
    }

    @Test
    void testGetRomAddress_NullLocation() {
        String address = mapper.getRomAddress(null);
        assertNull(address);
    }

    @Test
    void testHasRomAddress_ExistingLocation() {
        assertTrue(mapper.hasRomAddress("brinstar_morph_ball_room"));
        assertTrue(mapper.hasRomAddress("tourian_metroids"));
    }

    @Test
    void testHasRomAddress_NonExistentLocation() {
        assertFalse(mapper.hasRomAddress("nonexistent_location"));
    }

    @Test
    void testHasRomAddress_NullLocation() {
        assertFalse(mapper.hasRomAddress(null));
    }

    @Test
    void testGetAllMappedLocations() {
        var locations = mapper.getAllMappedLocations();
        assertNotNull(locations);
        assertFalse(locations.isEmpty());

        // Verify all locations have ROM addresses
        for (String locationId : locations) {
            assertNotNull(mapper.getRomAddress(locationId));
        }
    }

    @Test
    void testSnesToPcAddress() {
        // Test SNES to PC address conversion
        // 0x8282F5 = ((0x8282F5 >> 1) & 0x3F8000) | (0x8282F5 & 0x7FFF) = 0x102F5
        assertEquals(0x102F5, LocationRomAddressMapper.snesToPc("0x8282F5"));
        assertEquals(0x0000, LocationRomAddressMapper.snesToPc("0x800000"));
        assertEquals(0x7FFF, LocationRomAddressMapper.snesToPc("0x80FFFF"));
    }

    @Test
    void testSnesToPcAddress_InvalidFormat() {
        // Test invalid formats
        assertThrows(IllegalArgumentException.class, () -> {
            LocationRomAddressMapper.snesToPc("invalid");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LocationRomAddressMapper.snesToPc("0xGHIJK");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LocationRomAddressMapper.snesToPc(null);
        });
    }

    @Test
    void testGetPcAddress() {
        // Test getting PC address for location
        // 0x8282F5 SNES = 0x102F5 PC
        int pcAddr = mapper.getPcAddress("brinstar_morph_ball_room");
        assertEquals(0x102F5, pcAddr);
    }

    @Test
    void testGetPcAddress_NoRomAddress() {
        int pcAddr = mapper.getPcAddress("nonexistent_location");
        assertEquals(-1, pcAddr);
    }

    @Test
    void testValidateRomAddressFormat() {
        assertTrue(LocationRomAddressMapper.isValidRomAddressFormat("0x8282F5"));
        assertTrue(LocationRomAddressMapper.isValidRomAddressFormat("0x800000"));
        assertTrue(LocationRomAddressMapper.isValidRomAddressFormat("0xFFFFFF"));

        assertFalse(LocationRomAddressMapper.isValidRomAddressFormat("7E82F5"));
        assertFalse(LocationRomAddressMapper.isValidRomAddressFormat("0x7E82G5"));
        assertFalse(LocationRomAddressMapper.isValidRomAddressFormat(null));
        assertFalse(LocationRomAddressMapper.isValidRomAddressFormat(""));
    }

    @Test
    void testMapperWithAllLocations() {
        // Test that mapper works with all loaded locations
        var locationData = dataLoader.getLocationData();
        assertNotNull(locationData);
        assertNotNull(locationData.getLocations());

        int mappedCount = 0;
        for (LocationData.LocationDefinition loc : locationData.getLocations()) {
            if (mapper.hasRomAddress(loc.getId())) {
                mappedCount++;
                assertNotNull(mapper.getRomAddress(loc.getId()));
                assertNotEquals(-1, mapper.getPcAddress(loc.getId()));
            }
        }

        // All locations should have ROM addresses in our test data
        assertTrue(mappedCount > 0, "At least some locations should have ROM addresses");
    }
}
